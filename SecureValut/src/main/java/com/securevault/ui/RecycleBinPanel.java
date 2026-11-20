package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.model.VaultEntry;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RecycleBinPanel extends JPanel {

    private final Database database;
    private final SecretKey secretKey;

    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JLabel countLabel;
    private final JLabel descriptionLabel;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a");

    public RecycleBinPanel(Database database, SecretKey secretKey) {

        this.database = database;
        this.secretKey = secretKey;

        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(30, 35, 30, 35));

        // =====================================================
        // HEADER
        // =====================================================

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(
                titlePanel,
                BoxLayout.Y_AXIS
        ));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Recycle Bin");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Theme.LIGHT);

        descriptionLabel = new JLabel(
                "Manage deleted credentials and restore them when needed."
        );
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionLabel.setForeground(Theme.MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(descriptionLabel);

        countLabel = new JLabel("0 deleted items");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        countLabel.setForeground(Theme.LIGHT);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // =====================================================
        // TABLE
        // =====================================================

        String[] columns = {
                "ID",
                "Credential",
                "Username",
                "Category",
                "Date Added",
                "Date Deleted"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        };

        table = new JTable(tableModel);

        table.setBackground(Theme.PANEL);
        table.setForeground(Theme.LIGHT);
        table.setSelectionBackground(Theme.ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(Theme.BG);
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setBackground(Theme.LIGHT);
        table.getTableHeader().setForeground(Theme.BG);
        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        table.getTableHeader().setPreferredSize(
                new Dimension(0, 38)
        );

        table.setAutoCreateRowSorter(true);

        // Hide the ID column while keeping it available internally.
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                Theme.LIGHT.darker()
        ));
        scrollPane.getViewport().setBackground(Theme.PANEL);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // =====================================================
        // BOTTOM ACTIONS
        // =====================================================

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JLabel warning = new JLabel(
                "Permanently deleted credentials cannot be recovered."
        );
        warning.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        warning.setForeground(Theme.MUTED);

        JPanel buttons = new JPanel(new FlowLayout(
                FlowLayout.RIGHT,
                10,
                0
        ));
        buttons.setOpaque(false);

        JButton refreshButton = createButton(
                "Refresh",
                Theme.LIGHT,
                Theme.BG
        );

        JButton restoreButton = createButton(
                "Restore",
                new Color(65, 145, 105),
                Color.WHITE
        );

        JButton deleteButton = createButton(
                "Delete Permanently",
                new Color(170, 65, 65),
                Color.WHITE
        );

        JButton emptyButton = createButton(
                "Empty Recycle Bin",
                new Color(115, 45, 45),
                Color.WHITE
        );

        refreshButton.addActionListener(e -> refresh());
        restoreButton.addActionListener(e -> restoreSelected());
        deleteButton.addActionListener(e -> permanentlyDeleteSelected());
        emptyButton.addActionListener(e -> emptyRecycleBin());

        buttons.add(refreshButton);
        buttons.add(restoreButton);
        buttons.add(deleteButton);
        buttons.add(emptyButton);

        bottomPanel.add(warning, BorderLayout.WEST);
        bottomPanel.add(buttons, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        refresh();
    }

    // =========================================================
    // BUTTON CREATION
    // =========================================================

    private JButton createButton(
            String text,
            Color background,
            Color foreground
    ) {

        JButton button = new JButton(text);

        button.setFont(new Font(
                "Segoe UI",
                Font.BOLD,
                12
        ));

        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(
                11, 15, 11, 15
        ));
        button.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR
        ));

        return button;
    }

    // =========================================================
    // REFRESH RECYCLE BIN
    // =========================================================

    public void refresh() {

        tableModel.setRowCount(0);

        try {

            List<VaultEntry> deletedEntries =
                    database.deletedEntries(secretKey);

            for (VaultEntry entry : deletedEntries) {

                String deletedDate = database.deletedAt(
                        entry.id()
                );

                tableModel.addRow(new Object[]{

                        entry.id(),

                        entry.title(),

                        entry.username(),

                        entry.category() == null
                                || entry.category().isBlank()
                                ? "Uncategorized"
                                : entry.category(),

                        formatDate(entry.createdAt()),

                        formatDate(deletedDate)

                });
            }

            int count = deletedEntries.size();

            countLabel.setText(
                    count + (count == 1
                            ? " deleted item"
                            : " deleted items")
            );

            descriptionLabel.setText(
                    count == 0
                            ? "Your Recycle Bin is currently empty."
                            : "Restore deleted credentials or permanently remove them."
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load deleted credentials:\n"
                            + e.getMessage(),
                    "Recycle Bin Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // RESTORE SELECTED CREDENTIAL
    // =========================================================

    private void restoreSelected() {

        Long id = getSelectedId();

        if (id == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a credential to restore.",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Restore this credential to your Vault?",
                "Restore Credential",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            database.restore(id);

            JOptionPane.showMessageDialog(
                    this,
                    "Credential successfully restored.",
                    "Restore Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

            refresh();

        } catch (Exception e) {

            showError("Could not restore credential.", e);
        }
    }

    // =========================================================
    // PERMANENTLY DELETE SELECTED CREDENTIAL
    // =========================================================

    private void permanentlyDeleteSelected() {

        Long id = getSelectedId();

        if (id == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a credential to delete.",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete this credential?\n\n"
                        + "This action cannot be undone.",
                "Permanently Delete Credential",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            database.permanentlyDelete(id);

            JOptionPane.showMessageDialog(
                    this,
                    "Credential permanently deleted.",
                    "Delete Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

            refresh();

        } catch (Exception e) {

            showError("Could not permanently delete credential.", e);
        }
    }

    // =========================================================
    // EMPTY RECYCLE BIN
    // =========================================================

    private void emptyRecycleBin() {

        int count = database.recycleBinCount();

        if (count == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "The Recycle Bin is already empty.",
                    "Recycle Bin",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "You are about to permanently delete all "
                        + count + " deleted credential(s).\n\n"
                        + "This action cannot be undone.",
                "Empty Recycle Bin",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            database.emptyRecycleBin();

            JOptionPane.showMessageDialog(
                    this,
                    "Recycle Bin successfully emptied.",
                    "Operation Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

            refresh();

        } catch (Exception e) {

            showError("Could not empty the Recycle Bin.", e);
        }
    }

    // =========================================================
    // SELECTED ROW
    // =========================================================

    private Long getSelectedId() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            return null;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        return ((Number) tableModel.getValueAt(
                modelRow,
                0
        )).longValue();
    }

    // =========================================================
    // DATE FORMATTING
    // =========================================================

    private String formatDate(String value) {

        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        try {

            return LocalDateTime.parse(value).format(DATE_FORMAT);

        } catch (Exception ignored) {

            return value;
        }
    }

    // =========================================================
    // ERROR DIALOG
    // =========================================================

    private void showError(String message, Exception e) {

        JOptionPane.showMessageDialog(
                this,
                message + "\n\n" + e.getMessage(),
                "Recycle Bin Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
