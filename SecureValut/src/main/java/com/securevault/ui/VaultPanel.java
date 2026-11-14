package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.model.VaultEntry;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VaultPanel extends JPanel {

    private final Database database;
    private final SecretKey key;

    private final JTextField searchField = new JTextField();

    private final JComboBox<String> favoriteFilter =
            new JComboBox<>(
                    new String[]{
                            "All Credentials",
                            "Favorites Only",
                            "Non-Favorites"
                    }
            );

    private final JComboBox<String> sortOptions =
            new JComboBox<>(
                    new String[]{
                            "Newest First",
                            "Oldest First",
                            "Title A-Z",
                            "Title Z-A",
                            "Username A-Z",
                            "Category A-Z"
                    }
            );

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                            "ID",
                            "Favorite",
                            "Title",
                            "Username",
                            "Category",
                            "Website"
                    },
                    0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int column) {
                    return column == 1
                            ? Boolean.class
                            : Object.class;
                }
            };

    private final JTable table = new JTable(tableModel);

    private final TableRowSorter<DefaultTableModel> sorter =
            new TableRowSorter<>(tableModel);

    private final JLabel countLabel =
            new JLabel("0 credentials");

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
        titleArea.setLayout(
                new BoxLayout(titleArea, BoxLayout.Y_AXIS)
        );
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

        JPanel toolbar = new JPanel(
                new BorderLayout(8, 8)
        );
        toolbar.setBackground(Theme.BG);

        JPanel searchArea = new JPanel(new BorderLayout(8, 0));
        searchArea.setBackground(Theme.BG);

        searchField.setBackground(Theme.LIGHT);
        searchField.setForeground(Theme.FG);
        searchField.setCaretColor(Theme.FG);
        searchField.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        searchField.setBorder(
                new EmptyBorder(10, 12, 10, 12)
        );

        searchField.setToolTipText(
                "Search title, username, category, website, or notes"
        );

        searchField.addActionListener(e -> applyFilters());

        JButton searchButton = Theme.button("Search");
        searchButton.addActionListener(e -> applyFilters());

        JButton clearButton = Theme.button("Clear");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            favoriteFilter.setSelectedIndex(0);
            applyFilters();
        });

        JPanel searchActions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 5, 0)
        );
        searchActions.setBackground(Theme.BG);

        searchActions.add(searchButton);
        searchActions.add(clearButton);

        searchArea.add(searchField, BorderLayout.CENTER);
        searchArea.add(searchActions, BorderLayout.EAST);

        JPanel filterArea = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0)
        );
        filterArea.setBackground(Theme.BG);

        favoriteFilter.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        sortOptions.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        favoriteFilter.setBackground(Theme.LIGHT);
        favoriteFilter.setForeground(Theme.FG);

        sortOptions.setBackground(Theme.LIGHT);
        sortOptions.setForeground(Theme.FG);

        favoriteFilter.addActionListener(e -> applyFilters());
        sortOptions.addActionListener(e -> applySorting());

        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setForeground(Theme.MUTED);

        JLabel sortLabel = new JLabel("Sort:");
        sortLabel.setForeground(Theme.MUTED);

        filterArea.add(filterLabel);
        filterArea.add(favoriteFilter);
        filterArea.add(Box.createHorizontalStrut(12));
        filterArea.add(sortLabel);
        filterArea.add(sortOptions);

        toolbar.add(searchArea, BorderLayout.NORTH);
        toolbar.add(filterArea, BorderLayout.SOUTH);

        configureTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.PANEL);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Theme.BG);

        countLabel.setForeground(Theme.MUTED);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 7, 0)
        );
        actions.setBackground(Theme.BG);

        JButton favoriteButton = Theme.button("Toggle Favorite");
        JButton copyButton = Theme.button("Copy Password");
        JButton viewButton = Theme.button("View");
        JButton editButton = Theme.button("Edit");
        JButton deleteButton = Theme.button("Delete");

        favoriteButton.addActionListener(e -> toggleFavorite());
        copyButton.addActionListener(e -> copyPassword());
        viewButton.addActionListener(e -> viewCredential());
        editButton.addActionListener(e -> editCredential());
        deleteButton.addActionListener(e -> deleteCredential());

        actions.add(favoriteButton);
        actions.add(copyButton);
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

    private void configureTable() {
        table.setBackground(Theme.PANEL);
        table.setForeground(Theme.FG);
        table.setGridColor(Theme.LIGHT);
        table.setSelectionBackground(Theme.ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowVerticalLines(false);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.setRowSorter(sorter);

        table.getTableHeader().setBackground(Theme.LIGHT);
        table.getTableHeader().setForeground(Theme.FG);
        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        table.getTableHeader().setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getColumnModel().getColumn(1).setMaxWidth(75);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewCredential();
                }
            }
        });
    }

    public void refresh() {
        refreshData();
    }

    public void refreshData() {
        try {
            currentEntries = database.entries(key);

            tableModel.setRowCount(0);

            for (VaultEntry entry : currentEntries) {
                tableModel.addRow(new Object[]{
                        entry.id(),
                        entry.favorite(),
                        entry.title(),
                        entry.username(),
                        entry.category(),
                        entry.url()
                });
            }

            applyFilters();
            applySorting();

        } catch (Exception e) {
            showError("Could not load credentials.", e);
        }
    }

    private void applyFilters() {
        String query = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        int favoriteMode = favoriteFilter.getSelectedIndex();

        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(
                    Entry<? extends DefaultTableModel,
                            ? extends Integer> entry
            ) {
                int modelRow = entry.getIdentifier();

                long id = ((Number) tableModel.getValueAt(
                        modelRow, 0
                )).longValue();

                VaultEntry credential = findEntry(id);

                if (credential == null) {
                    return false;
                }

                if (favoriteMode == 1 && !credential.favorite()) {
                    return false;
                }

                if (favoriteMode == 2 && credential.favorite()) {
                    return false;
                }

                if (query.isBlank()) {
                    return true;
                }

                return credential.title().toLowerCase(Locale.ROOT)
                        .contains(query)
                        || credential.username().toLowerCase(Locale.ROOT)
                        .contains(query)
                        || credential.category().toLowerCase(Locale.ROOT)
                        .contains(query)
                        || credential.url().toLowerCase(Locale.ROOT)
                        .contains(query)
                        || credential.notes().toLowerCase(Locale.ROOT)
                        .contains(query);
            }
        });

        countLabel.setText(
                table.getRowCount() + " credentials"
        );
    }

    private void applySorting() {
        int selected = sortOptions.getSelectedIndex();

        switch (selected) {
            case 1 -> sorter.setSortKeys(List.of(
                    new RowSorter.SortKey(
                            0,
                            SortOrder.ASCENDING
                    )
            ));

            case 2 -> sorter.setSortKeys(List.of(
                    new RowSorter.SortKey(
                            2,
                            SortOrder.ASCENDING
                    )
            ));

            case 3 -> sorter.setSortKeys(List.of(
                    new RowSorter.SortKey(
                            2,
                            SortOrder.DESCENDING
                    )
            ));

            case 4 -> sorter.setSortKeys(List.of(
                    new RowSorter.SortKey(
                            3,
                            SortOrder.ASCENDING
                    )
            ));

            case 5 -> sorter.setSortKeys(List.of(
                    new RowSorter.SortKey(
                            4,
                            SortOrder.ASCENDING
                    )
            ));

            default -> sorter.setSortKeys(List.of(
                    new RowSorter.SortKey(
                            0,
                            SortOrder.DESCENDING
                    )
            ));
        }

        countLabel.setText(
                table.getRowCount() + " credentials"
        );
    }

    private VaultEntry findEntry(long id) {
        for (VaultEntry entry : currentEntries) {
            if (entry.id() == id) {
                return entry;
            }
        }

        return null;
    }

    private Window getOwnerWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void addCredential() {
        EntryDialog dialog = new EntryDialog(
                getOwnerWindow(),
                null
        );

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

        EntryDialog dialog = new EntryDialog(
                getOwnerWindow(),
                entry
        );

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

    private void toggleFavorite() {
        VaultEntry entry = getSelectedEntry();

        if (entry == null) {
            showMessage("Please select a credential first.");
            return;
        }

        try {
            database.setFavorite(
                    entry.id(),
                    !entry.favorite()
            );

            refreshData();

        } catch (Exception e) {
            showError("Could not update favorite.", e);
        }
    }

    private void copyPassword() {
        VaultEntry entry = getSelectedEntry();

        if (entry == null) {
            showMessage("Please select a credential first.");
            return;
        }

        try {
            String password = entry.password();

            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(
                            new StringSelection(password),
                            null
                    );

            database.logEvent("Credential password copied");

            Timer clearTimer = new Timer(30_000, e -> {
                try {
                    Transferable current =
                            Toolkit.getDefaultToolkit()
                                    .getSystemClipboard()
                                    .getContents(null);

                    if (current != null
                            && current.isDataFlavorSupported(
                                    DataFlavor.stringFlavor
                            )) {

                        String clipboardText = (String)
                                current.getTransferData(
                                        DataFlavor.stringFlavor
                                );

                        if (password.equals(clipboardText)) {
                            Toolkit.getDefaultToolkit()
                                    .getSystemClipboard()
                                    .setContents(
                                            new StringSelection(""),
                                            null
                                    );
                        }
                    }

                } catch (Exception ignored) {
                    // Clipboard may be unavailable or locked.
                }
            });

            clearTimer.setRepeats(false);
            clearTimer.start();

            showMessage(
                    "Password copied. Clipboard clearing will be attempted in 30 seconds."
            );

        } catch (Exception e) {
            showError("Could not copy password.", e);
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

        dialog.setSize(440, 460);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(
                new GridLayout(0, 1, 8, 8)
        );

        panel.setBackground(Theme.PANEL);
        panel.setBorder(
                new EmptyBorder(20, 22, 20, 22)
        );

        addDetail(panel, "Title", entry.title());
        addDetail(panel, "Username", entry.username());
        addDetail(panel, "Website", entry.url());
        addDetail(panel, "Category", entry.category());
        addDetail(panel, "Created", entry.createdAt());
        addDetail(
                panel,
                "Favorite",
                entry.favorite() ? "Yes" : "No"
        );

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
            boolean hidden =
                    passwordField.getEchoChar() != 0;

            passwordField.setEchoChar(
                    hidden ? (char) 0 : '•'
            );

            revealButton.setText(
                    hidden ? "Hide" : "Reveal"
            );
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
                value == null || value.isBlank()
                        ? "—"
                        : value
        );

        content.setForeground(Theme.FG);

        panel.add(name);
        panel.add(content);
    }

    private VaultEntry getSelectedEntry() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            return null;
        }

        int modelRow = table.convertRowIndexToModel(
                selectedRow
        );

        long id = ((Number) tableModel.getValueAt(
                modelRow, 0
        )).longValue();

        return findEntry(id);
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
