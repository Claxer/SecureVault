package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.model.SecureNote;
import com.securevault.service.NoteService;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SecureNotesPanel extends JPanel {

    private static final Color BG = new Color(18, 18, 20);
    private static final Color CARD = new Color(29, 29, 32);
    private static final Color BORDER = new Color(48, 48, 52);
    private static final Color TEXT = new Color(240, 240, 240);
    private static final Color MUTED = new Color(155, 155, 160);
    private static final Color ACCENT = new Color(85, 135, 210);

    private final NoteService noteService;

    private final DefaultTableModel tableModel;
    private final JTable notesTable;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final JTextField searchField;
    private final JLabel totalLabel;
    private final JLabel pinnedLabel;

    private List<SecureNote> notes = new ArrayList<>();

    public SecureNotesPanel(Database database, SecretKey secretKey) {

        noteService = new NoteService(database, secretKey);

        setLayout(new BorderLayout(18, 18));
        setBackground(BG);
        setBorder(new EmptyBorder(27, 28, 27, 28));

        // Initialize the table before building the layout.
        tableModel = new DefaultTableModel(
                new String[]{
                        "ID", "Title", "Category", "Pinned", "Last Updated"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        notesTable = new JTable(tableModel);
        configureTable();

        sorter = new TableRowSorter<>(tableModel);
        notesTable.setRowSorter(sorter);

        searchField = createTextField();

        searchField.setToolTipText(
                "Search notes by title or category"
        );

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        applySearch();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        applySearch();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        applySearch();
                    }
                }
        );

        totalLabel = new JLabel("0");
        pinnedLabel = new JLabel("0");

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(
                heading, BoxLayout.Y_AXIS
        ));
        heading.setOpaque(false);

        JLabel title = new JLabel("Secure Notes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Store and protect your private information."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);

        JButton addButton = createButton("+ New Note", ACCENT);
        addButton.addActionListener(e -> showNoteDialog(null));

        header.add(heading, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Main content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(
                content, BoxLayout.Y_AXIS
        ));
        content.setOpaque(false);

        content.add(createStatistics());
        content.add(Box.createVerticalStrut(18));
        content.add(createNotesSection());

        add(content, BorderLayout.CENTER);

        refresh();
    }

    private void configureTable() {

        notesTable.setFont(new Font(
                "Segoe UI", Font.PLAIN, 13
        ));

        notesTable.setRowHeight(39);
        notesTable.setForeground(TEXT);
        notesTable.setBackground(CARD);
        notesTable.setGridColor(BORDER);

        notesTable.setSelectionBackground(
                new Color(65, 65, 73)
        );

        notesTable.setSelectionForeground(TEXT);
        notesTable.setFillsViewportHeight(true);

        notesTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        notesTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS
        );

        notesTable.getTableHeader().setBackground(
                new Color(42, 42, 46)
        );

        notesTable.getTableHeader().setForeground(TEXT);

        notesTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        notesTable.getColumnModel().getColumn(0)
                .setMinWidth(0);

        notesTable.getColumnModel().getColumn(0)
                .setMaxWidth(0);

        notesTable.getColumnModel().getColumn(0)
                .setPreferredWidth(0);

        notesTable.getColumnModel().getColumn(1)
                .setPreferredWidth(220);

        notesTable.getColumnModel().getColumn(2)
                .setPreferredWidth(130);

        notesTable.getColumnModel().getColumn(3)
                .setPreferredWidth(75);

        notesTable.getColumnModel().getColumn(4)
                .setPreferredWidth(180);

        notesTable.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent e
                    ) {

                        if (e.getClickCount() == 2
                                && notesTable.getSelectedRow() >= 0) {
                            editSelectedNote();
                        }
                    }
                }
        );
    }

    private JPanel createStatistics() {

        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setOpaque(false);

        panel.add(createStatCard(
                "Total Secure Notes",
                totalLabel
        ));

        panel.add(createStatCard(
                "Pinned Notes",
                pinnedLabel
        ));

        return panel;
    }

    private JPanel createStatCard(
            String heading,
            JLabel value
    ) {

        JPanel card = createCard();

        card.setLayout(new BoxLayout(
                card, BoxLayout.Y_AXIS
        ));

        card.setBorder(new EmptyBorder(17, 20, 17, 20));

        JLabel title = new JLabel(heading);
        title.setForeground(MUTED);
        title.setFont(new Font(
                "Segoe UI", Font.PLAIN, 12
        ));

        value.setForeground(TEXT);
        value.setFont(new Font(
                "Segoe UI", Font.BOLD, 27
        ));

        card.add(title);
        card.add(Box.createVerticalStrut(9));
        card.add(value);

        return card;
    }

    private JPanel createNotesSection() {

        JPanel section = createCard();

        section.setLayout(new BorderLayout(12, 15));
        section.setBorder(new EmptyBorder(19, 19, 19, 19));

        JLabel heading = new JLabel("Your Private Notes");
        heading.setForeground(TEXT);
        heading.setFont(new Font(
                "Segoe UI", Font.BOLD, 16
        ));

        JPanel actions = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 7, 0
        ));
        actions.setOpaque(false);

        JButton editButton = createButton(
                "Edit", new Color(65, 65, 72)
        );

        JButton pinButton = createButton(
                "Pin / Unpin", new Color(65, 65, 72)
        );

        JButton deleteButton = createButton(
                "Delete", new Color(155, 65, 65)
        );

        JButton refreshButton = createButton(
                "Refresh", new Color(65, 65, 72)
        );

        editButton.addActionListener(e -> editSelectedNote());
        pinButton.addActionListener(e -> toggleSelectedPin());
        deleteButton.addActionListener(e -> deleteSelectedNote());
        refreshButton.addActionListener(e -> refresh());

        actions.add(editButton);
        actions.add(pinButton);
        actions.add(deleteButton);
        actions.add(refreshButton);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(heading, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setForeground(MUTED);

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        top.add(toolbar);
        top.add(Box.createVerticalStrut(14));
        top.add(searchPanel);

        section.add(top, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(notesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(CARD);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        section.add(scrollPane, BorderLayout.CENTER);

        JLabel hint = new JLabel(
                "Double-click a note to view or edit its contents."
        );

        hint.setForeground(MUTED);
        hint.setFont(new Font(
                "Segoe UI", Font.PLAIN, 11
        ));

        section.add(hint, BorderLayout.SOUTH);

        return section;
    }

    private JPanel createCard() {

        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createLineBorder(BORDER));

        return panel;
    }

    private JButton createButton(String text, Color background) {

        JButton button = new JButton(text);

        button.setFont(new Font(
                "Segoe UI", Font.BOLD, 12
        ));

        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);

        button.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR
        ));

        button.setBorder(new EmptyBorder(
                10, 15, 10, 15
        ));

        return button;
    }

    private JTextField createTextField() {

        JTextField field = new JTextField();

        field.setFont(new Font(
                "Segoe UI", Font.PLAIN, 13
        ));

        field.setBackground(new Color(39, 39, 43));
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(9, 10, 9, 10)
        ));

        return field;
    }

    // =========================================================
    // REFRESH AND SEARCH
    // =========================================================

    public void refresh() {

        try {

            notes = noteService.getAllNotes();

            tableModel.setRowCount(0);

            int pinnedCount = 0;

            for (SecureNote note : notes) {

                if (note.pinned()) {
                    pinnedCount++;
                }

                tableModel.addRow(new Object[]{
                        note.id(),
                        note.title(),
                        note.category(),
                        note.pinned() ? "Yes" : "No",
                        note.updatedAt().replace('T', ' ')
                });
            }

            totalLabel.setText(String.valueOf(notes.size()));
            pinnedLabel.setText(String.valueOf(pinnedCount));

            applySearch();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load secure notes:\n" + e.getMessage(),
                    "Secure Notes Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void applySearch() {

        String search = searchField.getText().trim();

        if (search.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" + Pattern.quote(search),
                            1, 2
                    )
            );
        }
    }

    // =========================================================
    // CREATE AND EDIT
    // =========================================================

    private void showNoteDialog(SecureNote existing) {

        boolean editing = existing != null;

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                editing ? "Edit Secure Note" : "Create Secure Note",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setSize(650, 570);
        dialog.setMinimumSize(new Dimension(550, 480));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(
                main, BoxLayout.Y_AXIS
        ));

        main.setBackground(BG);
        main.setBorder(new EmptyBorder(23, 25, 20, 25));

        JLabel heading = new JLabel(
                editing ? "Edit Note" : "New Secure Note"
        );

        heading.setFont(new Font(
                "Segoe UI", Font.BOLD, 23
        ));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Your note will be encrypted before being saved."
        );

        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font(
                "Segoe UI", Font.PLAIN, 12
        ));

        main.add(heading);
        main.add(Box.createVerticalStrut(5));
        main.add(subtitle);
        main.add(Box.createVerticalStrut(20));

        JTextField titleField = createTextField();

        if (editing) {
            titleField.setText(existing.title());
        }

        JComboBox<String> categoryBox = new JComboBox<>(
                new String[]{
                        "Personal",
                        "Work",
                        "School",
                        "Financial",
                        "Technical",
                        "Other"
                }
        );

        categoryBox.setEditable(true);
        categoryBox.setSelectedItem(
                editing ? existing.category() : "Personal"
        );

        categoryBox.setFont(new Font(
                "Segoe UI", Font.PLAIN, 13
        ));

        categoryBox.setBackground(new Color(39, 39, 43));
        categoryBox.setForeground(TEXT);

        Component editor = categoryBox.getEditor()
                .getEditorComponent();

        if (editor instanceof JTextField field) {
            field.setBackground(new Color(39, 39, 43));
            field.setForeground(TEXT);
            field.setCaretColor(TEXT);
        }

        JTextArea contentArea = new JTextArea();

        contentArea.setFont(new Font(
                "Segoe UI", Font.PLAIN, 13
        ));

        contentArea.setBackground(new Color(39, 39, 43));
        contentArea.setForeground(TEXT);
        contentArea.setCaretColor(TEXT);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        contentArea.setBorder(new EmptyBorder(
                12, 12, 12, 12
        ));

        if (editing) {
            contentArea.setText(existing.content());
        }

        JCheckBox pinnedCheckBox = new JCheckBox(
                "Pin this note"
        );

        pinnedCheckBox.setOpaque(false);
        pinnedCheckBox.setForeground(TEXT);
        pinnedCheckBox.setFont(new Font(
                "Segoe UI", Font.PLAIN, 12
        ));

        pinnedCheckBox.setSelected(
                editing && existing.pinned()
        );

        addField(main, "Note Title", titleField);
        main.add(Box.createVerticalStrut(14));

        addField(main, "Category", categoryBox);
        main.add(Box.createVerticalStrut(14));

        JLabel contentLabel = new JLabel("Note Content");
        contentLabel.setForeground(TEXT);
        contentLabel.setFont(new Font(
                "Segoe UI", Font.BOLD, 12
        ));

        main.add(contentLabel);
        main.add(Box.createVerticalStrut(7));

        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        contentScroll.getViewport().setBackground(
                new Color(39, 39, 43)
        );

        contentScroll.setPreferredSize(
                new Dimension(570, 210)
        );

        contentScroll.setMinimumSize(
                new Dimension(400, 150)
        );

        main.add(contentScroll);
        main.add(Box.createVerticalStrut(12));
        main.add(pinnedCheckBox);

        JPanel footer = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 10, 12
        ));

        footer.setBackground(CARD);

        JButton cancelButton = createButton(
                "Cancel", new Color(65, 65, 72)
        );

        JButton saveButton = createButton(
                editing ? "Save Changes" : "Create Note",
                ACCENT
        );

        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {

            String title = titleField.getText();
            String content = contentArea.getText();

            Object categoryValue = categoryBox.getEditor()
                    .getItem();

            String category = categoryValue == null
                    ? "Personal"
                    : categoryValue.toString().trim();

            if (category.isEmpty()) {
                category = "Personal";
            }

            try {

                if (editing) {

                    noteService.updateNote(
                            existing.id(),
                            title,
                            content,
                            category,
                            pinnedCheckBox.isSelected()
                    );

                } else {

                    noteService.createNote(
                            title,
                            content,
                            category,
                            pinnedCheckBox.isSelected()
                    );
                }

                dialog.dispose();
                refresh();

                JOptionPane.showMessageDialog(
                        this,
                        editing
                                ? "Your secure note has been updated."
                                : "Your secure note has been created.",
                        "Secure Notes",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (IllegalArgumentException ex) {

                JOptionPane.showMessageDialog(
                        dialog,
                        ex.getMessage(),
                        "Invalid Note",
                        JOptionPane.WARNING_MESSAGE
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        dialog,
                        "Could not save your note:\n" + ex.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        footer.add(cancelButton);
        footer.add(saveButton);

        dialog.add(main, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addField(
            JPanel panel,
            String label,
            JComponent component
    ) {

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(TEXT);
        fieldLabel.setFont(new Font(
                "Segoe UI", Font.BOLD, 12
        ));

        component.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 40)
        );

        component.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(fieldLabel);
        panel.add(Box.createVerticalStrut(7));
        panel.add(component);
    }

    // =========================================================
    // SELECTED NOTE
    // =========================================================

    private SecureNote getSelectedNote() {

        int selectedRow = notesTable.getSelectedRow();

        if (selectedRow < 0) {
            return null;
        }

        int modelRow = notesTable.convertRowIndexToModel(
                selectedRow
        );

        long id = ((Number) tableModel.getValueAt(
                modelRow, 0
        )).longValue();

        for (SecureNote note : notes) {
            if (note.id() == id) {
                return note;
            }
        }

        return null;
    }

    private void editSelectedNote() {

        SecureNote note = getSelectedNote();

        if (note == null) {
            showSelectionMessage();
            return;
        }

        showNoteDialog(note);
    }

    // =========================================================
    // PIN AND UNPIN
    // =========================================================

    private void toggleSelectedPin() {

        SecureNote note = getSelectedNote();

        if (note == null) {
            showSelectionMessage();
            return;
        }

        try {

            noteService.setPinned(
                    note.id(),
                    !note.pinned()
            );

            refresh();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not update note pin status:\n"
                            + e.getMessage(),
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    private void deleteSelectedNote() {

        SecureNote note = getSelectedNote();

        if (note == null) {
            showSelectionMessage();
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this secure note?\n\n"
                        + note.title()
                        + "\n\nThis action cannot be undone.",
                "Delete Secure Note",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            noteService.deleteNote(note.id());

            refresh();

            JOptionPane.showMessageDialog(
                    this,
                    "The secure note has been deleted.",
                    "Secure Notes",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not delete the note:\n" + e.getMessage(),
                    "Delete Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showSelectionMessage() {

        JOptionPane.showMessageDialog(
                this,
                "Please select a note first.",
                "No Note Selected",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
