package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.service.BackupService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.prefs.Preferences;
import java.util.function.IntConsumer;

public class SettingsPanel extends JPanel {

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(SettingsPanel.class);

    private static final String AUTO_LOCK_KEY = "autoLockMinutes";
    private static final int DEFAULT_AUTO_LOCK = 5;

    private final Database database;
    private final Runnable lockAction;
    private final IntConsumer autoLockChange;

    private JComboBox<String> autoLockCombo;

    public SettingsPanel(
            Database database,
            Runnable lockAction,
            IntConsumer autoLockChange
    ) {

        if (database == null) {
            throw new IllegalArgumentException(
                    "Database is required."
            );
        }

        if (lockAction == null) {
            throw new IllegalArgumentException(
                    "Lock action is required."
            );
        }

        this.database = database;
        this.lockAction = lockAction;
        this.autoLockChange = autoLockChange;

        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        createHeader();
        createContent();
    }

    // =========================================================
    // HEADER
    // =========================================================

    private void createHeader() {

        JPanel header = new JPanel();

        header.setLayout(new BoxLayout(
                header,
                BoxLayout.Y_AXIS
        ));

        header.setBackground(Theme.BG);

        JLabel title = new JLabel("Settings");

        title.setFont(new Font(
                "Segoe UI",
                Font.BOLD,
                28
        ));

        title.setForeground(Theme.LIGHT);

        JLabel subtitle = new JLabel(
                "Manage your SecureVault application and security."
        );

        subtitle.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                13
        ));

        subtitle.setForeground(Theme.MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);
    }

    // =========================================================
    // MAIN CONTENT
    // =========================================================

    private void createContent() {

        JPanel content = new JPanel();

        content.setLayout(new BoxLayout(
                content,
                BoxLayout.Y_AXIS
        ));

        content.setBackground(Theme.BG);

        // APPLICATION INFORMATION

        JPanel appCard = createCard();

        appCard.add(createSectionTitle(
                "Application Information"
        ));

        appCard.add(Box.createVerticalStrut(18));

        appCard.add(createInfoRow(
                "Application",
                "SecureVault"
        ));

        appCard.add(createInfoRow(
                "Version",
                "1.0.0"
        ));

        appCard.add(createInfoRow(
                "Language",
                "Java 25"
        ));

        appCard.add(createInfoRow(
                "Interface",
                "Java Swing"
        ));

        appCard.add(createInfoRow(
                "Database",
                "SQLite"
        ));

        content.add(appCard);
        content.add(Box.createVerticalStrut(18));

        // SECURITY INFORMATION

        JPanel securityCard = createCard();

        securityCard.add(createSectionTitle(
                "Security Information"
        ));

        securityCard.add(Box.createVerticalStrut(18));

        securityCard.add(createInfoRow(
                "Password Hashing",
                "PBKDF2-HMAC-SHA256"
        ));

        securityCard.add(createInfoRow(
                "Credential Encryption",
                "AES-256-GCM"
        ));

        securityCard.add(createInfoRow(
                "Encryption Key",
                "Derived from master password"
        ));

        securityCard.add(createInfoRow(
                "Storage",
                "Local SQLite database"
        ));

        content.add(securityCard);
        content.add(Box.createVerticalStrut(18));

        // AUTOMATIC LOCK SETTINGS

        JPanel autoLockCard = createCard();

        autoLockCard.add(createSectionTitle(
                "Automatic Lock"
        ));

        autoLockCard.add(Box.createVerticalStrut(12));

        JLabel autoLockDescription = new JLabel(
                "<html>Choose how long SecureVault can remain inactive " +
                        "before automatically locking your vault.</html>"
        );

        autoLockDescription.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                12
        ));

        autoLockDescription.setForeground(Theme.MUTED);
        autoLockDescription.setAlignmentX(Component.LEFT_ALIGNMENT);

        autoLockCard.add(autoLockDescription);
        autoLockCard.add(Box.createVerticalStrut(15));

        autoLockCombo = new JComboBox<>(new String[]{
                "Never",
                "1 minute",
                "5 minutes",
                "10 minutes",
                "15 minutes",
                "30 minutes"
        });

        autoLockCombo.setMaximumSize(
                new Dimension(250, 38)
        );

        autoLockCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        int savedMinutes = PREFS.getInt(
                AUTO_LOCK_KEY,
                DEFAULT_AUTO_LOCK
        );

        autoLockCombo.setSelectedItem(
                getAutoLockLabel(savedMinutes)
        );

        autoLockCombo.addActionListener(e -> {

            int minutes = getSelectedAutoLockMinutes();

            PREFS.putInt(
                    AUTO_LOCK_KEY,
                    minutes
            );

            if (this.autoLockChange != null) {
                this.autoLockChange.accept(minutes);
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Automatic lock setting updated to "
                            + getAutoLockLabel(minutes) + ".",
                    "Settings Updated",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        autoLockCard.add(autoLockCombo);
        autoLockCard.add(Box.createVerticalStrut(10));

        JLabel autoLockNote = new JLabel(
                "Your selection will be saved automatically."
        );

        autoLockNote.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                11
        ));

        autoLockNote.setForeground(Theme.MUTED);
        autoLockNote.setAlignmentX(Component.LEFT_ALIGNMENT);

        autoLockCard.add(autoLockNote);

        content.add(autoLockCard);
        content.add(Box.createVerticalStrut(18));

        // BACKUP AND RESTORE

        JPanel backupCard = createCard();

        backupCard.add(createSectionTitle(
                "Backup and Restore"
        ));

        backupCard.add(Box.createVerticalStrut(10));

        JLabel backupDescription = new JLabel(
                "<html>Create an encrypted backup of your SecureVault database. " +
                        "Your backup will require a separate password. " +
                        "You can also restore a previously created backup.</html>"
        );

        backupDescription.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                12
        ));

        backupDescription.setForeground(Theme.MUTED);
        backupDescription.setAlignmentX(Component.LEFT_ALIGNMENT);

        backupCard.add(backupDescription);
        backupCard.add(Box.createVerticalStrut(16));

        JButton backupButton = Theme.button(
                "Create Encrypted Backup"
        );

        backupButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        backupButton.addActionListener(e -> createBackup());

        backupCard.add(backupButton);
        backupCard.add(Box.createVerticalStrut(10));

        JButton restoreButton = Theme.button(
                "Restore Encrypted Backup"
        );

        restoreButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        restoreButton.addActionListener(e -> restoreBackup());

        backupCard.add(restoreButton);

        JLabel backupNote = new JLabel(
                "Backup files use the .svb extension. " +
                        "Keep your backup password safe."
        );

        backupNote.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                11
        ));

        backupNote.setForeground(Theme.MUTED);
        backupNote.setAlignmentX(Component.LEFT_ALIGNMENT);

        backupCard.add(Box.createVerticalStrut(12));
        backupCard.add(backupNote);

        content.add(backupCard);
        content.add(Box.createVerticalStrut(18));

        // VAULT CONTROLS

        JPanel vaultCard = createCard();

        vaultCard.add(createSectionTitle(
                "Vault Controls"
        ));

        vaultCard.add(Box.createVerticalStrut(10));

        JLabel description = new JLabel(
                "Lock your vault to end the current session."
        );

        description.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                12
        ));

        description.setForeground(Theme.MUTED);

        vaultCard.add(description);
        vaultCard.add(Box.createVerticalStrut(18));

        JButton lockButton = Theme.button(
                "Lock SecureVault"
        );

        lockButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        lockButton.addActionListener(e -> lockVault());

        vaultCard.add(lockButton);

        content.add(vaultCard);
        content.add(Box.createVerticalStrut(18));

        // ABOUT SECTION

        JPanel aboutCard = createCard();

        aboutCard.add(createSectionTitle(
                "About SecureVault"
        ));

        aboutCard.add(Box.createVerticalStrut(12));

        JLabel aboutText = new JLabel(
                "<html>SecureVault is a Java desktop password manager " +
                        "designed to organize and protect stored credentials. " +
                        "It includes credential management, password generation, " +
                        "security analysis, categories, and activity history.</html>"
        );

        aboutText.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                13
        ));

        aboutText.setForeground(Theme.MUTED);

        aboutCard.add(aboutText);

        content.add(aboutCard);

        // SCROLL PANE

        JScrollPane scrollPane = new JScrollPane(content);

        scrollPane.setBorder(null);

        scrollPane.getViewport().setBackground(Theme.BG);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    // =========================================================
    // CREATE ENCRYPTED BACKUP
    // =========================================================

    private void createBackup() {

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle(
                "Create SecureVault Backup"
        );

        chooser.setFileFilter(
                new FileNameExtensionFilter(
                        "SecureVault Backup (*.svb)",
                        "svb"
                )
        );

        chooser.setSelectedFile(
                new File("SecureVault-Backup.svb")
        );

        int fileChoice = chooser.showSaveDialog(this);

        if (fileChoice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path destination = chooser.getSelectedFile().toPath();

        String filename = destination.getFileName().toString();

        if (!filename.toLowerCase().endsWith(".svb")) {

            destination = destination.resolveSibling(
                    filename + ".svb"
            );
        }

        if (Files.exists(destination)) {

            JOptionPane.showMessageDialog(
                    this,
                    "A file already exists at this location.\n" +
                            "Choose another filename.",
                    "File Already Exists",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);

        JPanel passwordPanel = new JPanel(
                new GridLayout(0, 1, 5, 5)
        );

        passwordPanel.add(new JLabel(
                "Create a backup password:"
        ));

        passwordPanel.add(passwordField);

        passwordPanel.add(new JLabel(
                "Confirm your backup password:"
        ));

        passwordPanel.add(confirmField);

        passwordPanel.add(new JLabel(
                "Use at least 12 characters. Keep this password safe."
        ));

        int passwordChoice = JOptionPane.showConfirmDialog(
                this,
                passwordPanel,
                "Protect Your Backup",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (passwordChoice != JOptionPane.OK_OPTION) {

            passwordField.setText("");
            confirmField.setText("");

            return;
        }

        char[] password = passwordField.getPassword();
        char[] confirmation = confirmField.getPassword();

        try {

            if (password.length < 12) {

                JOptionPane.showMessageDialog(
                        this,
                        "Your backup password must contain at least " +
                                "12 characters.",
                        "Password Too Short",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if (!Arrays.equals(password, confirmation)) {

                JOptionPane.showMessageDialog(
                        this,
                        "The backup passwords do not match.",
                        "Password Mismatch",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            BackupService.createBackup(
                    database,
                    destination,
                    password
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Your encrypted backup was created successfully.\n\n" +
                            "Location:\n" + destination.toAbsolutePath() +
                            "\n\nKeep your backup password safe. " +
                            "It will be needed to restore this backup.",
                    "Backup Created",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not create the backup.\n\n" +
                            getErrorMessage(e),
                    "Backup Failed",
                    JOptionPane.ERROR_MESSAGE
            );

        } finally {

            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');

            passwordField.setText("");
            confirmField.setText("");
        }
    }

    // =========================================================
    // RESTORE ENCRYPTED BACKUP
    // =========================================================

    private void restoreBackup() {

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle(
                "Select SecureVault Backup"
        );

        chooser.setFileFilter(
                new FileNameExtensionFilter(
                        "SecureVault Backup (*.svb)",
                        "svb"
                )
        );

        int fileChoice = chooser.showOpenDialog(this);

        if (fileChoice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path backupFile = chooser.getSelectedFile().toPath();

        // Validate the backup container first.

        try {

            BackupService.validateBackupFile(backupFile);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "The selected backup file is invalid or damaged.\n\n" +
                            getErrorMessage(e),
                    "Invalid Backup",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        // Request the backup password.

        JPasswordField passwordField = new JPasswordField(20);

        JPanel passwordPanel = new JPanel(
                new GridLayout(0, 1, 5, 5)
        );

        passwordPanel.add(new JLabel(
                "Enter the password used to protect this backup:"
        ));

        passwordPanel.add(passwordField);

        int passwordChoice = JOptionPane.showConfirmDialog(
                this,
                passwordPanel,
                "Unlock Encrypted Backup",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (passwordChoice != JOptionPane.OK_OPTION) {

            passwordField.setText("");

            return;
        }

        char[] backupPassword = passwordField.getPassword();

        try {

            if (backupPassword.length < 12) {

                JOptionPane.showMessageDialog(
                        this,
                        "The backup password must contain at least " +
                                "12 characters.",
                        "Invalid Password",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            // Confirm database replacement.

            int confirmation = JOptionPane.showConfirmDialog(
                    this,
                    "Restoring this backup will replace your current " +
                            "SecureVault database.\n\n" +
                            "Any changes made after this backup was created " +
                            "may be lost.\n\n" +
                            "Make sure you have a separate backup of your " +
                            "current database before continuing.\n\n" +
                            "Do you want to continue?",
                    "Confirm Database Restoration",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }

            // Restore the database.

            BackupService.restoreBackup(
                    database,
                    backupFile,
                    backupPassword
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Your SecureVault database was restored successfully.\n\n" +
                            "You will now return to the login screen. " +
                            "Sign in using the master password associated " +
                            "with the restored backup.",
                    "Backup Restored",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Return to login and require authentication again.
            // The lock action is responsible for ending the current
            // application session and handling its database lifecycle.

            try {

                lockAction.run();

            } catch (Exception e) {

                JOptionPane.showMessageDialog(
                        null,
                        "The backup was restored, but SecureVault " +
                                "could not automatically return to the " +
                                "login screen.\n\n" +
                                getErrorMessage(e) +
                                "\n\nPlease close and reopen SecureVault.",
                        "Login Transition Error",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not restore the backup.\n\n" +
                            getErrorMessage(e),
                    "Restore Failed",
                    JOptionPane.ERROR_MESSAGE
            );

        } finally {

            Arrays.fill(backupPassword, '\0');

            passwordField.setText("");
        }
    }

    // =========================================================
    // AUTOMATIC LOCK HELPERS
    // =========================================================

    private int getSelectedAutoLockMinutes() {

        String selected =
                (String) autoLockCombo.getSelectedItem();

        if (selected == null || selected.equals("Never")) {
            return 0;
        }

        return Integer.parseInt(
                selected.replace(" minutes", "")
                        .replace(" minute", "")
        );
    }

    private String getAutoLockLabel(int minutes) {

        if (minutes == 0) {
            return "Never";
        }

        return minutes == 1
                ? "1 minute"
                : minutes + " minutes";
    }

    // =========================================================
    // ERROR MESSAGE HELPER
    // =========================================================

    private String getErrorMessage(Exception e) {

        if (e == null) {
            return "An unknown error occurred.";
        }

        String message = e.getMessage();

        if (message == null || message.isBlank()) {
            return e.getClass().getSimpleName();
        }

        return message;
    }

    // =========================================================
    // CREATE CARD
    // =========================================================

    private JPanel createCard() {

        JPanel card = Theme.panel();

        card.setLayout(new BoxLayout(
                card,
                BoxLayout.Y_AXIS
        ));

        card.setBorder(new EmptyBorder(
                22,
                22,
                22,
                22
        ));

        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        return card;
    }

    // =========================================================
    // CREATE SECTION TITLE
    // =========================================================

    private JLabel createSectionTitle(String text) {

        JLabel label = new JLabel(text);

        label.setFont(new Font(
                "Segoe UI",
                Font.BOLD,
                17
        ));

        label.setForeground(Theme.LIGHT);

        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    // =========================================================
    // CREATE INFORMATION ROW
    // =========================================================

    private JPanel createInfoRow(
            String label,
            String value
    ) {

        JPanel row = new JPanel(
                new BorderLayout(15, 0)
        );

        row.setBackground(Theme.PANEL);

        row.setBorder(new EmptyBorder(
                8,
                0,
                8,
                0
        ));

        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38)
        );

        JLabel nameLabel = new JLabel(label);

        nameLabel.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                13
        ));

        nameLabel.setForeground(Theme.MUTED);

        JLabel valueLabel = new JLabel(value);

        valueLabel.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                13
        ));

        valueLabel.setForeground(Theme.LIGHT);

        row.add(nameLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);

        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        return row;
    }

    // =========================================================
    // LOCK VAULT
    // =========================================================

    private void lockVault() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to lock SecureVault?",
                "Lock Vault",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            database.close();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "There was a problem closing the database.\n\n" +
                            getErrorMessage(e),
                    "Lock Warning",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        Window window =
                SwingUtilities.getWindowAncestor(this);

        if (window != null) {
            window.dispose();
        }

        lockAction.run();
    }
}
