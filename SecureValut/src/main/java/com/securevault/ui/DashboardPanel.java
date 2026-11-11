package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.model.VaultEntry;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DashboardPanel extends JPanel {

    private final Database database;
    private final SecretKey secretKey;

    private JLabel totalEntriesValue;
    private JLabel categoriesValue;
    private JLabel activityValue;

    private JPanel recentEntriesPanel;

    public DashboardPanel(Database database, SecretKey secretKey) {

        this.database = database;
        this.secretKey = secretKey;

        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        createHeader();
        createDashboardContent();

        refresh();
    }

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBackground(Theme.BG);

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Theme.LIGHT);

        JLabel subtitle = new JLabel(
                "Overview of your password vault and account activity."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(5));
        titles.add(subtitle);

        header.add(titles, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);
    }

    private void createDashboardContent() {

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG);

        JPanel statistics = new JPanel(new GridLayout(1, 3, 15, 0));
        statistics.setBackground(Theme.BG);
        statistics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        totalEntriesValue = new JLabel("0");
        categoriesValue = new JLabel("0");
        activityValue = new JLabel("0");

        statistics.add(createStatCard(
                "Saved Credentials",
                totalEntriesValue,
                "Total entries stored in your vault"
        ));

        statistics.add(createStatCard(
                "Categories",
                categoriesValue,
                "Different credential groups"
        ));

        statistics.add(createStatCard(
                "Activity Records",
                activityValue,
                "Recorded vault activities"
        ));

        content.add(statistics);
        content.add(Box.createVerticalStrut(25));

        JPanel recentCard = Theme.panel();
        recentCard.setLayout(new BorderLayout(0, 15));
        recentCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel recentTitle = new JLabel("Recently Added");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        recentTitle.setForeground(Theme.LIGHT);

        JLabel recentSubtitle = new JLabel(
                "Your latest saved credentials."
        );
        recentSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recentSubtitle.setForeground(Theme.MUTED);

        JPanel recentHeader = new JPanel();
        recentHeader.setLayout(new BoxLayout(recentHeader, BoxLayout.Y_AXIS));
        recentHeader.setBackground(Theme.PANEL);

        recentHeader.add(recentTitle);
        recentHeader.add(Box.createVerticalStrut(5));
        recentHeader.add(recentSubtitle);

        recentEntriesPanel = new JPanel();
        recentEntriesPanel.setLayout(new BoxLayout(
                recentEntriesPanel,
                BoxLayout.Y_AXIS
        ));
        recentEntriesPanel.setBackground(Theme.PANEL);

        recentCard.add(recentHeader, BorderLayout.NORTH);
        recentCard.add(recentEntriesPanel, BorderLayout.CENTER);

        content.add(recentCard);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createStatCard(
            String title,
            JLabel valueLabel,
            String description
    ) {

        JPanel card = Theme.panel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(Theme.MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(Theme.LIGHT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descriptionLabel.setForeground(Theme.MUTED);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descriptionLabel);

        return card;
    }

    public void refresh() {

        try {

            List<VaultEntry> entries = database.entries(secretKey);

            Set<String> categories = new LinkedHashSet<>();

            for (VaultEntry entry : entries) {
                String category = entry.category();

                if (category != null && !category.isBlank()) {
                    categories.add(category);
                }
            }

            List<String[]> activities = database.activity();

            totalEntriesValue.setText(
                    String.valueOf(entries.size())
            );

            categoriesValue.setText(
                    String.valueOf(categories.size())
            );

            activityValue.setText(
                    String.valueOf(activities.size())
            );

            updateRecentEntries(entries);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load dashboard information:\n" + e.getMessage(),
                    "Dashboard Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateRecentEntries(List<VaultEntry> entries) {

        recentEntriesPanel.removeAll();

        if (entries.isEmpty()) {

            JLabel emptyLabel = new JLabel(
                    "No credentials have been added yet."
            );

            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            emptyLabel.setForeground(Theme.MUTED);

            recentEntriesPanel.add(emptyLabel);

        } else {

            int start = Math.max(0, entries.size() - 5);

            for (int i = entries.size() - 1; i >= start; i--) {

                VaultEntry entry = entries.get(i);

                JPanel row = new JPanel(new BorderLayout(10, 5));
                row.setBackground(Theme.PANEL);
                row.setBorder(new EmptyBorder(12, 0, 12, 0));

                JPanel information = new JPanel();
                information.setLayout(new BoxLayout(
                        information,
                        BoxLayout.Y_AXIS
                ));
                information.setBackground(Theme.PANEL);

                JLabel name = new JLabel(entry.title());
                name.setFont(new Font("Segoe UI", Font.BOLD, 14));
                name.setForeground(Theme.LIGHT);

                JLabel username = new JLabel(entry.username());
                username.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                username.setForeground(Theme.MUTED);

                information.add(name);
                information.add(Box.createVerticalStrut(4));
                information.add(username);

                JLabel category = new JLabel(
                        entry.category() == null
                                ? "Uncategorized"
                                : entry.category()
                );

                category.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                category.setForeground(Theme.ACCENT);

                row.add(information, BorderLayout.CENTER);
                row.add(category, BorderLayout.EAST);

                recentEntriesPanel.add(row);

                JSeparator separator = new JSeparator();
                separator.setForeground(Theme.LIGHT);
                separator.setBackground(Theme.PANEL);

                recentEntriesPanel.add(separator);
            }
        }

        recentEntriesPanel.revalidate();
        recentEntriesPanel.repaint();
    }
}
