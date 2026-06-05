package view;

import controller.AuthController;
import controller.MedicineController;
import database.PatientDAO;
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
 * DoctorDashboard - main interface for Doctor/Guardian role.
 * Features: manage patients, assign medicines, view medication status.
 */
public class DoctorDashboard extends JFrame {

    private final User               doctor;
    private final PatientDAO         patientDAO   = new PatientDAO();
    private final MedicineController medController = new MedicineController();

    private JTable  patientTable;
    private DefaultTableModel patientModel;
    private JTextField searchField;

    public DoctorDashboard(User doctor) {
        this.doctor = doctor;
        setTitle("MediReminder – Doctor Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 640);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // ---- Sidebar ----
        JPanel sidebar = buildSidebar();

        // ---- Main content area (CardLayout) ----
        JPanel content = buildContent();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, content);
        split.setDividerLocation(220);
        split.setDividerSize(1);
        split.setEnabled(false);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtil.PRIMARY_DARK);
        panel.setPreferredSize(new Dimension(220, 0));

        // User info at top
        JPanel userPanel = new JPanel(new GridBagLayout());
        userPanel.setBackground(UIUtil.PRIMARY_DARK);
        userPanel.setBorder(new EmptyBorder(20, 16, 20, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel emojiLabel = new JLabel("👨‍⚕️", SwingConstants.CENTER);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

        JLabel nameLabel = new JLabel(doctor.getFullName(), SwingConstants.CENTER);
        nameLabel.setFont(UIUtil.FONT_LABEL);
        nameLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel("Doctor / Guardian", SwingConstants.CENTER);
        roleLabel.setFont(UIUtil.FONT_SMALL);
        roleLabel.setForeground(new Color(0xBBDEFB));

        gc.gridy = 0; userPanel.add(emojiLabel, gc);
        gc.gridy = 1; gc.insets = new Insets(6,0,2,0); userPanel.add(nameLabel, gc);
        gc.gridy = 2; gc.insets = new Insets(0,0,0,0); userPanel.add(roleLabel, gc);

        // Menu buttons
        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 0, 2));
        menuPanel.setBackground(UIUtil.PRIMARY_DARK);
        menuPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[][] menuItems = {
            {"👥  My Patients",    "patients"},
            {"➕  Add Patient",    "addPatient"},
            {"💊  All Medicines",  "allMed"},
            {"📊  Status Monitor", "monitor"},
            {"📋  Reports",        "reports"}
        };

        for (String[] item : menuItems) {
            JButton btn = createMenuButton(item[0], item[1]);
            menuPanel.add(btn);
        }

