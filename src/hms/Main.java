package hms;

import hms.ui.LoginFrame;
import hms.ui.UiUtils;
import hms.util.AppLogger;
import hms.util.DatabaseConnection;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Application entry point. */
public class Main {

    public static void main(String[] args) {
        AppLogger.info("Application starting");
        UiUtils.applyLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            if (!DatabaseConnection.testConnection()) {
                JOptionPane.showMessageDialog(null,
                        "Cannot connect to the database.\n\n"
                      + "Please make sure the MySQL server is running.\n"
                      + "Tip: run start-database.bat first, then launch the app.\n\n"
                      + "Connection: " + DatabaseConnection.url(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                AppLogger.error("Startup aborted - no database connection");
                System.exit(1);
            }
            new LoginFrame().setVisible(true);
        });
    }
}
