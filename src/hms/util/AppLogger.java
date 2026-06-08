package hms.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple file logger. Writes INFO / ERROR lines to logs/app.log.
 * Satisfies the "Logging in case of errors" requirement.
 * SOFTWARE CLASS #2
 */
public final class AppLogger {

    private static final Path LOG_DIR = Paths.get("logs");
    private static final Path LOG_FILE = LOG_DIR.resolve("app.log");
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() { }

    public static void info(String message) {
        write("INFO", message);
    }

    public static void error(String message) {
        write("ERROR", message);
    }

    public static void error(String message, Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        write("ERROR", message + System.lineSeparator() + sw);
    }

    private static synchronized void write(String level, String message) {
        String line = "[" + LocalDateTime.now().format(TS) + "] " + level + " - " + message
                + System.lineSeparator();
        try {
            if (!Files.exists(LOG_DIR)) {
                Files.createDirectories(LOG_DIR);
            }
            Files.writeString(LOG_FILE, line,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            // last resort - never let logging crash the app
            System.err.println("Logging failed: " + e.getMessage());
        }
        if ("ERROR".equals(level)) {
            System.err.println(line.trim());
        }
    }
}
