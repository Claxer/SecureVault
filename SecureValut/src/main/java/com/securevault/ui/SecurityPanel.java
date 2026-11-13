package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.model.VaultEntry;
import com.securevault.service.PasswordStrengthAnalyzer;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SecurityPanel extends JPanel {

    private final Database database;
    private final SecretKey key;

    private final JLabel totalLabel = new JLabel("0");
    private final JLabel weakLabel = new JLabel("0");
    private final JLabel reusedLabel = new JLabel("0");
    private final JLabel strongLabel = new JLabel("0");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                            "Credential",
                            "Strength",
                            "Score",
                            "Security Status",
                            "Recommendations"
                    },
                    0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable table = new JTable(tableModel);

    private final JTextArea recommendationArea = new JTextArea();

    private List<AnalysisRow> analysisRows = new ArrayList<>();

    public SecurityPanel(Database database, SecretKey key) {

        this.database = database;
        this.key = key;

        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG);

        JPanel titleArea = new JPanel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setBackground(Theme.BG);

        JLabel title = Theme.heading("Security Center");

        JLabel subtitle = new JLabel(
                "Review password strength and identify security weaknesses."
        );

        subtitle.setForeground(Theme.MUTED);

        titleArea.add(title);
        titleArea.add(Box.createVerticalStrut(5));
        titleArea.add(subtitle);

        JButton refreshButton = Theme.button("Refresh Analysis");
        refreshButton.addActionListener(e -> refresh());

        header.add(titleArea, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBackground(Theme.BG);

        JPanel statistics = new JPanel(new GridLayout(1, 4, 12, 0));
        statistics.setBackground(Theme.BG);

        statistics.add(createStatCard(
                "Total Credentials",
                totalLabel,
                Theme.FG
        ));

        statistics.add(createStatCard(
                "Weak Passwords",
                weakLabel,
                Theme.DANGER
        ));

        statistics.add(createStatCard(
                "Reused Passwords",
                reusedLabel,
                new Color(230, 180, 80)
        ));

        statistics.add(createStatCard(
                "Strong Passwords",
                strongLabel,
                Theme.SUCCESS
        ));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(Theme.BG);

        JLabel tableTitle = new JLabel("Password Security Report");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(Theme.FG);

        table.setBackground(Theme.PANEL);
        table.setForeground(Theme.FG);
        table.setGridColor(Theme.LIGHT);
        table.setSelectionBackground(Theme.ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(Theme.LIGHT);
        table.getTableHeader().setForeground(Theme.FG);
        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        table.getTableHeader().setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(55);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(280);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedRecommendations();
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(Theme.PANEL);

        JPanel reportPanel = Theme.panel();
        reportPanel.setLayout(new BorderLayout(0, 8));
        reportPanel.setBorder(new EmptyBorder(15, 18, 15, 18));

        JLabel reportTitle = new JLabel("Security Recommendations");
        reportTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        reportTitle.setForeground(Theme.FG);

        recommendationArea.setEditable(false);
        recommendationArea.setLineWrap(true);
        recommendationArea.setWrapStyleWord(true);
        recommendationArea.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        recommendationArea.setForeground(Theme.MUTED);
        recommendationArea.setBackground(Theme.PANEL);
        recommendationArea.setText(
                "Select a credential above to view its recommendations."
        );

        JScrollPane recommendationScroll =
                new JScrollPane(recommendationArea);

        recommendationScroll.setBorder(BorderFactory.createEmptyBorder());
        recommendationScroll.setPreferredSize(new Dimension(100, 100));

        reportPanel.add(reportTitle, BorderLayout.NORTH);
        reportPanel.add(recommendationScroll, BorderLayout.CENTER);

        center.add(tableTitle, BorderLayout.NORTH);
        center.add(tableScroll, BorderLayout.CENTER);
        center.add(reportPanel, BorderLayout.SOUTH);

        content.add(statistics, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);

        return content;
    }

    private JPanel createStatCard(
            String title,
            JLabel valueLabel,
            Color valueColor
    ) {

        JPanel card = Theme.panel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 17, 15, 17));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(Theme.MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 25));
        valueLabel.setForeground(valueColor);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    public void refresh() {

        try {

            List<VaultEntry> entries = database.entries(key);

            Map<String, Integer> passwordOccurrences = new HashMap<>();

            for (VaultEntry entry : entries) {

                String password = safe(entry.password());

                String normalized = password;

                if (!normalized.isEmpty()) {
                    passwordOccurrences.merge(normalized, 1, Integer::sum);
                }
            }

            analysisRows.clear();
            tableModel.setRowCount(0);

            int weakCount = 0;
            int reusedCount = 0;
            int strongCount = 0;

            for (VaultEntry entry : entries) {

                String password = safe(entry.password());

                PasswordStrengthAnalyzer.Result result =
                        PasswordStrengthAnalyzer.analyze(password);

                boolean reused =
                        passwordOccurrences.getOrDefault(password, 0) > 1;

                if (reused) {
                    reusedCount++;
                }

                if (result.score() < 45) {
                    weakCount++;
                }

                if (result.score() >= 65) {
                    strongCount++;
                }

                String status;

                if (reused) {
                    status = "Reused";
                } else if (result.score() < 45) {
                    status = "Weak";
                } else if (result.score() < 65) {
                    status = "Needs Improvement";
                } else {
                    status = "Good";
                }

                List<String> recommendations =
                        new ArrayList<>(result.suggestions());

                if (reused) {
                    recommendations.add(
                            "Use a different password for this account."
                    );
                }

                if (recommendations.isEmpty()) {
                    recommendations.add(
                            "No immediate improvements suggested."
                    );
                }

                AnalysisRow row = new AnalysisRow(
                        entry.title(),
                        result,
                        reused,
                        recommendations
                );

                analysisRows.add(row);

                String recommendationSummary =
                        String.join("; ", recommendations);

                tableModel.addRow(new Object[]{
                        entry.title(),
                        result.strength(),
                        result.score() + "/100",
                        status,
                        recommendationSummary
                });
            }

            totalLabel.setText(String.valueOf(entries.size()));
            weakLabel.setText(String.valueOf(weakCount));
            reusedLabel.setText(String.valueOf(reusedCount));
            strongLabel.setText(String.valueOf(strongCount));

            recommendationArea.setText(
                    "Security analysis completed.\n\n"
                    + "Total credentials: " + entries.size() + "\n"
                    + "Weak passwords: " + weakCount + "\n"
                    + "Reused passwords: " + reusedCount + "\n"
                    + "Strong passwords: " + strongCount + "\n\n"
                    + "Select a credential in the table to see its "
                    + "individual recommendations."
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not analyze password security.\n"
                            + e.getMessage(),
                    "Security Analysis Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showSelectedRecommendations() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0 || selectedRow >= analysisRows.size()) {
            return;
        }

        AnalysisRow row = analysisRows.get(selectedRow);

        StringBuilder report = new StringBuilder();

        report.append("Credential: ")
                .append(row.title())
                .append("\n");

        report.append("Strength: ")
                .append(row.result().strength())
                .append("\n");

        report.append("Score: ")
                .append(row.result().score())
                .append("/100\n");

        report.append("Reused password: ")
                .append(row.reused() ? "Yes" : "No")
                .append("\n\n");

        report.append("Recommendations:\n");

        for (String recommendation : row.recommendations()) {
            report.append("• ")
                    .append(recommendation)
                    .append("\n");
        }

        recommendationArea.setText(report.toString());
        recommendationArea.setCaretPosition(0);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record AnalysisRow(
            String title,
            PasswordStrengthAnalyzer.Result result,
            boolean reused,
            List<String> recommendations
    ) {
    }
}
