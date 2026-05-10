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

    private FileStorageHelper() {}

    /**
     * Writes content to a file atomically by writing to a temporary file first.
     */
    public static void writeAtomic(String filePath, List<String> lines) throws IOException {
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

    /**
     * Appends a line to a file. (Not atomic, but handles directory creation).
     */
    public static void appendLine(String filePath, String line) throws IOException {
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

    /**
     * Escapes characters that would break the pipe-delimited storage format.
     * Order of replacement is critical: backslash must be escaped first.
     */
    public static String escape(Object input) {
        if (input == null) return "";
        String str = String.valueOf(input);
        return str.replace("\\", "\\\\") // Escape backslash first
                  .replace("|", "\\p")   // Escape pipe
                  .replace("\n", "\\n")  // Escape newline
                  .replace("\r", "");
    }

    /**
     * Unescapes characters from the storage format.
     * Order of replacement is critical: backslash must be unescaped last.
     */
    public static String unescape(String input) {
        if (input == null) return "";
        return input.replace("\\n", "\n")
                    .replace("\\p", "|")
                    .replace("\\\\", "\\");
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
