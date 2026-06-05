package view;

import controller.AuthController;
import model.User;
import model.Patient;
import database.PatientDAO;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * LoginView - the application entry screen.
 */
public class LoginView extends JFrame {

    private final AuthController authController = new AuthController();
    private final PatientDAO     patientDAO     = new PatientDAO();

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         statusLabel;

    public LoginView() {
        setTitle("MediReminder – Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        // Main panel with gradient-like background
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, UIUtil.PRIMARY, 0, getHeight(), UIUtil.PRIMARY_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Top branding panel
        JPanel brandPanel = new JPanel(new GridBagLayout());
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(40, 20, 20, 20));

        JLabel iconLabel = new JLabel("💊", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        JLabel titleLabel = new JLabel("MediReminder", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        JLabel subLabel = new JLabel("Remote Medicine Monitoring System", SwingConstants.CENTER);
        subLabel.setFont(UIUtil.FONT_SMALL);
        subLabel.setForeground(new Color(0xBBDEFB));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        brandPanel.add(iconLabel, gbc);
        gbc.gridy = 1; brandPanel.add(titleLabel, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(4,0,0,0); brandPanel.add(subLabel, gbc);

        // Card panel for the form
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIUtil.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 24, 32, 24),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.BORDER, 1, true),
                BorderFactory.createEmptyBorder(28, 28, 28, 28)
            )
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 0, 6, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.weightx = 1;

        JLabel loginTitle = new JLabel("Sign In");
        loginTitle.setFont(UIUtil.FONT_HEADING);
        loginTitle.setForeground(UIUtil.TEXT_PRIMARY);

        usernameField = UIUtil.styledField(20);
        usernameField.putClientProperty("JTextField.placeholderText", "Username");
        passwordField = UIUtil.styledPasswordField(20);
        passwordField.putClientProperty("JTextField.placeholderText", "Password");

        JButton loginBtn = UIUtil.primaryButton("Login");
        loginBtn.setPreferredSize(new Dimension(0, 40));

        JButton registerBtn = new JButton("Don't have an account? Register");
        registerBtn.setBorderPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setForeground(UIUtil.PRIMARY);
        registerBtn.setFont(UIUtil.FONT_BODY);
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(UIUtil.FONT_SMALL);
        statusLabel.setForeground(UIUtil.DANGER);

        c.gridy = 0; card.add(loginTitle, c);
        c.gridy = 1; card.add(fieldLabel("Username"), c);
        c.gridy = 2; card.add(usernameField, c);
        c.gridy = 3; card.add(fieldLabel("Password"), c);
        c.gridy = 4; card.add(passwordField, c);
        c.gridy = 5; c.insets = new Insets(14, 0, 4, 0); card.add(loginBtn, c);
        c.gridy = 6; c.insets = new Insets(4, 0, 0, 0); card.add(registerBtn, c);
        c.gridy = 7; card.add(statusLabel, c);

        // Actions
        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> openRegister());

        root.add(brandPanel, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIUtil.FONT_LABEL);
        lbl.setForeground(UIUtil.TEXT_MUTED);
        return lbl;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        try {
            User user = authController.login(username, password);
            if (user == null) {
                statusLabel.setText("Invalid username or password.");
                return;
            }

            dispose(); // close login window

            if (user.getRole() == User.Role.DOCTOR) {
                new DoctorDashboard(user).setVisible(true);
            } else {
                // Get patient record
                Patient patient = patientDAO.getPatientByUserId(user.getId());
                if (patient == null) {
                    JOptionPane.showMessageDialog(this,
                        "Your account exists but no doctor has added you as a patient yet.\n" +
                        "Please ask your doctor to add you to their patient list.",
                        "No Patient Record", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                new PatientDashboard(user, patient).setVisible(true);
            }

        } catch (SQLException ex) {
            statusLabel.setText("Database error. Please try again.");
            ex.printStackTrace();
        }
    }

    private void openRegister() {
        dispose();
        new RegisterView().setVisible(true);
    }
}
