package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple centralized logger for the application.
 */
public class Logger {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void error(String context, String message, Throwable t) {
        String timestamp = LocalDateTime.now().format(dtf);
        System.err.printf("[%s] [ERROR] %s: %s%n", timestamp, context, message);
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    public static void info(String message) {
        String timestamp = LocalDateTime.now().format(dtf);
        System.out.printf("[%s] [INFO] %s%n", timestamp, message);
    }
}
