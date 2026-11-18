package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.model.VaultEntry;
import com.securevault.service.PasswordHealthAnalyzer;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PasswordHealthPanel extends JPanel {

    private final Database database;
    private final SecretKey secretKey;

    private final PasswordHealthAnalyzer analyzer =
            new PasswordHealthAnalyzer();

    private JLabel scoreLabel;
    private JLabel totalLabel;
    private JLabel strongLabel;
    private JLabel mediumLabel;
    private JLabel weakLabel;
    private JLabel reusedLabel;

    private JLabel statusLabel;
    private JTable resultsTable;
    private DefaultTableModel tableModel;

    private static final Color BACKGROUND =
            new Color(18, 18, 20);

    private static final Color CARD =
            new Color(29, 29, 32);

    private static final Color TEXT =
            new Color(240, 240, 240);

    private static final Color MUTED =
            new Color(155, 155, 160);

    public PasswordHealthPanel(
            Database database,
            SecretKey secretKey
    ) {
        this.database = database;
        this.secretKey = secretKey;

        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(
                28, 30, 28, 30
        ));

        initializeUI();
        refresh();
    }

    private void initializeUI() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(
                titlePanel, BoxLayout.Y_AXIS
        ));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Password Health");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Monitor the strength and security of your saved passwords."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(subtitle);

        JButton refreshButton = new JButton("Refresh Analysis");
        refreshButton.setFocusPainted(false);
        refreshButton.setForeground(TEXT);
        refreshButton.setBackground(new Color(55, 55, 60));
        refreshButton.setBorder(BorderFactory.createEmptyBorder(
                11, 17, 11, 17
        ));
        refreshButton.addActionListener(e -> refresh());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(
                content, BoxLayout.Y_AXIS
        ));
        content.setOpaque(false);

        content.add(createScoreCard());
        content.add(Box.createVerticalStrut(18));
        content.add(createStatistics());
        content.add(Box.createVerticalStrut(22));
        content.add(createResultsSection());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BACKGROUND);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createScoreCard() {

        JPanel card = createCard();
        card.setLayout(new BorderLayout(20, 10));
        card.setBorder(BorderFactory.createEmptyBorder(
                22, 25, 22, 25
        ));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(
                left, BoxLayout.Y_AXIS
        ));
        left.setOpaque(false);

        JLabel heading = new JLabel("Overall Security Score");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        heading.setForeground(TEXT);

        scoreLabel = new JLabel("0/100");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 43));
        scoreLabel.setForeground(TEXT);

        statusLabel = new JLabel("Waiting for analysis");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(MUTED);

        left.add(heading);
        left.add(Box.createVerticalStrut(10));
        left.add(scoreLabel);
        left.add(Box.createVerticalStrut(5));
        left.add(statusLabel);

        card.add(left, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStatistics() {

        JPanel panel = new JPanel(new GridLayout(1, 5, 12, 0));
        panel.setOpaque(false);

        totalLabel = new JLabel("0");
        strongLabel = new JLabel("0");
        mediumLabel = new JLabel("0");
        weakLabel = new JLabel("0");
        reusedLabel = new JLabel("0");

        panel.add(createStatCard(
                "Total Passwords", totalLabel, TEXT
        ));

        panel.add(createStatCard(
                "Strong", strongLabel, new Color(100, 200, 130)
        ));

        panel.add(createStatCard(
                "Medium", mediumLabel, new Color(230, 190, 90)
        ));

        panel.add(createStatCard(
                "Weak", weakLabel, new Color(235, 105, 105)
        ));

        panel.add(createStatCard(
                "Reused", reusedLabel, new Color(220, 145, 90)
        ));

        return panel;
    }

    private JPanel createStatCard(
            String title,
            JLabel valueLabel,
            Color valueColor
    ) {

        JPanel card = createCard();
        card.setLayout(new BoxLayout(
                card, BoxLayout.Y_AXIS
        ));

        card.setBorder(BorderFactory.createEmptyBorder(
                17, 16, 17, 16
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 27));
        valueLabel.setForeground(valueColor);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);

        return card;
    }

    private JPanel createResultsSection() {

        JPanel section = createCard();
        section.setLayout(new BorderLayout(10, 15));
        section.setBorder(BorderFactory.createEmptyBorder(
                20, 20, 20, 20
        ));

        JLabel heading = new JLabel("Password Analysis");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(TEXT);

        String[] columns = {
                "Credential",
                "Category",
                "Strength",
                "Score",
                "Reused",
                "Recommendation"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultsTable.setRowHeight(36);
        resultsTable.setForeground(TEXT);
        resultsTable.setBackground(CARD);
        resultsTable.setGridColor(new Color(55, 55, 60));
        resultsTable.setSelectionBackground(
                new Color(65, 65, 72)
        );
        resultsTable.setSelectionForeground(TEXT);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF
        );

        resultsTable.getTableHeader().setBackground(
                new Color(42, 42, 46)
        );
        resultsTable.getTableHeader().setForeground(TEXT);
        resultsTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        int[] widths = {140, 110, 85, 65, 70, 270};

        for (int i = 0; i < widths.length; i++) {
            resultsTable.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(
                new Color(55, 55, 60)
        ));
        tableScroll.getViewport().setBackground(CARD);

        section.add(heading, BorderLayout.NORTH);
        section.add(tableScroll, BorderLayout.CENTER);

        return section;
    }

    private JPanel createCard() {

        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createLineBorder(
                new Color(48, 48, 52)
        ));

        return panel;
    }

    public void refresh() {

        try {

            List<VaultEntry> entries =
                    database.entries(secretKey);

            PasswordHealthAnalyzer.HealthReport report =
                    analyzer.analyze(entries);

            updateDashboard(report);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to analyze password health:\n"
                            + e.getMessage(),
                    "Analysis Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateDashboard(
            PasswordHealthAnalyzer.HealthReport report
    ) {

        scoreLabel.setText(
                report.overallScore() + "/100"
        );

        totalLabel.setText(
                String.valueOf(report.totalPasswords())
        );

        strongLabel.setText(
                String.valueOf(report.strongPasswords())
        );

        mediumLabel.setText(
                String.valueOf(report.mediumPasswords())
        );

        weakLabel.setText(
                String.valueOf(report.weakPasswords())
        );

        reusedLabel.setText(
                String.valueOf(report.reusedPasswords())
        );

        if (report.totalPasswords() == 0) {
            statusLabel.setText(
                    "Add credentials to begin your security analysis."
            );
        } else if (report.overallScore() >= 80) {
            statusLabel.setText(
                    "Your password security is looking good."
            );
        } else if (report.overallScore() >= 50) {
            statusLabel.setText(
                    "Some passwords need improvement."
            );
        } else {
            statusLabel.setText(
                    "Your vault needs security improvements."
            );
        }

        tableModel.setRowCount(0);

        for (PasswordHealthAnalyzer.PasswordResult result
                : report.results()) {

            tableModel.addRow(new Object[]{
                    result.title(),
                    result.category(),
                    result.strength(),
                    result.score() + "/5",
                    result.reused() ? "Yes" : "No",
                    result.recommendation()
            });
        }

        repaint();
    }
}
