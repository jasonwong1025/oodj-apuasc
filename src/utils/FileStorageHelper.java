package utils;

import java.io.*;
import java.nio.file.*;
import java.util.Base64;
import java.util.List;

/**
 * Helper for robust file storage operations.
 * Handles atomic writes and Base64 field encoding for absolute delimiter/newline safety.
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
     * Encodes a field using Base64 to ensure it contains no delimiters or newlines.
     * This provides absolute collision-safety for any character sequence.
     */
    public static String escape(Object input) {
        if (input == null) return "";
        byte[] bytes = String.valueOf(input).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Decodes a Base64-encoded field.
     */
    public static String unescape(String input) {
        if (input == null || input.isEmpty()) return "";
        try {
            byte[] decoded = Base64.getDecoder().decode(input);
            return new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // Fallback for legacy plaintext data during migration
            return input;
        }
    }

    /**
     * Joins parts using the delimiter after encoding each part in Base64.
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