        // Logout
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UIUtil.PRIMARY_DARK);
        bottomPanel.setBorder(new EmptyBorder(8, 8, 16, 8));
        JButton logoutBtn = createMenuButton("🚪  Logout", "logout");
        logoutBtn.setBackground(UIUtil.DANGER);
        bottomPanel.add(logoutBtn);

        panel.add(userPanel, BorderLayout.NORTH);
        panel.add(menuPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JButton createMenuButton(String text, String action) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtil.FONT_BODY);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0x1565C0));
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> handleMenu(action));
        return btn;
    }

    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JPanel buildContent() {
        cardLayout  = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(buildPatientsPanel(), "patients");
        contentPanel.add(buildAddPatientPanel(), "addPatient");
        contentPanel.add(buildAllMedicinesPanel(), "allMed");
        contentPanel.add(buildMonitorPanel(), "monitor");
        contentPanel.add(buildReportPanel(), "reports");
        return contentPanel;
    }

    private void handleMenu(String action) {
        if ("logout".equals(action)) {
            dispose();
            new LoginView().setVisible(true);
            return;
        }
        cardLayout.show(contentPanel, action);
        if ("patients".equals(action))    refreshPatients(null);
        if ("allMed".equals(action))      refreshAllMedicines();
        if ("monitor".equals(action))     refreshMonitor();
        if ("reports".equals(action))     refreshReport();
    }

    // ============================================================
    //  PATIENTS PANEL
    // ============================================================
    private JPanel buildPatientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtil.LIGHT_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("My Patients");
        title.setFont(UIUtil.FONT_TITLE);
        title.setForeground(UIUtil.TEXT_PRIMARY);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchField = UIUtil.styledField(18);
        searchField.putClientProperty("JTextField.placeholderText", "Search patient name…");
        JButton searchBtn = UIUtil.primaryButton("🔍 Search");
        JButton clearBtn  = UIUtil.primaryButton("Clear");
        searchBtn.addActionListener(e -> refreshPatients(searchField.getText().trim()));
        clearBtn.addActionListener(e  -> { searchField.setText(""); refreshPatients(null); });
        searchBar.add(new JLabel("Search:")); searchBar.add(searchField);
        searchBar.add(searchBtn); searchBar.add(clearBtn);

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(searchBar, BorderLayout.SOUTH);

        // Table
        String[] cols = {"#", "Name", "Username", "Date of Birth", "Action"};
        patientModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        patientTable = new JTable(patientModel);
        patientTable.setFont(UIUtil.FONT_BODY);
        patientTable.setRowHeight(32);
        patientTable.getTableHeader().setFont(UIUtil.FONT_LABEL);
        patientTable.getTableHeader().setBackground(UIUtil.PRIMARY);
        patientTable.getTableHeader().setForeground(Color.WHITE);
        patientTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        patientTable.getColumn("Action").setCellEditor(new PatientActionEditor());
        patientTable.getColumnModel().getColumn(0).setMaxWidth(40);
        patientTable.getColumnModel().getColumn(4).setPreferredWidth(160);

        JScrollPane scroll = new JScrollPane(patientTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtil.BORDER));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        refreshPatients(null);
        return panel;
    }

    void refreshPatients(String keyword) {
        patientModel.setRowCount(0);
        try {
            List<Patient> patients = (keyword == null || keyword.isEmpty())
                ? patientDAO.getPatientsByDoctor(doctor.getId())
                : patientDAO.searchPatients(doctor.getId(), keyword);
            int i = 1;
            for (Patient p : patients) {
                patientModel.addRow(new Object[]{
                    i++,
                    p.getPatientName(),
                    p.getPatientUsername(),
                    p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : "—",
                    "Manage"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage());
        }
    }

    /** Button renderer for table action column. */
    private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        ButtonRenderer() { setOpaque(true); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            setText(value != null ? value.toString() : "");
            setBackground(UIUtil.PRIMARY);
            setForeground(Color.WHITE);
            setFont(UIUtil.FONT_SMALL);
            return this;
        }
    }

    /** Editor that opens a patient action menu. */
    private class PatientActionEditor extends DefaultCellEditor {
        private Patient selectedPatient;
        PatientActionEditor() {
            super(new JCheckBox());
            editorComponent = new JButton();
            ((JButton) editorComponent).addActionListener(e -> openPatientMenu());
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSel, int row, int col) {
            try {
                List<Patient> patients = patientDAO.getPatientsByDoctor(doctor.getId());
                if (row < patients.size()) selectedPatient = patients.get(row);
            } catch (SQLException ignored) {}
            ((JButton) editorComponent).setText("Manage");
            ((JButton) editorComponent).setBackground(UIUtil.PRIMARY);
            ((JButton) editorComponent).setForeground(Color.WHITE);
            return editorComponent;
        }
        @Override public Object getCellEditorValue() { return "Manage"; }
        private void openPatientMenu() {
            stopCellEditing();
            if (selectedPatient == null) return;
            String[] options = {"💊 View Medicines", "➕ Assign Medicine", "📊 View Status"};
            int choice = JOptionPane.showOptionDialog(DoctorDashboard.this,
                "Select action for: " + selectedPatient.getPatientName(),
                "Patient Actions", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (choice == 0) new ViewMedicinesScreen(doctor, selectedPatient, DoctorDashboard.this).setVisible(true);
            else if (choice == 1) new AddMedicineForm(doctor, selectedPatient, DoctorDashboard.this).setVisible(true);
            else if (choice == 2) new MedicationStatusScreen(doctor, selectedPatient, DoctorDashboard.this).setVisible(true);
        }
    }

    // ============================================================
    //  ADD PATIENT PANEL
    // ============================================================
    private JPanel buildAddPatientPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtil.LIGHT_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Add Patient");
        title.setFont(UIUtil.FONT_TITLE);
        title.setForeground(UIUtil.TEXT_PRIMARY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIUtil.CARD_BG);
        form.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 0, 6, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.weightx = 1;

        JTextField usernameField  = UIUtil.styledField(20);
        JTextField dobField       = UIUtil.styledField(20);
        dobField.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd");
        JTextArea  notesArea      = new JTextArea(3, 20);
        notesArea.setFont(UIUtil.FONT_BODY);
        notesArea.setBorder(BorderFactory.createLineBorder(UIUtil.BORDER));
        JLabel statusLbl = new JLabel(" ", SwingConstants.CENTER);
        statusLbl.setFont(UIUtil.FONT_SMALL);
        statusLbl.setForeground(UIUtil.DANGER);

        JButton addBtn = UIUtil.primaryButton("Add Patient");
        addBtn.addActionListener(e -> {
            String err = new AuthController().addPatientToDoctor(
                usernameField.getText().trim(), doctor.getId(),
                dobField.getText().trim(), notesArea.getText().trim());
            if (err != null) { statusLbl.setForeground(UIUtil.DANGER); statusLbl.setText(err); }
            else {
                statusLbl.setForeground(UIUtil.SUCCESS);
                statusLbl.setText("Patient added successfully!");
                usernameField.setText(""); dobField.setText(""); notesArea.setText("");
                refreshPatients(null);
            }
        });

        String[][] fields = {{"Patient Username *", null}, {null, "usernameField"},
                             {"Date of Birth", null}, {null, "dobField"},
                             {"Medical Notes", null}, {null, "notesArea"}};
        int row = 0;
        c.gridy = row++; form.add(label("Patient Username *"), c);
        c.gridy = row++; form.add(usernameField, c);
        c.gridy = row++; form.add(label("Date of Birth (yyyy-MM-dd)"), c);
        c.gridy = row++; form.add(dobField, c);
        c.gridy = row++; form.add(label("Medical Notes"), c);
        c.gridy = row++; form.add(new JScrollPane(notesArea), c);
        c.gridy = row++; c.insets = new Insets(14, 0, 4, 0); form.add(addBtn, c);
        c.gridy = row;   c.insets = new Insets(4, 0, 0, 0);  form.add(statusLbl, c);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIUtil.FONT_LABEL);
        lbl.setForeground(UIUtil.TEXT_MUTED);
        return lbl;
    }

    // ============================================================
    //  ALL MEDICINES PANEL
    // ============================================================
    private DefaultTableModel allMedModel;

    private JPanel buildAllMedicinesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtil.LIGHT_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("All Assigned Medicines");
        title.setFont(UIUtil.FONT_TITLE);
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"#", "Patient", "Medicine", "Dosage", "Time", "Start", "End"};
        allMedModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(allMedModel);
        table.setFont(UIUtil.FONT_BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UIUtil.FONT_LABEL);
        table.getTableHeader().setBackground(UIUtil.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshAllMedicines() {
        allMedModel.setRowCount(0);
        try {
            int i = 1;
            for (var med : medController.getMedicinesByDoctor(doctor.getId())) {
                allMedModel.addRow(new Object[]{
                    i++, med.getPatientName(), med.getMedicineName(), med.getDosage(),
                    med.getReminderTime(), med.getStartDate(), med.getEndDate()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // ============================================================
    //  MONITOR PANEL
    // ============================================================
    private DefaultTableModel monitorModel;

    private JPanel buildMonitorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtil.LIGHT_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Medication Status Monitor");
        title.setFont(UIUtil.FONT_TITLE);
        JButton refreshBtn = UIUtil.primaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshMonitor());
        top.add(title, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"Patient", "Medicine", "Dosage", "Date", "Time", "Status", "Action Time"};
        monitorModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(monitorModel);
        styleStatusTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshMonitor() {
        monitorModel.setRowCount(0);
        try {
            for (var ms : medController.getDoctorMonitoring(doctor.getId())) {
                monitorModel.addRow(new Object[]{
                    ms.getPatientName(), ms.getMedicineName(), ms.getDosage(),
                    ms.getScheduledDate(), ms.getScheduledTime(),
                    ms.getStatus().name(),
                    ms.getActionTime() != null ? ms.getActionTime().toString() : "—"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // ============================================================
    //  REPORTS PANEL
    // ============================================================
    private DefaultTableModel reportModel;

    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtil.LIGHT_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Medication Reports");
        title.setFont(UIUtil.FONT_TITLE);
        JButton genBtn = UIUtil.primaryButton("Generate Report");
        genBtn.addActionListener(e -> refreshReport());
        top.add(title, BorderLayout.WEST);
        top.add(genBtn, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        // Summary cards
        JPanel summary = new JPanel(new GridLayout(1, 3, 12, 0));
        summary.setOpaque(false);
        summary.setBorder(new EmptyBorder(12, 0, 12, 0));
        JLabel takenCard   = statCard("✅ Taken",   "0", UIUtil.SUCCESS);
        JLabel missedCard  = statCard("❌ Missed",  "0", UIUtil.DANGER);
        JLabel pendingCard = statCard("⏳ Pending", "0", UIUtil.WARNING);
        summary.add(takenCard); summary.add(missedCard); summary.add(pendingCard);
        panel.add(summary, BorderLayout.NORTH);   // reuse north by nesting
        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.setOpaque(false);
        northWrap.add(top, BorderLayout.NORTH);
        northWrap.add(summary, BorderLayout.CENTER);
        panel.add(northWrap, BorderLayout.NORTH);

        String[] cols = {"Patient", "Medicine", "Date", "Status"};
        reportModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(reportModel);
        styleStatusTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JLabel statCard(String label, String value, Color color) {
        JLabel card = new JLabel("<html><center><b style='font-size:22px'>" + value +
            "</b><br>" + label + "</center></html>", SwingConstants.CENTER);
        card.setOpaque(true);
        card.setBackground(color);
        card.setForeground(Color.WHITE);
        card.setFont(UIUtil.FONT_BODY);
        card.setBorder(new EmptyBorder(14, 8, 14, 8));
        return card;
    }

    private void refreshReport() {
        reportModel.setRowCount(0);
        try {
            int taken = 0, missed = 0, pending = 0;
            for (var ms : medController.getDoctorMonitoring(doctor.getId())) {
                reportModel.addRow(new Object[]{
                    ms.getPatientName(), ms.getMedicineName(),
                    ms.getScheduledDate(), ms.getStatus().name()
                });
                switch (ms.getStatus()) {
                    case TAKEN:   taken++;   break;
                    case MISSED:  missed++;  break;
                    case PENDING: pending++; break;
                    default: break;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    /** Apply status-based row coloring to a table. */
    static void styleStatusTable(JTable table) {
        table.setFont(UIUtil.FONT_BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UIUtil.FONT_LABEL);
        table.getTableHeader().setBackground(UIUtil.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                // Find the Status column
                int statusCol = -1;
                for (int i = 0; i < t.getColumnCount(); i++) {
                    if ("Status".equalsIgnoreCase(t.getColumnName(i))) { statusCol = i; break; }
                }
                if (statusCol >= 0 && row < t.getRowCount()) {
                    Object statusVal = t.getValueAt(row, statusCol);
                    if ("TAKEN".equals(statusVal))   comp.setBackground(new Color(0xE8F5E9));
                    else if ("MISSED".equals(statusVal)) comp.setBackground(new Color(0xFFEBEE));
                    else if ("PENDING".equals(statusVal)) comp.setBackground(new Color(0xFFF8E1));
                    else if ("SNOOZED".equals(statusVal)) comp.setBackground(new Color(0xE3F2FD));
                    else if ("SKIPPED".equals(statusVal)) comp.setBackground(new Color(0xF3E5F5));
                    else comp.setBackground(Color.WHITE);
                    if (sel) comp.setBackground(UIUtil.ACCENT);
                }
                return comp;
            }
        });
    }
}
