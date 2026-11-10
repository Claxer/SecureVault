package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.model.VaultEntry;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CategoriesPanel extends JPanel {

    private final Database database;
    private final SecretKey secretKey;

    private JPanel categoriesPanel;
    private JLabel totalCategoriesLabel;
    private JLabel totalCredentialsLabel;

    public CategoriesPanel(Database database, SecretKey secretKey) {

        this.database = database;
        this.secretKey = secretKey;

        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        createHeader();
        createContent();

        refresh();
    }

    private void createHeader() {

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.BG);

        JLabel title = new JLabel("Categories");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Theme.LIGHT);

        JLabel subtitle = new JLabel(
                "Organize and browse your saved credentials by category."
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

        JPanel statistics = new JPanel(new GridLayout(1, 2, 15, 0));
        statistics.setBackground(Theme.BG);
        statistics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        totalCategoriesLabel = new JLabel("0");
        totalCredentialsLabel = new JLabel("0");

        statistics.add(createStatCard(
                "Total Categories",
                totalCategoriesLabel
        ));

        statistics.add(createStatCard(
                "Total Credentials",
                totalCredentialsLabel
        ));

        content.add(statistics);
        content.add(Box.createVerticalStrut(20));

        JPanel categoryCard = Theme.panel();
        categoryCard.setLayout(new BorderLayout(0, 15));
        categoryCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel categoryHeader = new JPanel(new BorderLayout());
        categoryHeader.setBackground(Theme.PANEL);

        JLabel categoryTitle = new JLabel("Your Categories");
        categoryTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        categoryTitle.setForeground(Theme.LIGHT);

        JButton refreshButton = Theme.button("Refresh");
        refreshButton.addActionListener(e -> refresh());

        categoryHeader.add(categoryTitle, BorderLayout.WEST);
        categoryHeader.add(refreshButton, BorderLayout.EAST);

        categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BoxLayout(
                categoriesPanel,
                BoxLayout.Y_AXIS
        ));
        categoriesPanel.setBackground(Theme.PANEL);

        categoryCard.add(categoryHeader, BorderLayout.NORTH);
        categoryCard.add(categoriesPanel, BorderLayout.CENTER);

        content.add(categoryCard);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel value) {

        JPanel card = Theme.panel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 18, 15, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(Theme.MUTED);

        value.setFont(new Font("Segoe UI", Font.BOLD, 27));
        value.setForeground(Theme.LIGHT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(value);

        return card;
    }

    public void refresh() {

        try {

            List<VaultEntry> entries = database.entries(secretKey);

            Map<String, Integer> categoryCounts = new LinkedHashMap<>();

            for (VaultEntry entry : entries) {

                String category = entry.category();

                if (category == null || category.isBlank()) {
                    category = "Uncategorized";
                }

                categoryCounts.merge(category, 1, Integer::sum);
            }

            totalCategoriesLabel.setText(
                    String.valueOf(categoryCounts.size())
            );

            totalCredentialsLabel.setText(
                    String.valueOf(entries.size())
            );

            displayCategories(categoryCounts, entries);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load categories:\n" + e.getMessage(),
                    "Category Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void displayCategories(
            Map<String, Integer> categoryCounts,
            List<VaultEntry> entries
    ) {

        categoriesPanel.removeAll();

        if (categoryCounts.isEmpty()) {

            JLabel emptyLabel = new JLabel(
                    "No categories available. Add credentials to get started."
            );

            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            emptyLabel.setForeground(Theme.MUTED);

            categoriesPanel.add(emptyLabel);

        } else {

            for (Map.Entry<String, Integer> category
                    : categoryCounts.entrySet()) {

                categoriesPanel.add(createCategoryCard(
                        category.getKey(),
                        category.getValue(),
                        entries
                ));

                categoriesPanel.add(Box.createVerticalStrut(12));
            }
        }

        categoriesPanel.revalidate();
        categoriesPanel.repaint();
    }

    private JPanel createCategoryCard(
            String categoryName,
            int count,
            List<VaultEntry> entries
    ) {

        JPanel card = Theme.panel();
        card.setLayout(new BorderLayout(15, 5));
        card.setBorder(new EmptyBorder(15, 18, 15, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JPanel information = new JPanel();
        information.setLayout(new BoxLayout(
                information,
                BoxLayout.Y_AXIS
        ));
        information.setBackground(Theme.PANEL);

        JLabel name = new JLabel(categoryName);
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        name.setForeground(Theme.LIGHT);

        JLabel description = new JLabel(
                count + (count == 1 ? " credential" : " credentials")
        );
        description.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        description.setForeground(Theme.MUTED);

        information.add(name);
        information.add(Box.createVerticalStrut(5));
        information.add(description);

        JButton viewButton = Theme.button("View");

        viewButton.addActionListener(e ->
                showCategoryEntries(categoryName, entries)
        );

        card.add(information, BorderLayout.CENTER);
        card.add(viewButton, BorderLayout.EAST);

        return card;
    }

    private void showCategoryEntries(
            String categoryName,
            List<VaultEntry> entries
    ) {

        StringBuilder details = new StringBuilder();

        details.append("Category: ")
                .append(categoryName)
                .append("\n\n");

        int count = 0;

        for (VaultEntry entry : entries) {

            String category = entry.category();

            if (category == null || category.isBlank()) {
                category = "Uncategorized";
            }

            if (category.equals(categoryName)) {

                count++;

                details.append(count)
                        .append(". ")
                        .append(entry.title())
                        .append("\n");

                details.append("   Username: ")
                        .append(entry.username() == null
                                ? "Not provided"
                                : entry.username())
                        .append("\n\n");
            }
        }

        JTextArea textArea = new JTextArea(details.toString());

        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textArea.setBackground(Theme.PANEL);
        textArea.setForeground(Theme.LIGHT);
        textArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                categoryName + " Credentials",
                JOptionPane.PLAIN_MESSAGE
        );
    }
}
