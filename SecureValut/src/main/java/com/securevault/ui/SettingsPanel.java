package com.securevault.ui;

import com.securevault.db.Database;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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

        content.add(securityCard);
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
