package view;

import controller.MedicineController;
import model.MedicationStatus;
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
 * MedicationStatusScreen - shows full dose history for a patient (doctor's view).
 */
public class MedicationStatusScreen extends JDialog {

    private final MedicineController controller = new MedicineController();
    private final Patient patient;

    private DefaultTableModel tableModel;

    public MedicationStatusScreen(User doctor, Patient patient, JFrame owner) {
        super(owner, "Medication Status – " + patient.getPatientName(), true);
        this.patient = patient;
        setSize(740, 480);
        setLocationRelativeTo(owner);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtil.LIGHT_BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtil.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("📊 Status for " + patient.getPatientName());
        title.setFont(UIUtil.FONT_HEADING);
        title.setForeground(Color.WHITE);
        JButton refreshBtn = UIUtil.successButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());
        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        legend.setBackground(UIUtil.LIGHT_BG);
        legend.add(legendItem("✅ TAKEN",   new Color(0xE8F5E9)));
        legend.add(legendItem("❌ MISSED",  new Color(0xFFEBEE)));
        legend.add(legendItem("⏳ PENDING", new Color(0xFFF8E1)));
        legend.add(legendItem("⏰ SNOOZED", new Color(0xE3F2FD)));
        legend.add(legendItem("⛔ SKIPPED", new Color(0xF3E5F5)));

        String[] cols = {"Medicine", "Dosage", "Scheduled Date", "Time", "Status", "Acted At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        DoctorDashboard.styleStatusTable(table);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton closeBtn = UIUtil.dangerButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);

        root.add(header, BorderLayout.NORTH);
        root.add(legend, BorderLayout.CENTER); // temp placeholder
        JPanel center = new JPanel(new BorderLayout());
        center.add(legend, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JLabel legendItem(String text, Color bg) {
        JLabel lbl = new JLabel("  " + text + "  ");
        lbl.setFont(UIUtil.FONT_SMALL);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setBorder(BorderFactory.createLineBorder(UIUtil.BORDER));
        return lbl;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<MedicationStatus> list = controller.getStatusForPatient(patient.getId());
            for (MedicationStatus ms : list) {
                tableModel.addRow(new Object[]{
                    ms.getMedicineName(), ms.getDosage(),
                    ms.getScheduledDate(), ms.getScheduledTime(),
                    ms.getStatus().name(),
                    ms.getActionTime() != null ? ms.getActionTime().toString() : "—"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
}
