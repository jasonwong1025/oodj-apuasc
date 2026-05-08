package service_layer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles system backup and restore operations.
 * Copies .txt files from the data directory into timestamped backup folders,
 * and restores them back on demand.
 */
public class BackupService {

    private static final String DATA_DIR = "data";
    private static final String BACKUPS_DIR = "backups";
    private static final DateTimeFormatter BACKUP_TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Creates a timestamped backup of all .txt files in the data directory.
     *
     * @return the name of the created backup folder (e.g. "Backup_2026-05-08_14-30-00")
     * @throws IOException if the backup directory cannot be created or any file copy fails
     * @throws IllegalStateException if no .txt data files are found to back up
     */
    public String createBackup() throws IOException {
        String folderName = "Backup_" + LocalDateTime.now().format(BACKUP_TS_FMT);

        File backupDir = new File(BACKUPS_DIR, folderName);
        if (!backupDir.mkdirs()) {
            throw new IOException("Could not create backup directory: " + backupDir.getAbsolutePath());
        }

        File[] sources = listDataFiles();
        for (File src : sources) {
            Files.copy(src.toPath(), new File(backupDir, src.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        return folderName;
    }

    /**
     * Restores data files from the given backup folder into the data directory.
     *
     * @param backupFolder the backup folder selected by the user
     * @throws IOException           if any file copy fails
     * @throws IllegalArgumentException if the folder is null, not a directory, or contains no .txt files
     */
    public void restoreBackup(File backupFolder) throws IOException {
        File[] backupFiles = validateBackupFolder(backupFolder);

        File dataDir = new File(DATA_DIR);
        for (File src : backupFiles) {
            Files.copy(src.toPath(), new File(dataDir, src.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Returns the number of .txt files that would be included in a backup.
     * Useful for informational display in the UI before a backup is triggered.
     */
    public int countDataFiles() {
        return listDataFiles().length;
    }

    /**
     * Returns the root backups directory, creating it if it does not yet exist.
     * The UI uses this as the starting directory for the folder chooser.
     */
    public File getBackupsDirectory() {
        File dir = new File(BACKUPS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private File[] listDataFiles() {
        File dataDir = new File(DATA_DIR);
        File[] files = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null || files.length == 0) {
            throw new IllegalStateException("No .txt data files found in '" + DATA_DIR + "/'.");
        }
        return files;
    }

    private File[] validateBackupFolder(File folder) {
        if (folder == null || !folder.isDirectory()) {
            throw new IllegalArgumentException("Invalid backup folder selected.");
        }
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException(
                    "The selected folder contains no .txt data files: " + folder.getName());
        }
        return files;
    }
}
