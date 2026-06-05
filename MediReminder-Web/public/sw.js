// ═══════════════════════════════════════════════════════════════
//   MediReminder Service Worker — EMERGENCY SIREN MODE
// ═══════════════════════════════════════════════════════════════

const DB_NAME   = 'medireminder-alarms';
const DB_STORE  = 'alarms';
const DB_VER    = 1;

let activeAlarms = {}; // To manage looping notifications

self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', event => event.waitUntil(self.clients.claim()));

// ─── IndexedDB Utils ──────────────────────────────────────────
function openDB() {
    return new Promise((resolve, reject) => {
        const req = indexedDB.open(DB_NAME, DB_VER);
        req.onupgradeneeded = e => {
            const db = e.target.result;
            if (!db.objectStoreNames.contains(DB_STORE)) db.createObjectStore(DB_STORE, { keyPath: 'id' });
        };
        req.onsuccess = e => resolve(e.target.result);
        req.onerror = e => reject(e.target.error);
    });
}

async function saveAlarms(alarms) {
    const db = await openDB();
    const tx = db.transaction(DB_STORE, 'readwrite');
    const store = tx.objectStore(DB_STORE);
    store.clear();
    alarms.forEach(a => store.put(a));
}

async function loadAlarms() {
    const db = await openDB();
    const tx = db.transaction(DB_STORE, 'readonly');
    const req = tx.objectStore(DB_STORE).getAll();
    return new Promise(res => req.onsuccess = () => res(req.result || []));
}

async function markFired(id) {
    const db = await openDB();
    const tx = db.transaction(DB_STORE, 'readwrite');
    const store = tx.objectStore(DB_STORE);
    const req = store.get(id);
    req.onsuccess = () => {
        const a = req.result;
        if (a) { a.fired = true; store.put(a); }
    };
}

// ─── Core Siren Engine ───────────────────────────────────────
async function checkAndFireAlarms() {
    const alarms = await loadAlarms();
    const d = new Date();
    const now = d.getHours() * 60 + d.getMinutes();

    for (const alarm of alarms) {
        if (alarm.fired) continue;
        
        // Parse time (e.g., "08:30 AM")
        const match = alarm.time.match(/(\d+):(\d+)\s*(AM|PM)/i);
        if (!match) continue;
        let h = parseInt(match[1]);
        const m = parseInt(match[2]);
        if (match[3].toUpperCase() === 'PM' && h !== 12) h += 12;
        if (match[3].toUpperCase() === 'AM' && h === 12) h = 0;
        
        const target = h * 60 + m;
        const diff = now - target;

        if (diff >= 0 && diff <= 60) {
            // FIRE SIREN
            startSiren(alarm.medName, alarm.id);
            await markFired(alarm.id);
        }
    }
}

function startSiren(medName, id) {
    if (activeAlarms[id]) return;

    // Immediately show the first one
    showSirenNotification(medName, id);

    // Loop every 12 seconds until dismissed
    activeAlarms[id] = setInterval(() => {
        showSirenNotification(medName, id);
    }, 12000);
}

function showSirenNotification(medName, id) {
    self.registration.showNotification('🚨 URGENT: MEDICATION DUE', {
        body: `ACTION REQUIRED: Take ${medName} now. Tapping stops the alarm.`,
        icon: '/icon-512.png',
        badge: '/icon-192.png',
        vibrate: [0, 1000, 200, 1000, 200, 1000, 500, 1000],
        requireInteraction: true,
        tag: 'siren-' + id, // Using same tag replaces the old one, but triggers NEW vibrate/sound
        renotify: true,
        silent: false,
        data: { medName, id }
    });
}

// ─── Events ──────────────────────────────────────────────────
self.addEventListener('message', event => {
    const { type } = event.data || {};
    if (type === 'REGISTER_ALARMS') event.waitUntil(saveAlarms(event.data.alarms).then(() => checkAndFireAlarms()));
    if (type === 'KEEPALIVE_PING') event.waitUntil(checkAndFireAlarms());
});

self.addEventListener('notificationclick', event => {
    const id = event.notification.data.id;
    const medName = event.notification.data.medName;
    
    // STOP THE SIREN
    if (activeAlarms[id]) {
        clearInterval(activeAlarms[id]);
        delete activeAlarms[id];
    }
    
    event.notification.close();
    event.waitUntil(
        self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clients => {
            const url = '/?alarm=' + encodeURIComponent(medName);
            for (const client of clients) {
                if (client.url.includes(location.host)) return client.navigate(url).then(c => c.focus());
            }
            return self.clients.openWindow(url);
        })
    );
});

// Reschedule check every 30s
setInterval(checkAndFireAlarms, 30000);
