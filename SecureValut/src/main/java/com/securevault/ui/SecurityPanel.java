
package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.model.VaultEntry;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityPanel extends JPanel {

    private final Database database;
    private final SecretKey key;

    private final JLabel scoreLabel = new JLabel("—");
    private final JLabel totalLabel = new JLabel("0");
    private final JLabel weakLabel = new JLabel("0");
    private final JLabel reusedLabel = new JLabel("0");

    private final DefaultListModel<String> findingsModel =
            new DefaultListModel<>();

    private final JList<String> findingsList =
            new JList<>(findingsModel);

    private final JProgressBar scoreBar = new JProgressBar(0, 100);

    public SecurityPanel(Database database, SecretKey key) {
        this.database = database;
        this.key = key;

        setLayout(new BorderLayout(0, 18));
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

        JLabel title = Theme.heading("Security Center");

        JLabel subtitle = new JLabel(
                "Review the strength and reuse of your saved passwords"
        );
        subtitle.setForeground(Theme.MUTED);

        titleArea.add(title);
        titleArea.add(Box.createVerticalStrut(5));
        titleArea.add(subtitle);

        JButton refreshButton = Theme.button("Run Analysis");
        refreshButton.setBackground(Theme.ACCENT);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> refreshData());

        header.add(titleArea, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(Theme.BG);

        JPanel top = new JPanel(new BorderLayout(15, 0));
        top.setBackground(Theme.BG);

        JPanel scoreCard = Theme.panel();
        scoreCard.setLayout(new BoxLayout(scoreCard, BoxLayout.Y_AXIS));

        JLabel scoreTitle = new JLabel("Security Score");
        scoreTitle.setForeground(Theme.MUTED);

        scoreLabel.setForeground(Theme.FG);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));

        scoreBar.setStringPainted(true);
        scoreBar.setString("Security overview");
        scoreBar.setBackground(Theme.LIGHT);
        scoreBar.setForeground(Theme.ACCENT);
        scoreBar.setBorderPainted(false);

        scoreCard.add(scoreTitle);
        scoreCard.add(Box.createVerticalStrut(10));
        scoreCard.add(scoreLabel);
        scoreCard.add(Box.createVerticalStrut(14));
        scoreCard.add(scoreBar);

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setBackground(Theme.BG);

        stats.add(createStatCard("Credentials", totalLabel));
        stats.add(createStatCard("Weak Passwords", weakLabel));
        stats.add(createStatCard("Reused Passwords", reusedLabel));

        top.add(scoreCard, BorderLayout.CENTER);
        top.add(stats, BorderLayout.SOUTH);

        JPanel findings = Theme.panel();
        findings.setLayout(new BorderLayout(0, 12));

        JLabel findingsTitle = Theme.heading("Security Findings");
        findingsTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));

        findingsList.setBackground(Theme.PANEL);
        findingsList.setForeground(Theme.FG);
        findingsList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        findingsList.setSelectionBackground(Theme.LIGHT);
        findingsList.setFixedCellHeight(32);

        JScrollPane scrollPane = new JScrollPane(findingsList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.PANEL);

        findings.add(findingsTitle, BorderLayout.NORTH);
        findings.add(scrollPane, BorderLayout.CENTER);

        content.add(top, BorderLayout.NORTH);
        content.add(findings, BorderLayout.CENTER);

        return content;
    }

    private JPanel createStatCard(String title, JLabel value) {
        JPanel card = Theme.panel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setForeground(Theme.MUTED);
        heading.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        value.setForeground(Theme.FG);
        value.setFont(new Font("Segoe UI", Font.BOLD, 25));

        card.add(heading);
        card.add(Box.createVerticalStrut(10));
        card.add(value);

        return card;
    }

    public void refresh() {
        refreshData();
    }

    public void refreshData() {
        try {
            List<VaultEntry> entries = database.entries(key);

            int weakCount = 0;
            int reusedCount = 0;

            Map<String, Integer> passwordCounts = new HashMap<>();

            findingsModel.clear();

            if (entries.isEmpty()) {
                findingsModel.addElement(
                        "No credentials have been saved yet."
                );
            }

            for (VaultEntry entry : entries) {
                String password = entry.password();

                passwordCounts.merge(password, 1, Integer::sum);

                if (isWeak(password)) {
                    weakCount++;

                    findingsModel.addElement(
                            "Weak password: " + entry.title()
                    );
                }
            }

            for (VaultEntry entry : entries) {
                if (passwordCounts.getOrDefault(
                        entry.password(), 0
                ) > 1) {

                    reusedCount++;

                    findingsModel.addElement(
                            "Reused password: " + entry.title()
                    );
                }
            }

            int total = entries.size();

            int score = total == 0
                    ? 100
                    : Math.max(
                            0,
                            100 - (weakCount * 15) - (reusedCount * 20)
                    );

            scoreLabel.setText(score + "/100");
            scoreBar.setValue(score);

            totalLabel.setText(String.valueOf(total));
            weakLabel.setText(String.valueOf(weakCount));
            reusedLabel.setText(String.valueOf(reusedCount));

            if (weakCount == 0 && reusedCount == 0 && total > 0) {
                findingsModel.addElement(
                        "No weak or reused passwords detected."
                );
            }

            if (score >= 80) {
                scoreBar.setForeground(Theme.SUCCESS);
            } else if (score >= 50) {
                scoreBar.setForeground(Theme.ACCENT);
            } else {
                scoreBar.setForeground(Theme.DANGER);
            }

            database.logEvent("Security analysis performed");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not analyze credentials:\n" + e.getMessage(),
                    "Security Analysis Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean isWeak(String password) {
        if (password == null || password.length() < 10) {
            return true;
        }

        boolean uppercase = false;
        boolean lowercase = false;
        boolean number = false;
        boolean symbol = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                uppercase = true;
            } else if (Character.isLowerCase(c)) {
                lowercase = true;
            } else if (Character.isDigit(c)) {
                number = true;
            } else {
                symbol = true;
            }
        }

        int variety = 0;

        if (uppercase) variety++;
        if (lowercase) variety++;
        if (number) variety++;
        if (symbol) variety++;

        return variety < 3;
    }
}
