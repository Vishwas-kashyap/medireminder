// ═══════════════════════════════════════════
//   MediReminder Service Worker
//   Background Medication Alarm Notifications
// ═══════════════════════════════════════════

self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', event => event.waitUntil(self.clients.claim()));

// When user taps the notification → bring app to foreground
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

// Listen for messages from the main page to show notifications
self.addEventListener('message', event => {
    if (event.data && event.data.type === 'SHOW_ALARM') {
        const { medName } = event.data;
        self.registration.showNotification('💊 MediReminder — Dose Due!', {
            body: `Time to take: ${medName}`,
            icon: '/favicon.svg',
            badge: '/favicon.svg',
            vibrate: [300, 150, 300, 150, 600],
            tag: 'medication-alarm-' + Date.now(),
            requireInteraction: true,   // stays on screen until dismissed
            silent: false               // plays system notification sound
        });
    }
});
