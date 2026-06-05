package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Singleton JDBC connection manager.
 * Provides a single shared connection to the MySQL database.
 */
public class DBConnection {

    private static final String URL      = "jdbc:sqlite:medi_reminder.db";
    private static final String USER     = "";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    /** Private constructor – use getConnection() */
    private DBConnection() {}

    /**
     * Returns the singleton Connection, creating it if needed.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                System.out.println("[DB] Connected to SQLite successfully.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC Driver not found. Add sqlite-jdbc.jar to classpath.", e);
            }
        }
        return connection;
    }

    /** Closes the connection safely. */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}
