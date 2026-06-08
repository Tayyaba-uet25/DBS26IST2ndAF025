package hms.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central database connection manager. Reads settings from db.properties
 * (in the working directory) and falls back to sensible defaults.
 * SOFTWARE CLASS #1
 */
public final class DatabaseConnection {

    private static String host = "localhost";
    private static String port = "3306";
    private static String name = "hms";
    private static String user = "hms";
    private static String password = "hms123";

    static {
        loadProperties();
        try {
            // Make sure the MySQL JDBC driver is registered.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            AppLogger.error("MySQL JDBC driver not found on classpath", e);
        }
    }

    private DatabaseConnection() { }

    private static void loadProperties() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("db.properties")) {
            p.load(in);
            host = p.getProperty("db.host", host);
            port = p.getProperty("db.port", port);
            name = p.getProperty("db.name", name);
            user = p.getProperty("db.user", user);
            password = p.getProperty("db.password", password);
        } catch (Exception e) {
            // file missing -> use defaults (logged once)
            System.out.println("[INFO] db.properties not found, using default DB settings.");
        }
    }

    public static String url() {
        return "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false&allowPublicKeyRetrieval=true"
                + "&serverTimezone=Asia/Karachi&characterEncoding=UTF-8";
    }

    /** Returns a brand new connection. Caller is responsible for closing it. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url(), user, password);
    }

    /** Quick connectivity test used at start-up. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            AppLogger.error("Database connection test failed", e);
            return false;
        }
    }
}
