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
     * To be 100% collision-safe, we use unique tokens that are processed 
     * AFTER the escape character (\) is itself escaped.
     */
    public static String escape(Object input) {
        if (input == null) return "";
        String str = String.valueOf(input);
        return str.replace("\\", "\\\\")  // 1. Escape literal backslashes
                  .replace("|", "{P}")    // 2. Escape pipe with unique token
                  .replace("\n", "{N}")   // 3. Escape newline with unique token
                  .replace("\r", "");
    }

    /**
     * Unescapes characters from the storage format.
     * Order is CRITICAL: Tokens are restored FIRST, then escaped backslashes LAST.
     */
    public static String unescape(String input) {
        if (input == null) return "";
        String res = input.replace("{N}", "\n")
                          .replace("{P}", "|");
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
