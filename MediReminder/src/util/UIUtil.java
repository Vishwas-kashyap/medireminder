package util;

import java.awt.*;

/**
 * UIUtil - centralised color palette and font constants for consistent UI styling.
 */
public class UIUtil {

    // Primary palette
    public static final Color PRIMARY      = new Color(0x1565C0);   // deep blue
    public static final Color PRIMARY_DARK = new Color(0x003c8f);
    public static final Color ACCENT       = new Color(0x00ACC1);   // teal accent
    public static final Color SUCCESS      = new Color(0x2E7D32);
    public static final Color WARNING      = new Color(0xF57C00);
    public static final Color DANGER       = new Color(0xC62828);
    public static final Color LIGHT_BG     = new Color(0xF5F7FA);
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color BORDER       = new Color(0xDDE1E7);
    public static final Color TEXT_PRIMARY = new Color(0x1A1A2E);
    public static final Color TEXT_MUTED   = new Color(0x6B7280);

    // Fonts
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD, 12);

    /** Creates a styled primary button. */
    public static javax.swing.JButton primaryButton(String text) {
        javax.swing.JButton btn = new javax.swing.JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Creates a styled danger/red button. */
    public static javax.swing.JButton dangerButton(String text) {
        javax.swing.JButton btn = primaryButton(text);
        btn.setBackground(DANGER);
        return btn;
    }

    /** Creates a styled success/green button. */
    public static javax.swing.JButton successButton(String text) {
        javax.swing.JButton btn = primaryButton(text);
        btn.setBackground(SUCCESS);
        return btn;
    }

    /** Creates a styled text field. */
    public static javax.swing.JTextField styledField(int cols) {
        javax.swing.JTextField tf = new javax.swing.JTextField(cols);
        tf.setFont(FONT_BODY);
        tf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER, 1),
            javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return tf;
    }

    /** Creates a styled password field. */
    public static javax.swing.JPasswordField styledPasswordField(int cols) {
        javax.swing.JPasswordField pf = new javax.swing.JPasswordField(cols);
        pf.setFont(FONT_BODY);
        pf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER, 1),
            javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return pf;
    }

    /** Applies global Look and Feel settings. */
    public static void setupLookAndFeel() {
        try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        javax.swing.UIManager.put("Button.arc", 8);
        javax.swing.UIManager.put("Component.arc", 8);
    }
}
