package utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 * Utility for platform-agnostic file operations.
 */
public class FileUtil {

    /**
     * Attempts to open a file using the system's default application.
     * Provides fallbacks for Linux environments where Desktop API might fail.
     *
     * @param file The file to open.
     * @throws IOException if the file cannot be opened.
     */
    public static void openFile(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist: " + (file == null ? "null" : file.getAbsolutePath()));
        }

        // Try Desktop API first
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(file.toURI());
                    return;
                } catch (Exception e) {
                    // Fall back if browse fails
                }
            }
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    desktop.open(file);
                    return;
                } catch (Exception e) {
                    // Fall back
                }
            }
        }

        // Fallback for Linux/Unix
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux") || os.contains("unix")) {
            Runtime.getRuntime().exec(new String[]{"xdg-open", file.getAbsolutePath()});
        } else if (os.contains("mac")) {
            Runtime.getRuntime().exec(new String[]{"open", file.getAbsolutePath()});
        } else {
            throw new IOException("System Desktop API not supported and no fallback available for this OS.");
        }
    }

    /**
     * Convenience method to open a file from a string path.
     */
    public static void openFile(String path) throws IOException {
        openFile(new File(path));
    }
}
