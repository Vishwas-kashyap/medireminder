// ═══════════════════════════════════════════
//   MediReminder Service Worker
//   Schedules alarms INSIDE the SW independently
//   Works even when Chrome tab is in background
// ═══════════════════════════════════════════

const scheduledAlarms = {};  // medId -> timeoutId

self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', event => event.waitUntil(self.clients.claim()));

// ─── Receive messages from the page ───
self.addEventListener('message', event => {
    const { type } = event.data;

    if (type === 'REGISTER_ALARMS') {
        registerAlarms(event.data.alarms);
    }

    if (type === 'CLEAR_ALARMS') {
        clearAllAlarms();
    }

    // Fallback: page can still directly ask SW to show a notification
    if (type === 'SHOW_ALARM') {
        showMedNotification(event.data.medName);
    }
});

// ─── Schedule independent timers inside the SW ───
function registerAlarms(alarms) {
    clearAllAlarms();

    alarms.forEach(alarm => {
        const delay = getDelayMs(alarm.time);

        // Only schedule alarms that are within the next 12 hours
        if (delay >= 0 && delay <= 43200000) {
            console.log(`[SW] Scheduling alarm for "${alarm.medName}" in ${Math.round(delay / 60000)} min`);
            scheduledAlarms[alarm.id] = setTimeout(() => {
                showMedNotification(alarm.medName);
            }, delay);
        }
    });
}

function clearAllAlarms() {
    Object.values(scheduledAlarms).forEach(id => clearTimeout(id));
    Object.keys(scheduledAlarms).forEach(k => delete scheduledAlarms[k]);
}

// ─── Parse "8:30 AM" → milliseconds from now ───
function getDelayMs(timeStr) {
    const match = timeStr.match(/(\d+):(\d+)\s*(AM|PM)/i);
    if (!match) return -1;

    let h = parseInt(match[1]);
    const m = parseInt(match[2]);
    const period = match[3].toUpperCase();

    if (period === 'PM' && h !== 12) h += 12;
    if (period === 'AM' && h === 12) h = 0;

    const now = new Date();
    const alarm = new Date();
    alarm.setHours(h, m, 0, 0);

    // If time already passed today, don't schedule
    return alarm - now;
}

// ─── Show the notification (works in background) ───
function showMedNotification(medName) {
    return self.registration.showNotification('💊 MediReminder — Dose Due!', {
        body: `Time to take: ${medName}. Tap to open.`,
        icon: '/favicon.svg',
        badge: '/favicon.svg',
        vibrate: [400, 150, 400, 150, 800],
        requireInteraction: true,   // stays visible until dismissed
        silent: false,              // plays system notification sound
        tag: 'med-alarm-' + Date.now()
    });
}

// ─── Tap notification → open / focus the app ───
self.addEventListener('notificationclick', event => {
    event.notification.close();
    event.waitUntil(
        self.clients.matchAll({ type: 'window', includeUncontrolled: true })
            .then(clientList => {
                for (const client of clientList) {
                    if ('focus' in client) return client.focus();
                }
                return self.clients.openWindow('/');
            })
    );
});
