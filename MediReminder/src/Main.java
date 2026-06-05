import util.UIUtil;
import view.LoginView;

import javax.swing.*;

/**
 * Main - entry point for the Remote Medicine Reminder & Monitoring System.
 *
 * Run this class to launch the application.
 * Prerequisites:
 *   1. MySQL running with the schema from sql/schema.sql imported.
 *   2. DBConnection.java configured with your MySQL credentials.
 *   3. mysql-connector-java-8.x.x.jar in classpath.
 */
public class Main {

    public static void main(String[] args) {
        // Apply system look-and-feel for native widgets
        UIUtil.setupLookAndFeel();

        // Launch on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}
    