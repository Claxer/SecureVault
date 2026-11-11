package com.securevault.ui;

import com.securevault.db.Database;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.function.BiConsumer;

public class LoginFrame extends JFrame {

    private final Database database;
    private final BiConsumer<Database, SecretKey> successAction;

    private final JPasswordField passwordField = new JPasswordField(22);
    private final JPasswordField confirmField = new JPasswordField(22);

    private final JLabel messageLabel = new JLabel(" ");

    public LoginFrame(
            Database database,
            BiConsumer<Database, SecretKey> successAction
    ) {
        this.database = database;
        this.successAction = successAction;

        setTitle("SecureVault");
        setSize(440, 460);
        setMinimumSize(new Dimension(400, 420));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        boolean firstSetup = !database.isInitialized();

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Theme.BG);
        root.setBorder(new EmptyBorder(30, 35, 30, 35));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.PANEL);
        content.setBorder(new EmptyBorder(28, 25, 28, 25));

        JLabel title = Theme.heading("SecureVault");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                firstSetup
                        ? "Create your master password"
                        : "Unlock your personal vault"
        );

        subtitle.setForeground(Theme.MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(28));

        addField(content, "Master Password", passwordField);

        if (firstSetup) {
            addField(content, "Confirm Master Password", confirmField);
        }

        content.add(Box.createVerticalStrut(12));

        messageLabel.setForeground(Theme.DANGER);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(messageLabel);

        content.add(Box.createVerticalStrut(12));

        JButton actionButton = Theme.button(
                firstSetup ? "Create Vault" : "Unlock Vault"
        );

        actionButton.setBackground(Theme.ACCENT);
        actionButton.setForeground(Color.WHITE);
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
        );

        actionButton.addActionListener(e -> authenticate(firstSetup));

        passwordField.addActionListener(e -> authenticate(firstSetup));
        confirmField.addActionListener(e -> authenticate(firstSetup));

        content.add(actionButton);

        root.add(content);
        setContentPane(root);
    }

    private void addField(
            JPanel panel,
            String labelText,
            JPasswordField field
    ) {
        JLabel label = new JLabel(labelText);
        label.setForeground(Theme.FG);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        Theme.style(field);

        field.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 40)
        );

        panel.add(label);
        panel.add(Box.createVerticalStrut(7));
        panel.add(field);
        panel.add(Box.createVerticalStrut(16));
    }

    private void authenticate(boolean firstSetup) {
        char[] password = passwordField.getPassword();
        char[] confirmation = confirmField.getPassword();

        try {
            if (password.length == 0) {
                messageLabel.setText("Enter your master password.");
                return;
            }

            SecretKey key;

            if (firstSetup) {
                if (password.length < 10) {
                    messageLabel.setText(
                            "Password must contain at least 10 characters."
                    );
                    return;
                }

                if (!Arrays.equals(password, confirmation)) {
                    messageLabel.setText(
                            "The passwords do not match."
                    );
                    return;
                }

                key = database.initializeMasterPassword(password);

            } else {
                key = database.unlockMasterPassword(password);
            }

            dispose();
            successAction.accept(database, key);

        } catch (SecurityException e) {
            messageLabel.setText("Incorrect master password.");

        } catch (Exception e) {
            messageLabel.setText(
                    e.getMessage() == null
                            ? "Unable to open the vault."
                            : e.getMessage()
            );

        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');

            passwordField.setText("");
            confirmField.setText("");
        }
    }
}
