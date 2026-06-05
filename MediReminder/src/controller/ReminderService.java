package controller;

import model.MedicationStatus;
import model.Patient;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ReminderService - background service that checks for due medicines every minute.
 * Runs on a daemon thread so it stops when the application exits.
 */
public class ReminderService {

    private static final long CHECK_INTERVAL_MS = 60_000; // every 60 seconds

    private final MedicineController controller = new MedicineController();
    private Timer timer;
    private final Patient patient;
    private final JFrame parentFrame;

    public ReminderService(Patient patient, JFrame parentFrame) {
        this.patient     = patient;
        this.parentFrame = parentFrame;
    }

    /** Start the reminder background thread. */
    public void start() {
        // Generate today's status rows on startup
        controller.generateTodayStatuses(patient.getId());

        timer = new Timer("ReminderTimer", true); // daemon = true
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAndRemind();
            }
        }, 0, CHECK_INTERVAL_MS);

        System.out.println("[ReminderService] Started for patient: " + patient.getPatientName());
    }

    /** Stop the reminder service. */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            System.out.println("[ReminderService] Stopped.");
        }
    }

    /** Check for due doses and show popups on the EDT. */
    private void checkAndRemind() {
        try {
            // First, mark any overdue doses as MISSED
            controller.processMissed();

            // Get doses that are now due
            List<MedicationStatus> due = controller.getTodayPending(patient.getId());
            for (MedicationStatus ms : due) {
                SwingUtilities.invokeLater(() -> showReminderPopup(ms));
            }
        } catch (SQLException e) {
            System.err.println("[ReminderService] DB error: " + e.getMessage());
        }
    }

    /** Show a JOptionPane reminder popup for a due medicine. */
    private void showReminderPopup(MedicationStatus ms) {
        String message = "<html><body style='width:280px'>"
            + "<h2 style='color:#1565C0'>⏰ Medicine Reminder</h2>"
            + "<b>Medicine:</b> " + ms.getMedicineName() + "<br>"
            + "<b>Dosage:</b> " + ms.getDosage() + "<br>"
            + "<b>Instructions:</b> " + (ms.getInstructions() != null ? ms.getInstructions() : "—") + "<br>"
            + "<b>Scheduled:</b> " + ms.getScheduledTime()
            + "</body></html>";

        String[] options = {"✅ Mark as Taken", "⏰ Snooze 15 min", "❌ Skip"};
        int choice = JOptionPane.showOptionDialog(
            parentFrame, message, "Medicine Reminder",
            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0]
        );

        try {
            if (choice == 0) {
                controller.markTaken(ms.getId());
                JOptionPane.showMessageDialog(parentFrame,
                    "✅ " + ms.getMedicineName() + " marked as TAKEN.", "Done",
                    JOptionPane.INFORMATION_MESSAGE);
            } else if (choice == 1) {
                controller.markSnoozed(ms.getId());
                // Re-check in 15 minutes
                Timer snoozeTimer = new Timer("SnoozeTimer", true);
                snoozeTimer.schedule(new TimerTask() {
                    @Override public void run() {
                        SwingUtilities.invokeLater(() -> showReminderPopup(ms));
                    }
                }, 15 * 60 * 1000L);
            } else if (choice == 2) {
                controller.markSkipped(ms.getId());
            }
            // If dialog is closed (choice == -1), do nothing – it remains PENDING until MISSED
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentFrame,
                "Error updating status: " + e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
