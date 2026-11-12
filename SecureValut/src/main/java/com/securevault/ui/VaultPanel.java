package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.model.VaultEntry;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VaultPanel extends JPanel {

    private final Database database;
    private final SecretKey key;

    private final JTextField searchField = new JTextField();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{"ID", "Title", "Username", "Category", "Website"},
                    0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable table = new JTable(tableModel);
    private final JLabel countLabel = new JLabel("0 credentials");

    private List<VaultEntry> currentEntries = new ArrayList<>();

    public VaultPanel(Database database, SecretKey key) {
        this.database = database;
        this.key = key;

        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(22, 22, 22, 22));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG);

        JPanel titleArea = new JPanel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setBackground(Theme.BG);

        JLabel title = Theme.heading("Password Vault");

        JLabel subtitle = new JLabel(
                "Manage your saved accounts and credentials"
        );
        subtitle.setForeground(Theme.MUTED);

        titleArea.add(title);
        titleArea.add(Box.createVerticalStrut(5));
        titleArea.add(subtitle);

        JButton addButton = Theme.button("+ Add Credential");
        addButton.setBackground(Theme.ACCENT);
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addCredential());

        header.add(titleArea, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Theme.BG);

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(Theme.BG);

        searchField.setBackground(Theme.LIGHT);
        searchField.setForeground(Theme.FG);
        searchField.setCaretColor(Theme.FG);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(
                new EmptyBorder(10, 12, 10, 12)
        );

        searchField.setToolTipText(
                "Search by title, username, or category"
        );

        searchField.addActionListener(e -> refreshData());

        JButton searchButton = Theme.button("Search");
        searchButton.addActionListener(e -> refreshData());

        JButton clearButton = Theme.button("Clear");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshData();
        });

        JPanel searchActions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 5, 0)
        );

        searchActions.setBackground(Theme.BG);
        searchActions.add(searchButton);
        searchActions.add(clearButton);

        toolbar.add(searchField, BorderLayout.CENTER);
        toolbar.add(searchActions, BorderLayout.EAST);

        table.setBackground(Theme.PANEL);
        table.setForeground(Theme.FG);
        table.setGridColor(Theme.LIGHT);
        table.setSelectionBackground(Theme.ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getTableHeader().setBackground(Theme.LIGHT);
        table.getTableHeader().setForeground(Theme.FG);
        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        table.getTableHeader().setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewCredential();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.PANEL);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Theme.BG);

        countLabel.setForeground(Theme.MUTED);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        actions.setBackground(Theme.BG);

        JButton viewButton = Theme.button("View");
        JButton editButton = Theme.button("Edit");
        JButton deleteButton = Theme.button("Delete");

        viewButton.addActionListener(e -> viewCredential());
        editButton.addActionListener(e -> editCredential());
        deleteButton.addActionListener(e -> deleteCredential());

        actions.add(viewButton);
        actions.add(editButton);
        actions.add(deleteButton);

        bottom.add(countLabel, BorderLayout.WEST);
        bottom.add(actions, BorderLayout.EAST);

        content.add(toolbar, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        return content;
    }

    public void refresh() {
        refreshData();
    }

    public void refreshData() {
        try {
            currentEntries = database.entries(key);

            String query = searchField.getText()
                    .trim()
                    .toLowerCase();

            tableModel.setRowCount(0);

            for (VaultEntry entry : currentEntries) {
                boolean matches =
                        entry.title().toLowerCase().contains(query)
                        || entry.username().toLowerCase().contains(query)
                        || entry.category().toLowerCase().contains(query);

                if (matches) {
                    tableModel.addRow(new Object[]{
                            entry.id(),
                            entry.title(),
                            entry.username(),
                            entry.category(),
                            entry.url()
                    });
                }
            }

            countLabel.setText(
                    tableModel.getRowCount() + " credentials"
            );

        } catch (Exception e) {
            showError("Could not load credentials.", e);
        }
    }

    private Window getOwnerWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void addCredential() {
        EntryDialog dialog = new EntryDialog(getOwnerWindow(), null);
        dialog.setVisible(true);

        if (!dialog.saved()) {
            return;
        }

        try {
            database.add(dialog.value(), key);
            refreshData();

        } catch (Exception e) {
            showError("Could not save credential.", e);
        }
    }

    private void editCredential() {
        VaultEntry entry = getSelectedEntry();

        if (entry == null) {
            showMessage("Please select a credential first.");
            return;
        }

        EntryDialog dialog = new EntryDialog(getOwnerWindow(), entry);
        dialog.setVisible(true);

        if (!dialog.saved()) {
            return;
        }

        try {
            database.update(dialog.value(), key);
            refreshData();

        } catch (Exception e) {
            showError("Could not update credential.", e);
        }
    }

    private void deleteCredential() {
        VaultEntry entry = getSelectedEntry();

        if (entry == null) {
            showMessage("Please select a credential first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete \"" + entry.title() + "\" permanently?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            database.delete(entry.id());
            refreshData();

        } catch (Exception e) {
            showError("Could not delete credential.", e);
        }
    }

    private void viewCredential() {
        VaultEntry entry = getSelectedEntry();

        if (entry == null) {
            showMessage("Please select a credential first.");
            return;
        }

        JDialog dialog = new JDialog(
                getOwnerWindow(),
                "Credential Details",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setSize(440, 430);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBackground(Theme.PANEL);
        panel.setBorder(new EmptyBorder(20, 22, 20, 22));

        addDetail(panel, "Title", entry.title());
        addDetail(panel, "Username", entry.username());
        addDetail(panel, "Website", entry.url());
        addDetail(panel, "Category", entry.category());
        addDetail(panel, "Created", entry.createdAt());

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(Theme.MUTED);

        JPasswordField passwordField =
                new JPasswordField(entry.password());

        passwordField.setEchoChar('•');
        passwordField.setBackground(Theme.LIGHT);
        passwordField.setForeground(Theme.FG);
        passwordField.setEditable(false);

        JButton revealButton = Theme.button("Reveal");

        revealButton.addActionListener(e -> {
            boolean hidden = passwordField.getEchoChar() != 0;

            passwordField.setEchoChar(hidden ? (char) 0 : '•');
            revealButton.setText(hidden ? "Hide" : "Reveal");
        });

        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(revealButton);

        JTextArea notes = new JTextArea(entry.notes());
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setEditable(false);
        notes.setBackground(Theme.LIGHT);
        notes.setForeground(Theme.FG);

        panel.add(new JLabel("Notes"));
        panel.add(new JScrollPane(notes));

        dialog.setContentPane(panel);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        passwordField.setText("");
    }

    private void addDetail(
            JPanel panel,
            String label,
            String value
    ) {
        JLabel name = new JLabel(label);
        name.setForeground(Theme.MUTED);

        JLabel content = new JLabel(
                value == null || value.isBlank() ? "—" : value
        );
        content.setForeground(Theme.FG);

        panel.add(name);
        panel.add(content);
    }

    private VaultEntry getSelectedEntry() {
        int row = table.getSelectedRow();

        if (row < 0) {
            return null;
        }

        long id = ((Number) tableModel.getValueAt(row, 0)).longValue();

        for (VaultEntry entry : currentEntries) {
            if (entry.id() == id) {
                return entry;
            }
        }

        return null;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "SecureVault",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(
                this,
                message + "\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
