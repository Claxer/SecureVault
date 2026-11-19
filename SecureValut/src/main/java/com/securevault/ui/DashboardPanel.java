package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.model.VaultEntry;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DashboardPanel extends JPanel {

    private final Database database;
    private final SecretKey secretKey;

    private JLabel totalEntriesValue;
    private JLabel categoriesValue;
    private JLabel activityValue;
    private JLabel favoritesValue;

    private JPanel recentEntriesPanel;
    private JPanel favoriteEntriesPanel;

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

        // =====================================================
        // STATISTICS
        // =====================================================

        JPanel statistics = new JPanel(
                new GridLayout(1, 4, 12, 0)
        );

        statistics.setBackground(Theme.BG);
        statistics.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 120)
        );

        totalEntriesValue = new JLabel("0");
        categoriesValue = new JLabel("0");
        activityValue = new JLabel("0");
        favoritesValue = new JLabel("0");

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

        statistics.add(createStatCard(
                "Favorites",
                favoritesValue,
                "Your favorite credentials"
        ));

        content.add(statistics);
        content.add(Box.createVerticalStrut(25));

        // =====================================================
        // FAVORITES AND QUICK ACCESS
        // =====================================================

        JPanel favoritesCard = Theme.panel();
        favoritesCard.setLayout(new BorderLayout(0, 15));
        favoritesCard.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        JLabel favoritesTitle = new JLabel(
                "Favorite Credentials"
        );

        favoritesTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 18)
        );

        favoritesTitle.setForeground(Theme.LIGHT);

        JLabel favoritesSubtitle = new JLabel(
                "Quick access to your starred accounts."
        );

        favoritesSubtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        favoritesSubtitle.setForeground(Theme.MUTED);

        JPanel favoritesHeader = new JPanel();
        favoritesHeader.setLayout(
                new BoxLayout(favoritesHeader, BoxLayout.Y_AXIS)
        );

        favoritesHeader.setBackground(Theme.PANEL);

        favoritesHeader.add(favoritesTitle);
        favoritesHeader.add(Box.createVerticalStrut(5));
        favoritesHeader.add(favoritesSubtitle);

        favoriteEntriesPanel = new JPanel();

        favoriteEntriesPanel.setLayout(
                new BoxLayout(
                        favoriteEntriesPanel,
                        BoxLayout.Y_AXIS
                )
        );

        favoriteEntriesPanel.setBackground(Theme.PANEL);

        favoritesCard.add(
                favoritesHeader,
                BorderLayout.NORTH
        );

        favoritesCard.add(
                favoriteEntriesPanel,
                BorderLayout.CENTER
        );

        content.add(favoritesCard);
        content.add(Box.createVerticalStrut(20));

        // =====================================================
        // RECENTLY ADDED
        // =====================================================

        JPanel recentCard = Theme.panel();

        recentCard.setLayout(
                new BorderLayout(0, 15)
        );

        recentCard.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        JLabel recentTitle = new JLabel("Recently Added");

        recentTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 18)
        );

        recentTitle.setForeground(Theme.LIGHT);

        JLabel recentSubtitle = new JLabel(
                "Your latest saved credentials."
        );

        recentSubtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        recentSubtitle.setForeground(Theme.MUTED);

        JPanel recentHeader = new JPanel();

        recentHeader.setLayout(
                new BoxLayout(recentHeader, BoxLayout.Y_AXIS)
        );

        recentHeader.setBackground(Theme.PANEL);

        recentHeader.add(recentTitle);
        recentHeader.add(Box.createVerticalStrut(5));
        recentHeader.add(recentSubtitle);

        recentEntriesPanel = new JPanel();

        recentEntriesPanel.setLayout(
                new BoxLayout(
                        recentEntriesPanel,
                        BoxLayout.Y_AXIS
                )
        );

        recentEntriesPanel.setBackground(Theme.PANEL);

        recentCard.add(
                recentHeader,
                BorderLayout.NORTH
        );

        recentCard.add(
                recentEntriesPanel,
                BorderLayout.CENTER
        );

        content.add(recentCard);

        // =====================================================
        // SCROLL PANE
        // =====================================================

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

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBorder(
                new EmptyBorder(18, 18, 18, 18)
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        titleLabel.setForeground(Theme.MUTED);

        valueLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 30)
        );

        valueLabel.setForeground(Theme.LIGHT);

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );

        descriptionLabel.setForeground(Theme.MUTED);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descriptionLabel);

        return card;
    }

    // =========================================================
    // REFRESH DASHBOARD
    // =========================================================

    public void refresh() {

        try {

            List<VaultEntry> entries = database.entries(secretKey);

            Set<String> categories = new LinkedHashSet<>();

            List<VaultEntry> favorites = new ArrayList<>();

            for (VaultEntry entry : entries) {

                String category = entry.category();

                if (category != null && !category.isBlank()) {
                    categories.add(category);
                }

                if (entry.favorite()) {
                    favorites.add(entry);
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

            favoritesValue.setText(
                    String.valueOf(favorites.size())
            );

            updateFavoriteEntries(favorites);

            updateRecentEntries(entries);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load dashboard information:\n"
                            + e.getMessage(),
                    "Dashboard Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // FAVORITE CREDENTIALS
    // =========================================================

    private void updateFavoriteEntries(
            List<VaultEntry> favorites
    ) {

        favoriteEntriesPanel.removeAll();

        if (favorites.isEmpty()) {

            JLabel emptyLabel = new JLabel(
                    "No favorite credentials yet. "
                            + "Star a credential in your Vault to see it here."
            );

            emptyLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );

            emptyLabel.setForeground(Theme.MUTED);

            favoriteEntriesPanel.add(emptyLabel);

        } else {

            for (VaultEntry entry : favorites) {

                JPanel row = new JPanel(
                        new BorderLayout(12, 5)
                );

                row.setBackground(Theme.PANEL);

                row.setBorder(
                        new EmptyBorder(12, 0, 12, 0)
                );

                JPanel information = new JPanel();

                information.setLayout(
                        new BoxLayout(
                                information,
                                BoxLayout.Y_AXIS
                        )
                );

                information.setBackground(Theme.PANEL);

                JLabel name = new JLabel(
                        "★  " + entry.title()
                );

                name.setFont(
                        new Font("Segoe UI", Font.BOLD, 14)
                );

                name.setForeground(Theme.LIGHT);

                JLabel username = new JLabel(
                        entry.username()
                );

                username.setFont(
                        new Font("Segoe UI", Font.PLAIN, 12)
                );

                username.setForeground(Theme.MUTED);

                information.add(name);
                information.add(Box.createVerticalStrut(4));
                information.add(username);

                JButton viewButton = new JButton("Quick Access");

                viewButton.setFont(
                        new Font("Segoe UI", Font.PLAIN, 11)
                );

                viewButton.setFocusPainted(false);

                viewButton.setCursor(
                        Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                        )
                );

                viewButton.addActionListener(
                        e -> showFavoriteDetails(entry)
                );

                row.add(
                        information,
                        BorderLayout.CENTER
                );

                row.add(
                        viewButton,
                        BorderLayout.EAST
                );

                favoriteEntriesPanel.add(row);

                addSeparator(favoriteEntriesPanel);
            }
        }

        favoriteEntriesPanel.revalidate();
        favoriteEntriesPanel.repaint();
    }

    // =========================================================
    // FAVORITE DETAILS
    // =========================================================

    private void showFavoriteDetails(VaultEntry entry) {

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Favorite Credential",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setSize(420, 330);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = Theme.panel();

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBorder(
                new EmptyBorder(25, 25, 25, 25)
        );

        JLabel title = new JLabel(entry.title());

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 21)
        );

        title.setForeground(Theme.LIGHT);

        JLabel username = new JLabel(
                "Username: " + entry.username()
        );

        JLabel category = new JLabel(
                "Category: "
                        + (entry.category() == null
                        || entry.category().isBlank()
                        ? "Uncategorized"
                        : entry.category())
        );

        JLabel url = new JLabel(
                "Website: "
                        + (entry.url() == null
                        || entry.url().isBlank()
                        ? "Not provided"
                        : entry.url())
        );

        JLabel password = new JLabel(
                "Password: ••••••••••••"
        );

        username.setForeground(Theme.LIGHT);
        category.setForeground(Theme.LIGHT);
        url.setForeground(Theme.LIGHT);
        password.setForeground(Theme.LIGHT);

        username.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        category.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        url.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        password.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton copyPasswordButton = new JButton(
                "Copy Password"
        );

        copyPasswordButton.setFocusPainted(false);

        copyPasswordButton.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(
                    dialog,
                    "Copy this password to your clipboard?\n\n"
                            + "The clipboard may be accessible to other applications.",
                    "Confirm Password Copy",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {

                try {

                    Toolkit.getDefaultToolkit()
                            .getSystemClipboard()
                            .setContents(
                                    new StringSelection(entry.password()),
                                    null
                            );

                    JOptionPane.showMessageDialog(
                            dialog,
                            "Password copied to clipboard.",
                            "Quick Access",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            dialog,
                            "Unable to copy password:\n"
                                    + ex.getMessage(),
                            "Clipboard Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        JButton closeButton = new JButton("Close");

        closeButton.setFocusPainted(false);

        closeButton.addActionListener(
                e -> dialog.dispose()
        );

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        buttons.setBackground(Theme.PANEL);

        buttons.add(copyPasswordButton);
        buttons.add(closeButton);

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(username);
        panel.add(Box.createVerticalStrut(10));
        panel.add(category);
        panel.add(Box.createVerticalStrut(10));
        panel.add(url);
        panel.add(Box.createVerticalStrut(10));
        panel.add(password);
        panel.add(Box.createVerticalStrut(25));
        panel.add(buttons);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // =========================================================
    // RECENTLY ADDED
    // =========================================================

    private void updateRecentEntries(
            List<VaultEntry> entries
    ) {

        recentEntriesPanel.removeAll();

        if (entries.isEmpty()) {

            JLabel emptyLabel = new JLabel(
                    "No credentials have been added yet."
            );

            emptyLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );

            emptyLabel.setForeground(Theme.MUTED);

            recentEntriesPanel.add(emptyLabel);

        } else {

            int limit = Math.min(5, entries.size());

            // Database.entries() returns newest entries first.
            for (int i = 0; i < limit; i++) {

                VaultEntry entry = entries.get(i);

                JPanel row = new JPanel(
                        new BorderLayout(10, 5)
                );

                row.setBackground(Theme.PANEL);

                row.setBorder(
                        new EmptyBorder(12, 0, 12, 0)
                );

                JPanel information = new JPanel();

                information.setLayout(
                        new BoxLayout(
                                information,
                                BoxLayout.Y_AXIS
                        )
                );

                information.setBackground(Theme.PANEL);

                JLabel name = new JLabel(entry.title());

                name.setFont(
                        new Font("Segoe UI", Font.BOLD, 14)
                );

                name.setForeground(Theme.LIGHT);

                JLabel username = new JLabel(
                        entry.username()
                );

                username.setFont(
                        new Font("Segoe UI", Font.PLAIN, 12)
                );

                username.setForeground(Theme.MUTED);

                information.add(name);
                information.add(Box.createVerticalStrut(4));
                information.add(username);

                JLabel category = new JLabel(
                        entry.category() == null
                                || entry.category().isBlank()
                                ? "Uncategorized"
                                : entry.category()
                );

                category.setFont(
                        new Font("Segoe UI", Font.PLAIN, 11)
                );

                category.setForeground(Theme.ACCENT);

                row.add(
                        information,
                        BorderLayout.CENTER
                );

                row.add(
                        category,
                        BorderLayout.EAST
                );

                recentEntriesPanel.add(row);

                addSeparator(recentEntriesPanel);
            }
        }

        recentEntriesPanel.revalidate();
        recentEntriesPanel.repaint();
    }

    // =========================================================
    // SEPARATOR
    // =========================================================

    private void addSeparator(JPanel parent) {

        JSeparator separator = new JSeparator();

        separator.setForeground(Theme.LIGHT);
        separator.setBackground(Theme.PANEL);

        parent.add(separator);
    }
}
