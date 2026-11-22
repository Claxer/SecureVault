package com.securevault.ui;

import com.securevault.model.VaultEntry;
import com.securevault.service.PasswordHealthChecker;
import com.securevault.service.PasswordHealthChecker.HealthReport;
import com.securevault.service.PasswordHealthChecker.PasswordResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PasswordHealthPanel extends JPanel {

    private static final Color BACKGROUND = new Color(18, 18, 20);
    private static final Color PANEL = new Color(27, 27, 30);
    private static final Color PANEL_LIGHT = new Color(36, 36, 40);
    private static final Color BORDER = new Color(52, 52, 57);
    private static final Color TEXT = new Color(240, 240, 242);
    private static final Color MUTED = new Color(155, 155, 165);
    private static final Color GREEN = new Color(75, 190, 125);
    private static final Color YELLOW = new Color(235, 185, 75);
    private static final Color RED = new Color(235, 90, 90);
    private static final Color BLUE = new Color(90, 145, 230);

    private final DefaultTableModel tableModel;
    private final JTable resultsTable;

    private final JLabel scoreLabel;
    private final JLabel scoreDescription;

    private final JLabel totalLabel;
    private final JLabel strongLabel;
    private final JLabel mediumLabel;
    private final JLabel weakLabel;
    private final JLabel reusedLabel;
    private final JLabel commonLabel;

    private final JProgressBar scoreProgress;

    private final JTextArea recommendationArea;

    private List<VaultEntry> entries = List.of();

    public PasswordHealthPanel() {

        setLayout(new BorderLayout(0, 20));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Password Health");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Monitor password strength and improve your vault security."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        JButton refreshButton = createButton(
                "Refresh Analysis",
                BLUE
        );

        refreshButton.addActionListener(e -> refreshAnalysis());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        // Score panel
        JPanel scorePanel = createPanel();

        scorePanel.setLayout(new BorderLayout(20, 10));
        scorePanel.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel scoreInformation = new JPanel();
        scoreInformation.setOpaque(false);
        scoreInformation.setLayout(new BoxLayout(
                scoreInformation,
                BoxLayout.Y_AXIS
        ));

        JLabel scoreTitle = new JLabel("Overall Security Score");
        scoreTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        scoreTitle.setForeground(TEXT);

        scoreLabel = new JLabel("0/100");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        scoreLabel.setForeground(TEXT);

        scoreDescription = new JLabel(
                "Waiting for password analysis"
        );
        scoreDescription.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scoreDescription.setForeground(MUTED);

        scoreInformation.add(scoreTitle);
        scoreInformation.add(Box.createVerticalStrut(8));
        scoreInformation.add(scoreLabel);
        scoreInformation.add(Box.createVerticalStrut(5));
        scoreInformation.add(scoreDescription);

        scoreProgress = new JProgressBar(0, 100);
        scoreProgress.setValue(0);
        scoreProgress.setStringPainted(false);
        scoreProgress.setPreferredSize(new Dimension(180, 12));
        scoreProgress.setBackground(PANEL_LIGHT);
        scoreProgress.setForeground(BLUE);
        scoreProgress.setBorderPainted(false);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setOpaque(false);
        progressPanel.setPreferredSize(new Dimension(180, 40));
        progressPanel.add(scoreProgress, BorderLayout.CENTER);

        scorePanel.add(scoreInformation, BorderLayout.CENTER);
        scorePanel.add(progressPanel, BorderLayout.SOUTH);

        // Statistics
        JPanel statisticsPanel = new JPanel(new GridLayout(2, 3, 12, 12));
        statisticsPanel.setOpaque(false);

        totalLabel = addStatCard(
                statisticsPanel, "Total Passwords", "0", BLUE
        );

        strongLabel = addStatCard(
                statisticsPanel, "Strong Passwords", "0", GREEN
        );

        mediumLabel = addStatCard(
                statisticsPanel, "Medium Passwords", "0", YELLOW
        );

        weakLabel = addStatCard(
                statisticsPanel, "Weak Passwords", "0", RED
        );

        reusedLabel = addStatCard(
                statisticsPanel, "Reused Passwords", "0", YELLOW
        );

        commonLabel = addStatCard(
                statisticsPanel, "Common Passwords", "0", RED
        );

        // Main content
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        JPanel tablePanel = createPanel();
        tablePanel.setLayout(new BorderLayout(0, 12));
        tablePanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel tableTitle = new JLabel("Password Analysis");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT);

        String[] columns = {
                "Account",
                "Security Score",
                "Strength",
                "Issues"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);

        resultsTable.setBackground(PANEL);
        resultsTable.setForeground(TEXT);
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultsTable.setRowHeight(38);
        resultsTable.setShowGrid(false);
        resultsTable.setIntercellSpacing(new Dimension(0, 0));
        resultsTable.setSelectionBackground(PANEL_LIGHT);
        resultsTable.setSelectionForeground(TEXT);
        resultsTable.setFillsViewportHeight(true);

        resultsTable.getTableHeader().setBackground(PANEL_LIGHT);
        resultsTable.getTableHeader().setForeground(MUTED);
        resultsTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        resultsTable.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean selected,
                            boolean focused,
                            int row,
                            int column
                    ) {

                        Component component =
                                super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        selected,
                                        focused,
                                        row,
                                        column
                                );

                        setBorder(new EmptyBorder(0, 10, 0, 10));

                        if (!selected) {
                            setBackground(PANEL);
                            setForeground(TEXT);
                        }

                        if (column == 2 && value != null && !selected) {

                            String strength = value.toString();

                            if (strength.equals("Strong")) {
                                setForeground(GREEN);
                            } else if (strength.equals("Medium")) {
                                setForeground(YELLOW);
                            } else {
                                setForeground(RED);
                            }
                        }

                        return component;
                    }
                };

        for (int i = 0; i < resultsTable.getColumnCount(); i++) {
            resultsTable.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(PANEL);

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Recommendations
        JPanel recommendationsPanel = createPanel();
        recommendationsPanel.setLayout(new BorderLayout(0, 10));
        recommendationsPanel.setBorder(
                new EmptyBorder(18, 18, 18, 18)
        );

        JLabel recommendationTitle = new JLabel(
                "Security Recommendations"
        );

        recommendationTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );
        recommendationTitle.setForeground(TEXT);

        recommendationArea = new JTextArea();
        recommendationArea.setEditable(false);
        recommendationArea.setLineWrap(true);
        recommendationArea.setWrapStyleWord(true);
        recommendationArea.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        recommendationArea.setBackground(PANEL);
        recommendationArea.setForeground(MUTED);
        recommendationArea.setBorder(new EmptyBorder(8, 5, 8, 5));

        JScrollPane recommendationScroll = new JScrollPane(
                recommendationArea
        );

        recommendationScroll.setBorder(null);
        recommendationScroll.setPreferredSize(
                new Dimension(0, 110)
        );

        recommendationsPanel.add(
                recommendationTitle,
                BorderLayout.NORTH
        );

        recommendationsPanel.add(
                recommendationScroll,
                BorderLayout.CENTER
        );

        centerPanel.add(tablePanel, BorderLayout.CENTER);
        centerPanel.add(recommendationsPanel, BorderLayout.SOUTH);

        // Main layout
        JPanel mainContent = new JPanel(new BorderLayout(0, 18));
        mainContent.setOpaque(false);

        mainContent.add(scorePanel, BorderLayout.NORTH);

        JPanel lowerContent = new JPanel(new BorderLayout(0, 18));
        lowerContent.setOpaque(false);

        lowerContent.add(statisticsPanel, BorderLayout.NORTH);
        lowerContent.add(centerPanel, BorderLayout.CENTER);

        mainContent.add(lowerContent, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);

        // Display initial state.
        showEmptyState();
    }

    // Refreshes the password health analysis.
    public void refreshAnalysis() {

        HealthReport report = PasswordHealthChecker.analyze(entries);

        updateDashboard(report);
    }

    // Updates the credentials used by this panel.
    public void setEntries(List<VaultEntry> entries) {

        this.entries = entries == null
                ? List.of()
                : List.copyOf(entries);

        refreshAnalysis();
    }

    private void updateDashboard(HealthReport report) {

        scoreLabel.setText(report.securityScore() + "/100");

        scoreProgress.setValue(report.securityScore());

        if (report.securityScore() >= 80) {

            scoreLabel.setForeground(GREEN);
            scoreProgress.setForeground(GREEN);

            scoreDescription.setText("Your vault is looking secure.");

        } else if (report.securityScore() >= 50) {

            scoreLabel.setForeground(YELLOW);
            scoreProgress.setForeground(YELLOW);

            scoreDescription.setText(
                    "Some passwords need improvement."
            );

        } else {

            scoreLabel.setForeground(RED);
            scoreProgress.setForeground(RED);

            scoreDescription.setText(
                    "Your vault needs security improvements."
            );
        }

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

        commonLabel.setText(
                String.valueOf(report.commonPasswords())
        );

        tableModel.setRowCount(0);

        for (PasswordResult result : report.results()) {

            String issues = result.issues().isEmpty()
                    ? "No issues detected"
                    : String.join(", ", result.issues());

            tableModel.addRow(new Object[]{
                    result.title(),
                    result.score() + "/100",
                    result.strength(),
                    issues
            });
        }

        StringBuilder recommendations = new StringBuilder();

        for (String recommendation : report.recommendations()) {

            recommendations
                    .append("• ")
                    .append(recommendation)
                    .append("\n\n");
        }

        recommendationArea.setText(
                recommendations.toString()
        );
    }

    private void showEmptyState() {

        scoreLabel.setText("0/100");
        scoreDescription.setText(
                "Add credentials to analyze your vault."
        );

        scoreProgress.setValue(0);

        tableModel.setRowCount(0);

        recommendationArea.setText(
                "Your password health recommendations will appear here."
        );
    }

    private JLabel addStatCard(
            JPanel parent,
            String title,
            String value,
            Color accent
    ) {

        JPanel card = createPanel();

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        titleLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 24)
        );
        valueLabel.setForeground(accent);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        parent.add(card);

        return valueLabel;
    }

    private JPanel createPanel() {

        JPanel panel = new JPanel();

        panel.setBackground(PANEL);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(5, 5, 5, 5)
                )
        );

        return panel;
    }

    private JButton createButton(
            String text,
            Color background
    ) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setBorder(
                new EmptyBorder(10, 16, 10, 16)
        );

        return button;
    }
}
