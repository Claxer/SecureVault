package com.securevault.ui;

import com.securevault.model.VaultEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;

public class EntryDialog extends JDialog {

    private final JTextField titleField = new JTextField();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField urlField = new JTextField();
    private final JComboBox<String> categoryField = new JComboBox<>(
            new String[]{
                    "Uncategorized",
                    "Personal",
                    "Work",
                    "Finance",
                    "Social Media",
                    "Email",
                    "Development",
                    "Other"
            }
    );
    private final JTextArea notesField = new JTextArea(4, 20);

    private final VaultEntry originalEntry;
    private boolean saved = false;
    private VaultEntry result;

    public EntryDialog(Window owner, VaultEntry entry) {
        super(owner, entry == null ? "Add Credential" : "Edit Credential",
                ModalityType.APPLICATION_MODAL);

        this.originalEntry = entry;

        setSize(480, 610);
        setMinimumSize(new Dimension(420, 550));
        setLocationRelativeTo(owner);

        buildUI();

        if (entry != null) {
            populateFields(entry);
        }
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(Theme.BG);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel heading = Theme.heading(
                originalEntry == null
                        ? "Add Credential"
                        : "Edit Credential"
        );

        root.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 7));
        fields.setBackground(Theme.BG);

        addField(fields, "Title", titleField);
        addField(fields, "Username / Email", usernameField);
        addField(fields, "Password", passwordField);
        addField(fields, "Website URL", urlField);
        addField(fields, "Category", categoryField);

        JLabel notesLabel = createLabel("Notes");
        fields.add(notesLabel);

        notesField.setLineWrap(true);
        notesField.setWrapStyleWord(true);
        notesField.setBackground(Theme.LIGHT);
        notesField.setForeground(Theme.FG);
        notesField.setCaretColor(Theme.FG);
        notesField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesField.setBorder(new EmptyBorder(9, 10, 9, 10));

        JScrollPane notesScroll = new JScrollPane(notesField);
        notesScroll.setBorder(BorderFactory.createEmptyBorder());
        notesScroll.getViewport().setBackground(Theme.LIGHT);
        fields.add(notesScroll);

        root.add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Theme.BG);

        JButton cancelButton = Theme.button("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = Theme.button("Save Credential");
        saveButton.setBackground(Theme.ACCENT);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveEntry());

        actions.add(cancelButton);
        actions.add(saveButton);

        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addField(JPanel panel, String label, JComponent field) {
        panel.add(createLabel(label));

        Theme.style(field);

        field.setPreferredSize(new Dimension(300, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(field);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.MUTED);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }

    private void populateFields(VaultEntry entry) {
        titleField.setText(entry.title());
        usernameField.setText(entry.username());
        passwordField.setText(entry.password());
        urlField.setText(entry.url());
        notesField.setText(entry.notes());
        categoryField.setSelectedItem(entry.category());

        if (categoryField.getSelectedItem() == null) {
            categoryField.addItem(entry.category());
            categoryField.setSelectedItem(entry.category());
        }
    }

    private void saveEntry() {
        String title = titleField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String url = urlField.getText().trim();
        String category = String.valueOf(categoryField.getSelectedItem());
        String notes = notesField.getText().trim();

        if (title.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a credential title.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (password.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        long id = originalEntry == null ? 0 : originalEntry.id();

        String createdAt = originalEntry == null
                ? LocalDateTime.now().toString()
                : originalEntry.createdAt();

        result = new VaultEntry(
                id,
                title,
                username,
                password,
                url,
                category,
                notes,
                createdAt
        );

        saved = true;
        passwordField.setText("");
        dispose();
    }

    public boolean saved() {
        return saved;
    }

    public VaultEntry value() {
        return result;
    }
}
