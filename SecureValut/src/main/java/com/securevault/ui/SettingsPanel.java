package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.service.BackupService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class SettingsPanel extends JPanel {

    private final Database database;
    private final Runnable lockAction;

    public SettingsPanel(Database database, Runnable lockAction) {

        this.database = database;
        this.lockAction = lockAction;

        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        createHeader();
        createContent();
    }

    private void createHeader() {

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.BG);

        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Theme.LIGHT);

        JLabel subtitle = new JLabel(
                "Manage your SecureVault application and security."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);
    }

    private void createContent() {

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG);

        // Application Information
        JPanel appCard = createCard();

        appCard.add(createSectionTitle("Application Information"));
        appCard.add(Box.createVerticalStrut(18));

        appCard.add(createInfoRow("Application", "SecureVault"));
        appCard.add(createInfoRow("Version", "1.0.0"));
        appCard.add(createInfoRow("Language", "Java 25"));
        appCard.add(createInfoRow("Interface", "Java Swing"));
        appCard.add(createInfoRow("Database", "SQLite"));

        content.add(appCard);
        content.add(Box.createVerticalStrut(18));

        // Security Information
        JPanel securityCard = createCard();

        securityCard.add(createSectionTitle("Security Information"));
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

        securityCard.add(createInfoRow(
                "Automatic Lock",
                "After 5 minutes of inactivity"
        ));

        content.add(securityCard);
        content.add(Box.createVerticalStrut(18));

        // Backup and Restore
        JPanel backupCard = createCard();

        backupCard.add(createSectionTitle("Backup and Restore"));
        backupCard.add(Box.createVerticalStrut(10));

        JLabel backupDescription = new JLabel(
                "<html>Create an encrypted backup of your SecureVault database. " +
                        "Your backup will require a separate password.</html>"
        );

        backupDescription.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        backupDescription.setForeground(Theme.MUTED);
        backupDescription.setAlignmentX(Component.LEFT_ALIGNMENT);

        backupCard.add(backupDescription);
        backupCard.add(Box.createVerticalStrut(16));

        JButton backupButton = Theme.button("Create Encrypted Backup");

        backupButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backupButton.addActionListener(e -> createBackup());

        backupCard.add(backupButton);

        JLabel backupNote = new JLabel(
                "Backup files use the .svb extension."
        );

        backupNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        backupNote.setForeground(Theme.MUTED);
        backupNote.setAlignmentX(Component.LEFT_ALIGNMENT);

        backupCard.add(Box.createVerticalStrut(10));
        backupCard.add(backupNote);

        content.add(backupCard);
        content.add(Box.createVerticalStrut(18));

        // Vault Controls
        JPanel vaultCard = createCard();

        vaultCard.add(createSectionTitle("Vault Controls"));
        vaultCard.add(Box.createVerticalStrut(10));

        JLabel description = new JLabel(
                "Lock your vault to end the current session."
        );

        description.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        description.setForeground(Theme.MUTED);

        vaultCard.add(description);
        vaultCard.add(Box.createVerticalStrut(18));

        JButton lockButton = Theme.button("Lock SecureVault");

        lockButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        lockButton.addActionListener(e -> lockVault());

        vaultCard.add(lockButton);

        content.add(vaultCard);
        content.add(Box.createVerticalStrut(18));

        // About Section
        JPanel aboutCard = createCard();

        aboutCard.add(createSectionTitle("About SecureVault"));
        aboutCard.add(Box.createVerticalStrut(12));

        JLabel aboutText = new JLabel(
                "<html>SecureVault is a Java desktop password manager " +
                        "designed to organize and protect stored credentials. " +
                        "It includes credential management, password generation, " +
                        "security analysis, categories, and activity history.</html>"
        );

        aboutText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        aboutText.setForeground(Theme.MUTED);

        aboutCard.add(aboutText);

        content.add(aboutCard);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void createBackup() {

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle("Create SecureVault Backup");
        chooser.setSelectedFile(
                new File("SecureVault-Backup.svb")
        );

        int fileChoice = chooser.showSaveDialog(this);

        if (fileChoice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path destination = chooser.getSelectedFile().toPath();

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

        JPanel passwordPanel = new JPanel(new GridLayout(0, 1, 5, 5));

        passwordPanel.add(new JLabel("Create a backup password:"));
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
                    e.getMessage(),
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

    private JPanel createCard() {

        JPanel card = Theme.panel();

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        return card;
    }

    private JLabel createSectionTitle(String text) {

        JLabel label = new JLabel(text);

        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(Theme.LIGHT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    private JPanel createInfoRow(String label, String value) {

        JPanel row = new JPanel(new BorderLayout(15, 0));

        row.setBackground(Theme.PANEL);
        row.setBorder(new EmptyBorder(8, 0, 8, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameLabel.setForeground(Theme.MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueLabel.setForeground(Theme.LIGHT);

        row.add(nameLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);

        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        return row;
    }

    private void lockVault() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to lock SecureVault?",
                "Lock Vault",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {

            try {
                database.close();
            } catch (Exception ignored) {
            }

            Window window = SwingUtilities.getWindowAncestor(this);

            if (window != null) {
                window.dispose();
            }

            lockAction.run();
        }
    }
}
