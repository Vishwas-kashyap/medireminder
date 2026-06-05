package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;

/**
 * DBInitializer - Helper to initialize the SQLite database from a SQL file.
 */
public class DBInitializer {
    public static void main(String[] args) {
        String schemaPath = "sql/sqlite_schema.sql";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("[DB] Initializing database from " + schemaPath + "...");
            
            BufferedReader reader = new BufferedReader(new FileReader(schemaPath));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.isEmpty()) continue;
                sb.append(line).append(" ");
                if (trimmed.endsWith(";")) {
                    stmt.execute(sb.toString());
                    sb.setLength(0);
                }
            }
            reader.close();
            
            System.out.println("[DB] Database initialized successfully.");
            
        } catch (Exception e) {
            System.err.println("[DB] Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
