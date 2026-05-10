package ui.ManagerPortal;

import service_layer.BackupService;
import ui.core.Refreshable;
import ui.shared.SharedStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SystemMaintenanceTabPanel extends JPanel implements Refreshable {

    private static final Color STATUS_OK_COLOR = new Color(46, 160, 67);
    private static final Color STATUS_BUSY_COLOR = Color.DARK_GRAY;

    private final BackupService backupService;
    private final Runnable onLogout;
    private JLabel statusLabel;

    public SystemMaintenanceTabPanel(Runnable onLogout) {
        this.backupService = new BackupService();
        this.onLogout = onLogout;
        setLayout(new BorderLayout(0, 20));
        setBackground(SharedStyles.MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));
        buildUi();
    }

    // -------------------------------------------------------------------------
    // UI Construction
    // -------------------------------------------------------------------------

    private void buildUi() {
        add(buildHeadingCard(), BorderLayout.NORTH);

        JPanel actionsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        actionsRow.setOpaque(false);
        actionsRow.add(buildBackupCard());
        actionsRow.add(buildRestoreCard());

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(actionsRow, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildHeadingCard() {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel heading = new JLabel("System Maintenance");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        card.add(heading, BorderLayout.NORTH);

        statusLabel = new JLabel("System Database: Ready.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(STATUS_OK_COLOR);
        card.add(statusLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildBackupCard() {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 16));

        JLabel title = new JLabel("Create Backup");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        card.add(title, BorderLayout.NORTH);

        JTextArea desc = new JTextArea(
                "Take a complete snapshot of all current database files: appointments, users, " +
                "services, vehicles, payments, categories, feedbacks, and reviews.\n\n" +
                "Each backup is saved in a timestamped folder under the 'backups/' directory " +
                "and can be restored at any time."
        );
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(new Color(80, 80, 90));
        card.add(desc, BorderLayout.CENTER);

        JButton backupBtn = SharedStyles.createActionButton("Create Backup Now", SharedStyles.BTN_GREEN);
        backupBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        backupBtn.addActionListener(e -> onCreateBackup());

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(backupBtn);
        card.add(btnWrap, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildRestoreCard() {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 16));

        JLabel title = new JLabel("Restore from Backup");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(180, 60, 30));
        card.add(title, BorderLayout.NORTH);

        JTextArea desc = new JTextArea(
                "Overwrite the current database with files from a previously created backup folder.\n\n" +
                "WARNING: This operation is irreversible. All live data will be replaced by " +
                "the selected backup. The system will force a logout immediately after so the " +
                "restored data loads cleanly on next login."
        );
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(new Color(80, 80, 90));
        card.add(desc, BorderLayout.CENTER);

        JButton restoreBtn = SharedStyles.createActionButton("Restore from Backup", SharedStyles.BTN_ORANGE);
        restoreBtn.addActionListener(e -> onRestoreBackup());

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(restoreBtn);
        card.add(btnWrap, BorderLayout.SOUTH);

        return card;
    }

    // -------------------------------------------------------------------------
    // Button handlers — UI decisions only, delegate work to BackupService
    // -------------------------------------------------------------------------

    private void onCreateBackup() {
        setStatus("Creating backup...", STATUS_BUSY_COLOR);
        try {
            String folderName = backupService.createBackup();
            setStatus("System Database: Ready.", STATUS_OK_COLOR);
            SharedStyles.showMessage(this,
                    "Backup completed successfully.\n" +
                    "Saved to: " + folderName + "\n" +
                    "(" + backupService.countDataFiles() + " file(s) backed up)");
        } catch (IOException | IllegalStateException ex) {
            setStatus("System Database: Ready.", STATUS_OK_COLOR);
            SharedStyles.showError(this, "Backup failed:\n" + ex.getMessage());
        }
    }

    private void onRestoreBackup() {
        boolean confirmed = SharedStyles.showConfirm(this,
                "WARNING: This will overwrite all current system data.\n" +
                "Every live data file will be permanently replaced by the backup.\n\n" +
                "Are you sure you want to continue?");
        if (!confirmed) return;

        File selectedFolder = chooseBackupFolder();
        if (selectedFolder == null) return;

        setStatus("Restoring...", STATUS_BUSY_COLOR);
        try {
            backupService.restoreBackup(selectedFolder);
            SharedStyles.showMessage(this,
                    "Restore completed successfully.\n" +
                    "Data restored from: " + selectedFolder.getName() + "\n\n" +
                    "You will now be logged out. Please log back in to load the restored data.");
            SwingUtilities.invokeLater(onLogout);
        } catch (IOException | IllegalArgumentException ex) {
            setStatus("System Database: Ready.", STATUS_OK_COLOR);
            SharedStyles.showError(this, "Restore failed:\n" + ex.getMessage());
        }
    }

    private File chooseBackupFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Backup Folder to Restore");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setCurrentDirectory(backupService.getBackupsDirectory());

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return null;
        return chooser.getSelectedFile();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    @Override
    public void refresh() {
        setStatus("System Database: Ready.", STATUS_OK_COLOR);
    }
}
