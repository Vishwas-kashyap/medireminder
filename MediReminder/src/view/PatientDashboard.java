package view;

import controller.MedicineController;
import controller.ReminderService;
import model.MedicationStatus;
import model.Medicine;
import model.Patient;
import model.User;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * PatientDashboard - main interface for the Patient role.
 * Shows assigned medicines, allows taking/skipping, and displays history.
 */
public class PatientDashboard extends JFrame {

    private final User               patient;
    private final Patient            patientRecord;
    private final MedicineController controller = new MedicineController();
    private ReminderService          reminderService;

    private CardLayout  cardLayout;
    private JPanel      contentPanel;
    private DefaultTableModel medModel, historyModel;

    public PatientDashboard(User patient, Patient patientRecord) {
        this.patient       = patient;
        this.patientRecord = patientRecord;
        setTitle("MediReminder – Patient Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 600);
        setLocationRelativeTo(null);
        buildUI();
        startReminders();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(),  BorderLayout.WEST);
        add(buildContent(),  BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x00796B));
        panel.setPreferredSize(new Dimension(210, 0));

        JPanel userPanel = new JPanel(new GridBagLayout());
        userPanel.setBackground(new Color(0x00796B));
        userPanel.setBorder(new EmptyBorder(20, 16, 20, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel emoji = new JLabel("🧑‍⚕️", SwingConstants.CENTER);
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        JLabel name  = new JLabel(patient.getFullName(), SwingConstants.CENTER);
        name.setFont(UIUtil.FONT_LABEL); name.setForeground(Color.WHITE);
        JLabel doc  = new JLabel("Dr: " + (patientRecord.getDoctorName() != null
            ? patientRecord.getDoctorName() : "—"), SwingConstants.CENTER);
        doc.setFont(UIUtil.FONT_SMALL); doc.setForeground(new Color(0xB2DFDB));

        gc.gridy = 0; userPanel.add(emoji, gc);
        gc.gridy = 1; gc.insets = new Insets(6,0,2,0); userPanel.add(name, gc);
        gc.gridy = 2; gc.insets = new Insets(0,0,0,0); userPanel.add(doc, gc);

        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 0, 2));
        menuPanel.setBackground(new Color(0x00796B));
        menuPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[][] items = {
            {"💊  My Medicines",  "medicines"},
            {"📋  Dose History",  "history"}
        };
        for (String[] it : items) {
            JButton btn = sideBtn(it[0], it[1]);
            menuPanel.add(btn);
        }

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(0x00796B));
        bottom.setBorder(new EmptyBorder(8, 8, 16, 8));
        JButton logout = sideBtn("🚪  Logout", "logout");
        logout.setBackground(UIUtil.DANGER);
        bottom.add(logout);

        panel.add(userPanel, BorderLayout.NORTH);
        panel.add(menuPanel, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JButton sideBtn(String text, String action) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtil.FONT_BODY);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0x00897B));
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> handleMenu(action));
        return btn;
    }

    private JPanel buildContent() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(buildMedicinesPanel(), "medicines");
        contentPanel.add(buildHistoryPanel(),   "history");
        return contentPanel;
    }

    private void handleMenu(String action) {
        if ("logout".equals(action)) {
            if (reminderService != null) reminderService.stop();
            dispose();
            new LoginView().setVisible(true);
            return;
        }
        cardLayout.show(contentPanel, action);
        if ("medicines".equals(action)) refreshMedicines();
        if ("history".equals(action))   refreshHistory();
    }

    // ============================================================
    //  MY MEDICINES PANEL
    // ============================================================
    private JPanel buildMedicinesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtil.LIGHT_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("My Medicines");
        title.setFont(UIUtil.FONT_TITLE);
        JButton refreshBtn = UIUtil.primaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshMedicines());
        top.add(title, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"#", "Medicine", "Dosage", "Instructions", "Time", "Start", "End", "Assigned By"};
        medModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(medModel);
        table.setFont(UIUtil.FONT_BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UIUtil.FONT_LABEL);
        table.getTableHeader().setBackground(new Color(0x00796B));
        table.getTableHeader().setForeground(Color.WHITE);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom action bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setOpaque(false);
        JLabel info = new JLabel("Reminders run automatically in background. Use popup to act on each dose.");
        info.setFont(UIUtil.FONT_SMALL);
        info.setForeground(UIUtil.TEXT_MUTED);
        actions.add(info);
        panel.add(actions, BorderLayout.SOUTH);

        refreshMedicines();
        return panel;
    }

    private void refreshMedicines() {
        if (medModel == null) return;
        medModel.setRowCount(0);
        try {
            List<Medicine> meds = controller.getMedicinesByPatient(patientRecord.getId());
            int i = 1;
            for (Medicine m : meds) {
                medModel.addRow(new Object[]{
                    i++, m.getMedicineName(), m.getDosage(),
                    m.getInstructions() != null ? m.getInstructions() : "—",
                    m.getReminderTime(), m.getStartDate(), m.getEndDate(),
                    m.getDoctorName() != null ? m.getDoctorName() : "—"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + e.getMessage());
        }
    }

    // ============================================================
    //  DOSE HISTORY PANEL
    // ============================================================
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtil.LIGHT_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Dose History");
        title.setFont(UIUtil.FONT_TITLE);
        JButton refreshBtn = UIUtil.primaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshHistory());
        top.add(title, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"Medicine", "Dosage", "Date", "Time", "Status", "Action At"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(historyModel);
        DoctorDashboard.styleStatusTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshHistory() {
        if (historyModel == null) return;
        historyModel.setRowCount(0);
        try {
            for (MedicationStatus ms : controller.getPatientHistory(patientRecord.getId())) {
                historyModel.addRow(new Object[]{
                    ms.getMedicineName(), ms.getDosage(),
                    ms.getScheduledDate(), ms.getScheduledTime(),
                    ms.getStatus().name(),
                    ms.getActionTime() != null ? ms.getActionTime().toString() : "—"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading history: " + e.getMessage());
        }
    }

    /** Start the background reminder service after the window is shown. */
    private void startReminders() {
        reminderService = new ReminderService(patientRecord, this);
        // Start in a brief delay to let the window fully render first
        Timer t = new Timer(1500, e -> reminderService.start());
        t.setRepeats(false);
        t.start();
    }
}
