package view;

import controller.MedicineController;
import model.Medicine;
import model.Patient;
import model.User;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * AddMedicineForm - dialog for assigning or editing a medicine schedule.
 * Can be used both for creating new and updating existing medicines.
 */
public class AddMedicineForm extends JDialog {

    private final MedicineController controller = new MedicineController();
    private final User               doctor;
    private final Patient            patient;
    private final Medicine           existing;     // null = add new, non-null = edit

    private JTextField nameField, dosageField, timeField, startField, endField;
    private JTextArea  instructionsArea;
    private JLabel     statusLabel;

    /** Constructor for adding a new medicine. */
    public AddMedicineForm(User doctor, Patient patient, JFrame owner) {
        this(doctor, patient, null, owner);
    }

    /** Constructor for editing an existing medicine. */
    public AddMedicineForm(User doctor, Patient patient, Medicine existing, JFrame owner) {
        super(owner, existing == null ? "Assign Medicine" : "Edit Medicine", true);
        this.doctor   = doctor;
        this.patient  = patient;
        this.existing = existing;
        setSize(460, 520);
        setLocationRelativeTo(owner);
        buildUI();
        if (existing != null) populateFields();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtil.LIGHT_BG);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(UIUtil.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel headerTitle = new JLabel(existing == null
            ? "💊 Assign Medicine to " + patient.getPatientName()
            : "✏️ Edit Medicine – " + patient.getPatientName());
        headerTitle.setFont(UIUtil.FONT_HEADING);
        headerTitle.setForeground(Color.WHITE);
        header.add(headerTitle);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIUtil.CARD_BG);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 0, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.weightx = 1;

        nameField         = UIUtil.styledField(20);
        dosageField       = UIUtil.styledField(20);
        timeField         = UIUtil.styledField(10);
        timeField.putClientProperty("JTextField.placeholderText", "HH:mm  e.g. 08:00");
        startField        = UIUtil.styledField(10);
        startField.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd");
        endField          = UIUtil.styledField(10);
        endField.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd");
        instructionsArea  = new JTextArea(3, 20);
        instructionsArea.setFont(UIUtil.FONT_BODY);
        instructionsArea.setBorder(BorderFactory.createLineBorder(UIUtil.BORDER));
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(UIUtil.FONT_SMALL);
        statusLabel.setForeground(UIUtil.DANGER);

        JButton saveBtn   = UIUtil.primaryButton(existing == null ? "Assign Medicine" : "Save Changes");
        JButton cancelBtn = UIUtil.dangerButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        int row = 0;
        c.gridy = row++; form.add(lbl("Medicine Name *"), c);
        c.gridy = row++; form.add(nameField, c);
        c.gridy = row++; form.add(lbl("Dosage *  (e.g. 500mg)"), c);
        c.gridy = row++; form.add(dosageField, c);
        c.gridy = row++; form.add(lbl("Reminder Time * (HH:mm)"), c);
        c.gridy = row++; form.add(timeField, c);
        c.gridy = row++; form.add(lbl("Start Date * (yyyy-MM-dd)"), c);
        c.gridy = row++; form.add(startField, c);
        c.gridy = row++; form.add(lbl("End Date * (yyyy-MM-dd)"), c);
        c.gridy = row++; form.add(endField, c);
        c.gridy = row++; form.add(lbl("Instructions / Notes"), c);
        c.gridy = row++; form.add(new JScrollPane(instructionsArea), c);
        c.gridy = row++;  c.insets = new Insets(4, 0, 0, 0); form.add(statusLabel, c);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        c.gridy = row; c.insets = new Insets(10, 0, 0, 0); form.add(btnRow, c);

        saveBtn.addActionListener(e -> doSave());

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIUtil.FONT_LABEL);
        l.setForeground(UIUtil.TEXT_MUTED);
        return l;
    }

    private void populateFields() {
        nameField.setText(existing.getMedicineName());
        dosageField.setText(existing.getDosage());
        // Format time as HH:mm
        String time = existing.getReminderTime().toString();
        timeField.setText(time.substring(0, 5));
        startField.setText(existing.getStartDate().toString());
        endField.setText(existing.getEndDate().toString());
        if (existing.getInstructions() != null)
            instructionsArea.setText(existing.getInstructions());
    }

    private void doSave() {
        String name     = nameField.getText().trim();
        String dosage   = dosageField.getText().trim();
        String time     = timeField.getText().trim();
        String start    = startField.getText().trim();
        String end      = endField.getText().trim();
        String instruct = instructionsArea.getText().trim();

        String error;
        if (existing == null) {
            error = controller.addMedicine(
                patient.getId(), doctor.getId(), name, dosage, instruct, time, start, end);
        } else {
            error = controller.updateMedicine(
                existing.getId(), name, dosage, instruct, time, start, end);
        }

        if (error != null) {
            statusLabel.setForeground(UIUtil.DANGER);
            statusLabel.setText(error);
        } else {
            JOptionPane.showMessageDialog(this,
                existing == null ? "Medicine assigned successfully!" : "Medicine updated successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }
}
