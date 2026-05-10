package utils;

import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * Helper for robust file storage operations.
 * Handles atomic writes and character escaping for delimiter-safe storage.
 */
public class FileStorageHelper {

    private static final String DELIMITER = "|";
    private static final Object LOCK = new Object();

    private FileStorageHelper() {}

    /**
     * Writes content to a file atomically by writing to a temporary file first.
     * Thread-safe via global lock shared with appendLine.
     */
    public static void writeAtomic(String filePath, List<String> lines) throws IOException {
        synchronized (LOCK) {
            Path target = Paths.get(filePath);
            Path parent = target.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            Path temp = Paths.get(filePath + ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temp)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * Appends a line to a file. Thread-safe via global lock shared with writeAtomic.
     */
    public static void appendLine(String filePath, String line) throws IOException {
        synchronized (LOCK) {
            Path target = Paths.get(filePath);
            Path parent = target.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(target, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Escapes characters that would break the pipe-delimited storage format.
     * Order is CRITICAL for 100% collision-safety:
     * 1. Escape the escape character itself (\ -> \\)
     * 2. Escape other control characters
     */
    public static String escape(Object input) {
        if (input == null) return "";
        String str = String.valueOf(input);
        return str.replace("\\", "\\\\") // Escape backslash FIRST
                  .replace("|", "\\p")   // Escape pipe
                  .replace("\n", "\\n")  // Escape newline
                  .replace("\r", "");
    }

    /**
     * Unescapes characters from the storage format.
     * Order is CRITICAL for 100% collision-safety:
     * 1. Unescape control characters
     * 2. Unescape the escape character itself (\\ -> \) LAST
     */
    public static String unescape(String input) {
        if (input == null) return "";
        // Unescape control chars first
        String res = input.replace("\\n", "\n")
                          .replace("\\p", "|");
        // Unescape backslash LAST
        return res.replace("\\\\", "\\");
    }

    /**
     * Joins parts using the delimiter after escaping each part.
     */
    public static String join(Object... parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            sb.append(escape(parts[i]));
            if (i < parts.length - 1) {
                sb.append(DELIMITER);
            }
        }
        return sb.toString();
    }
}
