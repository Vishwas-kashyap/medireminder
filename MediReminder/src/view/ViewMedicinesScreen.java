package view;

import controller.MedicineController;
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
 * ViewMedicinesScreen - shows medicines assigned to a specific patient.
 * Doctor can edit or delete each schedule from here.
 */
public class ViewMedicinesScreen extends JDialog {

    private final MedicineController controller = new MedicineController();
    private final User     doctor;
    private final Patient  patient;
    private final JFrame   owner;

    private DefaultTableModel tableModel;
    private JTable table;
    private List<Medicine> medicines;

    public ViewMedicinesScreen(User doctor, Patient patient, JFrame owner) {
        super(owner, "Medicines – " + patient.getPatientName(), true);
        this.doctor  = doctor;
        this.patient = patient;
        this.owner   = owner;
        setSize(760, 480);
        setLocationRelativeTo(owner);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtil.LIGHT_BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtil.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("💊 Medicines for " + patient.getPatientName());
        title.setFont(UIUtil.FONT_HEADING);
        title.setForeground(Color.WHITE);
        JButton addBtn = UIUtil.successButton("➕ Add Medicine");
        addBtn.addActionListener(e -> {
            dispose();
            new AddMedicineForm(doctor, patient, owner).setVisible(true);
        });
        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        // Table
        String[] cols = {"#", "Medicine", "Dosage", "Time", "Start", "End", "Instructions", "Edit", "Delete"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 7 || c == 8; }
        };
        table = new JTable(tableModel);
        table.setFont(UIUtil.FONT_BODY);
        table.setRowHeight(30);
        table.getTableHeader().setFont(UIUtil.FONT_LABEL);
        table.getTableHeader().setBackground(UIUtil.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setMaxWidth(35);

        // Edit button column
        table.getColumn("Edit").setCellRenderer(new BtnRenderer("Edit", UIUtil.PRIMARY));
        table.getColumn("Edit").setCellEditor(new ActionEditor("Edit"));
        table.getColumn("Edit").setPreferredWidth(60);

        // Delete button column
        table.getColumn("Delete").setCellRenderer(new BtnRenderer("Delete", UIUtil.DANGER));
        table.getColumn("Delete").setCellEditor(new ActionEditor("Delete"));
        table.getColumn("Delete").setPreferredWidth(60);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton closeBtn = UIUtil.dangerButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);

        root.add(header, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            medicines = controller.getMedicinesForPatient(patient.getId(), doctor.getId());
            int i = 1;
            for (Medicine m : medicines) {
                tableModel.addRow(new Object[]{
                    i++, m.getMedicineName(), m.getDosage(),
                    m.getReminderTime().toString().substring(0, 5),
                    m.getStartDate(), m.getEndDate(),
                    m.getInstructions() != null ? m.getInstructions() : "—",
                    "Edit", "Delete"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private class BtnRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        private final Color bg;
        BtnRenderer(String text, Color bg) { super(text); this.bg = bg; setOpaque(true); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setBackground(bg); setForeground(Color.WHITE); setFont(UIUtil.FONT_SMALL); return this;
        }
    }

    private class ActionEditor extends DefaultCellEditor {
        private final String type;
        ActionEditor(String type) {
            super(new JCheckBox());
            this.type = type;
            editorComponent = new JButton(type);
            ((JButton) editorComponent).addActionListener(e -> handleAction());
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int row, int col) {
            ((JButton) editorComponent).setBackground("Edit".equals(type) ? UIUtil.PRIMARY : UIUtil.DANGER);
            ((JButton) editorComponent).setForeground(Color.WHITE);
            return editorComponent;
        }
        @Override public Object getCellEditorValue() { return type; }
        private void handleAction() {
            stopCellEditing();
            int row = table.getSelectedRow();
            if (row < 0 || row >= medicines.size()) return;
            Medicine med = medicines.get(row);
            if ("Edit".equals(type)) {
                dispose();
                new AddMedicineForm(doctor, patient, med, owner).setVisible(true);
            } else {
                int confirm = JOptionPane.showConfirmDialog(ViewMedicinesScreen.this,
                    "Delete medicine: " + med.getMedicineName() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    String err = controller.deleteMedicine(med.getId());
                    if (err != null) JOptionPane.showMessageDialog(ViewMedicinesScreen.this, err);
                    else loadData();
                }
            }
        }
    }
}
