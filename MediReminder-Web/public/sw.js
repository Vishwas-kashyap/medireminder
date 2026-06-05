// ═══════════════════════════════════════════════════════════════
//   MediReminder Service Worker — Android-Proof Background Alarms
//   Strategy:
//     1. Store alarms in IndexedDB (survives SW restarts)
//     2. On each 'message' wake, schedule a setTimeout AND
//        store a self-ping URL so the page can send periodic pings
//     3. 'periodicsync' (Android Chrome) fires this every 1 min
//     4. Each check: if an alarm is due, fire notification
//     5. SW keeps itself alive with waitUntil chains
// ═══════════════════════════════════════════════════════════════

const DB_NAME   = 'medireminder-alarms';
const DB_STORE  = 'alarms';
const DB_VER    = 1;

// ─── Install & Activate ───────────────────────────────────────
self.addEventListener('install',  () => self.skipWaiting());
self.addEventListener('activate', event => {
    event.waitUntil(self.clients.claim());
});

// ─── Open IndexedDB ──────────────────────────────────────────
function openDB() {
    return new Promise((resolve, reject) => {
        const req = indexedDB.open(DB_NAME, DB_VER);
        req.onupgradeneeded = e => {
            const db = e.target.result;
            if (!db.objectStoreNames.contains(DB_STORE)) {
                db.createObjectStore(DB_STORE, { keyPath: 'id' });
            }
        };
        req.onsuccess = e => resolve(e.target.result);
        req.onerror   = e => reject(e.target.error);
    });
}

async function saveAlarms(alarms) {
    const db = await openDB();
    return new Promise((resolve, reject) => {
        const tx = db.transaction(DB_STORE, 'readwrite');
        const store = tx.objectStore(DB_STORE);
        store.clear();
        alarms.forEach(a => store.put(a));
        tx.oncomplete = resolve;
        tx.onerror    = e => reject(e.target.error);
    });
}

async function loadAlarms() {
    const db = await openDB();
    return new Promise((resolve, reject) => {
        const tx    = db.transaction(DB_STORE, 'readonly');
        const store = tx.objectStore(DB_STORE);
        const req   = store.getAll();
        req.onsuccess = e => resolve(e.target.result || []);
        req.onerror   = e => reject(e.target.error);
    });
}

async function markFired(id) {
    const db = await openDB();
    return new Promise((resolve, reject) => {
        const tx    = db.transaction(DB_STORE, 'readwrite');
        const store = tx.objectStore(DB_STORE);
        const req   = store.get(id);
        req.onsuccess = e => {
            const alarm = e.target.result;
            if (alarm) { alarm.fired = true; store.put(alarm); }
            resolve();
        };
        req.onerror = e => reject(e.target.error);
    });
}

// ─── Time Helpers ────────────────────────────────────────────
function parseTimeToMins(timeStr) {
    // Accepts "8:30 AM", "08:30 AM", "13:00" (24-hr), "1:00 PM"
    let h, m;
    const ampm = timeStr.match(/(\d{1,2}):(\d{2})\s*(AM|PM)/i);
    const h24  = timeStr.match(/^(\d{1,2}):(\d{2})$/);
    if (ampm) {
        h = parseInt(ampm[1]);
        m = parseInt(ampm[2]);
        const period = ampm[3].toUpperCase();
        if (period === 'PM' && h !== 12) h += 12;
        if (period === 'AM' && h === 12) h = 0;
    } else if (h24) {
        h = parseInt(h24[1]);
        m = parseInt(h24[2]);
    } else {
        return -1;
    }
    return h * 60 + m;
}

function nowMins() {
    const d = new Date();
    return d.getHours() * 60 + d.getMinutes();
}

function minsUntil(targetMins) {
    const diff = targetMins - nowMins();
    return diff < 0 ? diff + 1440 : diff; // wrap around midnight
}

// ─── Core: Check & Fire Due Alarms ───────────────────────────
async function checkAndFireAlarms() {
    const alarms = await loadAlarms();
    const now    = nowMins();

    for (const alarm of alarms) {
        if (alarm.fired) continue;

        const target = parseTimeToMins(alarm.time);
        if (target < 0) continue;

        const diff = now - target; // positive = overdue, negative = future
        // Fire if alarm is due now or was missed in last 90 minutes
        if (diff >= 0 && diff <= 90) {
            await showMedNotification(alarm.medName, alarm.id);
            await markFired(alarm.id);
        }
    }
}

