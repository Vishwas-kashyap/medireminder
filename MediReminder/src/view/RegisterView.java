package view;

import controller.AuthController;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * RegisterView - new user registration screen.
 */
public class RegisterView extends JFrame {

    private final AuthController authController = new AuthController();

    private JTextField     usernameField, fullNameField, emailField, phoneField;
    private JPasswordField passwordField, confirmPassField;
    private JComboBox<String> roleCombo;
    private JLabel         statusLabel;

    public RegisterView() {
        setTitle("MediReminder – Register");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(460, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtil.LIGHT_BG);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(UIUtil.PRIMARY);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel title = new JLabel("💊 Create Account");
        title.setFont(UIUtil.FONT_TITLE);
        title.setForeground(Color.WHITE);
        header.add(title);

        // Form card
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIUtil.CARD_BG);
        form.setBorder(new EmptyBorder(24, 32, 24, 32));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 0, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.weightx = 1;

        usernameField  = UIUtil.styledField(20);
        passwordField  = UIUtil.styledPasswordField(20);
        confirmPassField = UIUtil.styledPasswordField(20);
        fullNameField  = UIUtil.styledField(20);
        emailField     = UIUtil.styledField(20);
        phoneField     = UIUtil.styledField(20);
        roleCombo      = new JComboBox<>(new String[]{"PATIENT", "DOCTOR"});
        roleCombo.setFont(UIUtil.FONT_BODY);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(UIUtil.FONT_SMALL);
        statusLabel.setForeground(UIUtil.DANGER);

        JButton registerBtn = UIUtil.primaryButton("Create Account");
        registerBtn.setPreferredSize(new Dimension(0, 40));

        JButton backBtn = new JButton("← Back to Login");
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setForeground(UIUtil.PRIMARY);
        backBtn.setFont(UIUtil.FONT_BODY);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Object[][] rows = {
            {"Full Name *", fullNameField},
            {"Username * (4-20 chars)", usernameField},
            {"Password * (min 6)", passwordField},
            {"Confirm Password *", confirmPassField},
            {"Email", emailField},
            {"Phone (10 digits)", phoneField},
            {"Role *", roleCombo}
        };

        int row = 0;
        for (Object[] r : rows) {
            c.gridy = row++;
            form.add(fieldLabel((String) r[0]), c);
            c.gridy = row++;
            form.add((Component) r[1], c);
        }
        c.gridy = row++; c.insets = new Insets(14, 0, 4, 0);
        form.add(registerBtn, c);
        c.gridy = row++;  c.insets = new Insets(4, 0, 0, 0);
        form.add(backBtn, c);
        c.gridy = row;
        form.add(statusLabel, c);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        registerBtn.addActionListener(e -> doRegister());
        backBtn.addActionListener(e -> { dispose(); new LoginView().setVisible(true); });

        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIUtil.FONT_LABEL);
        lbl.setForeground(UIUtil.TEXT_MUTED);
        return lbl;
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmPassField.getPassword());
        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();
        String role     = (String) roleCombo.getSelectedItem();

        if (!password.equals(confirm)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }

        String error = authController.register(username, password, fullName, email, phone, role);
        if (error != null) {
            statusLabel.setText(error);
        } else {
            JOptionPane.showMessageDialog(this,
                "Account created successfully! Please log in.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginView().setVisible(true);
        }
    }
}