// ─── Show Notification ───────────────────────────────────────
function showMedNotification(medName, alarmId) {
    return self.registration.showNotification('🚨 MediReminder: TIME FOR MEDICINE', {
        body: `URGENT: Take ${medName} now to maintain your streak!`,
        icon: '/icon-512.png',
        badge: '/icon-192.png',
        // Aggressive vibration: [Wait, Vibrate, Wait, Vibrate...]
        vibrate: [0, 500, 200, 500, 200, 500, 1000, 500, 200, 500, 200, 500],
        requireInteraction: true,
        tag: 'urgent-alarm-' + alarmId,
        renotify: true,
        data: { medName, alarmId }
    });
}

// ─── Notification Click → Focus/Open App + Start Alarm Sound ───
self.addEventListener('notificationclick', event => {
    event.notification.close();
    
    // We append ?alarm=... to the URL. The index.html will see this
    // and immediately start the looping chime.
    const medName = event.notification.data ? event.notification.data.medName : 'Medicine';
    const alarmUrl = '/?alarm=' + encodeURIComponent(medName);

    event.waitUntil(
        self.clients.matchAll({ type: 'window', includeUncontrolled: true })
            .then(clientList => {
                // If a tab is already open, navigate it to the alarm URL and focus
                for (const client of clientList) {
                    if (client.url.includes(location.host) && 'focus' in client) {
                        return client.navigate(alarmUrl).then(c => c.focus());
                    }
                }
                // If no tab is open, open a new one with the alarm flag
                if (self.clients.openWindow) {
                    return self.clients.openWindow(alarmUrl);
                }
            })
    );
});

// ─── Message Handler (from page) ────────────────────────────
self.addEventListener('message', event => {
    const { type } = event.data || {};

    if (type === 'REGISTER_ALARMS') {
        // Persist alarms to IndexedDB and set up timers
        event.waitUntil(
            saveAlarms(event.data.alarms).then(() => scheduleNextCheck())
        );
    }

    if (type === 'CLEAR_ALARMS') {
        event.waitUntil(saveAlarms([]));
    }

    if (type === 'SHOW_ALARM') {
        event.waitUntil(showMedNotification(event.data.medName, Date.now()));
    }

    // Page pings SW every 30s to keep it alive on Android
    if (type === 'KEEPALIVE_PING') {
        event.waitUntil(checkAndFireAlarms());
    }
});

// ─── Periodic Sync (Android Chrome 80+) ──────────────────────
self.addEventListener('periodicsync', event => {
    if (event.tag === 'med-alarm-check') {
        event.waitUntil(checkAndFireAlarms());
    }
});

// ─── Push (for future server-push support) ───────────────────
self.addEventListener('push', event => {
    const data = event.data ? event.data.json() : {};
    event.waitUntil(
        showMedNotification(data.medName || 'Medication', data.alarmId || Date.now())
    );
});

// ─── Schedule Next Check via setTimeout chain ─────────────────
// This keeps the SW alive by chaining 30-second self-checks
// Android may throttle this, but combined with page pings it works
let _checkTimer = null;
function scheduleNextCheck() {
    if (_checkTimer) clearTimeout(_checkTimer);
    _checkTimer = setTimeout(async () => {
        await checkAndFireAlarms();
        scheduleNextCheck(); // reschedule self
    }, 30_000);
}

// ─── Notification Click → Focus/Open App ────────────────────
self.addEventListener('notificationclick', event => {
    event.notification.close();
    event.waitUntil(
        self.clients
            .matchAll({ type: 'window', includeUncontrolled: true })
            .then(clientList => {
                for (const client of clientList) {
                    if ('focus' in client) return client.focus();
                }
                return self.clients.openWindow('/');
            })
    );
});

// ─── Fetch Handler (Offline Support) ────────────────────────
self.addEventListener('fetch', event => {
    // Let the browser handle firebase and external requests normally
    if (event.request.url.includes('firebase') ||
        event.request.url.includes('gstatic') ||
        event.request.url.includes('googleapis')) {
        return;
    }
    // For our own assets: network-first, cache fallback
    event.respondWith(
        fetch(event.request).catch(() => caches.match(event.request))
    );
});
