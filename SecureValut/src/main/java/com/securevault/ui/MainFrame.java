package com.securevault.ui;

import com.securevault.db.Database;
import com.securevault.model.VaultEntry;
import com.securevault.service.PasswordHealthChecker;
import com.securevault.service.PasswordHealthChecker.HealthReport;
import com.securevault.service.PasswordHealthChecker.PasswordResult;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PasswordHealthPanel extends JPanel {

    private final Database database;
    private final SecretKey secretKey;

    private final JLabel scoreLabel;
    private final JLabel scoreDescription;
    private final JLabel totalLabel;
    private final JLabel strongLabel;
    private final JLabel mediumLabel;
    private final JLabel weakLabel;
    private final JLabel reusedLabel;
    private final JLabel commonLabel;

    private final JProgressBar scoreBar;
    private final DefaultTableModel tableModel;
    private final JTextArea recommendationsArea;

    private static final Color BG = Theme.BG;
    private static final Color PANEL = Theme.PANEL;
    private static final Color TEXT = Theme.LIGHT;
    private static final Color MUTED = Theme.MUTED;

    private static final Color GREEN = new Color(75, 190, 125);
    private static final Color YELLOW = new Color(235, 185, 75);
    private static final Color RED = new Color(235, 90, 90);
    private static final Color BLUE = new Color(90, 145, 230);

    public PasswordHealthPanel(
            Database database,
            SecretKey secretKey
    ) {

        this.database = database;
        this.secretKey = secretKey;

        setLayout(new BorderLayout(0, 18));
        setBackground(BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Password Health");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 26)
        );

        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Analyze your passwords and improve your vault security."
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        JButton refreshButton = createButton(
                "Refresh Analysis",
                BLUE
        );

        refreshButton.addActionListener(
                e -> refresh()
        );

        header.add(titlePanel, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        // SECURITY SCORE
        JPanel scorePanel = createCard();

        scorePanel.setLayout(new BorderLayout(20, 10));
        scorePanel.setBorder(
                new EmptyBorder(20, 22, 20, 22)
        );

        JPanel scoreInfo = new JPanel();

        scoreInfo.setOpaque(false);

        scoreInfo.setLayout(
                new BoxLayout(scoreInfo, BoxLayout.Y_AXIS)
        );

        JLabel scoreTitle = new JLabel(
                "Overall Security Score"
        );

        scoreTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        scoreTitle.setForeground(TEXT);

        scoreLabel = new JLabel("0/100");

        scoreLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 36)
        );

        scoreLabel.setForeground(BLUE);

        scoreDescription = new JLabel(
                "Waiting for analysis"
        );

        scoreDescription.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        scoreDescription.setForeground(MUTED);

        scoreInfo.add(scoreTitle);
        scoreInfo.add(Box.createVerticalStrut(8));
        scoreInfo.add(scoreLabel);
        scoreInfo.add(Box.createVerticalStrut(5));
        scoreInfo.add(scoreDescription);

        scoreBar = new JProgressBar(0, 100);

        scoreBar.setValue(0);
        scoreBar.setStringPainted(false);
        scoreBar.setPreferredSize(
                new Dimension(180, 12)
        );

        scoreBar.setBackground(new Color(45, 45, 50));
        scoreBar.setForeground(BLUE);
        scoreBar.setBorderPainted(false);

        JPanel progressPanel = new JPanel(
                new BorderLayout()
        );

        progressPanel.setOpaque(false);

        progressPanel.setPreferredSize(
                new Dimension(180, 35)
        );

        progressPanel.add(
                scoreBar,
                BorderLayout.CENTER
        );

        scorePanel.add(scoreInfo, BorderLayout.CENTER);
        scorePanel.add(progressPanel, BorderLayout.SOUTH);

        // STATISTICS
        JPanel statistics = new JPanel(
                new GridLayout(2, 3, 12, 12)
        );

        statistics.setOpaque(false);

        totalLabel = addStat(
                statistics,
                "Total Passwords",
                BLUE
        );

        strongLabel = addStat(
                statistics,
                "Strong Passwords",
                GREEN
        );

        mediumLabel = addStat(
                statistics,
                "Medium Passwords",
                YELLOW
        );

        weakLabel = addStat(
                statistics,
                "Weak Passwords",
                RED
        );

        reusedLabel = addStat(
                statistics,
                "Reused Passwords",
                YELLOW
        );

        commonLabel = addStat(
                statistics,
                "Common Passwords",
                RED
        );

        // ANALYSIS TABLE
        JPanel tablePanel = createCard();

        tablePanel.setLayout(
                new BorderLayout(0, 12)
        );

        tablePanel.setBorder(
                new EmptyBorder(18, 18, 18, 18)
        );

        JLabel tableTitle = new JLabel(
                "Password Analysis"
        );

        tableTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        tableTitle.setForeground(TEXT);

        String[] columns = {
                "Account",
                "Score",
                "Strength",
                "Issues"
        };

        tableModel = new DefaultTableModel(
                columns,
                0
        ) {
            @Override
            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);

        table.setBackground(PANEL);
        table.setForeground(TEXT);

        table.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(
                new Dimension(0, 0)
        );

        table.setSelectionBackground(
                new Color(55, 55, 62)
        );

        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setBackground(
                new Color(40, 40, 45)
        );

        table.getTableHeader().setForeground(MUTED);

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        table.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );

        JScrollPane tableScroll = new JScrollPane(table);

        tableScroll.setBorder(
                BorderFactory.createLineBorder(
                        new Color(55, 55, 60)
                )
        );

        tableScroll.getViewport().setBackground(PANEL);

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // RECOMMENDATIONS
        JPanel recommendationPanel = createCard();

        recommendationPanel.setLayout(
                new BorderLayout(0, 10)
        );

        recommendationPanel.setBorder(
                new EmptyBorder(18, 18, 18, 18)
        );

        JLabel recommendationTitle = new JLabel(
                "Security Recommendations"
        );

        recommendationTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        recommendationTitle.setForeground(TEXT);

        recommendationsArea = new JTextArea();

        recommendationsArea.setEditable(false);
        recommendationsArea.setLineWrap(true);
        recommendationsArea.setWrapStyleWord(true);

        recommendationsArea.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        recommendationsArea.setBackground(PANEL);
        recommendationsArea.setForeground(MUTED);

        recommendationsArea.setBorder(
                new EmptyBorder(8, 5, 8, 5)
        );

        JScrollPane recommendationScroll =
                new JScrollPane(recommendationsArea);

        recommendationScroll.setBorder(null);

        recommendationScroll.setPreferredSize(
                new Dimension(0, 100)
        );

        recommendationPanel.add(
                recommendationTitle,
                BorderLayout.NORTH
        );

        recommendationPanel.add(
                recommendationScroll,
                BorderLayout.CENTER
        );

        // MAIN CONTENT
        JPanel mainContent = new JPanel(
                new BorderLayout(0, 18)
        );

        mainContent.setOpaque(false);

        JPanel centerContent = new JPanel(
                new BorderLayout(0, 18)
        );

        centerContent.setOpaque(false);

        centerContent.add(
                statistics,
                BorderLayout.NORTH
        );

        centerContent.add(
                tablePanel,
                BorderLayout.CENTER
        );

        centerContent.add(
                recommendationPanel,
                BorderLayout.SOUTH
        );

        mainContent.add(
                scorePanel,
                BorderLayout.NORTH
        );

        mainContent.add(
                centerContent,
                BorderLayout.CENTER
        );

        add(header, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);

        showEmptyState();
    }

    // =====================================================
    // REFRESH PASSWORD HEALTH
    // =====================================================

    public void refresh() {

        try {

            List<VaultEntry> entries =
                    database.entries(secretKey);

            HealthReport report =
                    PasswordHealthChecker.analyze(entries);

            updateDashboard(report);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to analyze password health.\n"
                            + e.getMessage(),
                    "Password Health Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // UPDATE DASHBOARD
    // =====================================================

    private void updateDashboard(HealthReport report) {

        int score = report.securityScore();

        scoreLabel.setText(score + "/100");
        scoreBar.setValue(score);

        if (score >= 80) {

            scoreLabel.setForeground(GREEN);
            scoreBar.setForeground(GREEN);

            scoreDescription.setText(
                    "Your vault is looking secure."
            );

        } else if (score >= 50) {

            scoreLabel.setForeground(YELLOW);
            scoreBar.setForeground(YELLOW);

            scoreDescription.setText(
                    "Some passwords need improvement."
            );

        } else {

            scoreLabel.setForeground(RED);
            scoreBar.setForeground(RED);

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

        StringBuilder recommendations =
                new StringBuilder();

        for (String recommendation :
                report.recommendations()) {

            recommendations
                    .append("• ")
                    .append(recommendation)
                    .append("\n\n");
        }

        recommendationsArea.setText(
                recommendations.toString()
        );

        recommendationsArea.setCaretPosition(0);
    }

    // =====================================================
    // EMPTY STATE
    // =====================================================

    private void showEmptyState() {

        scoreLabel.setText("0/100");

        scoreDescription.setText(
                "Add credentials to analyze your vault."
        );

        scoreBar.setValue(0);

        totalLabel.setText("0");
        strongLabel.setText("0");
        mediumLabel.setText("0");
        weakLabel.setText("0");
        reusedLabel.setText("0");
        commonLabel.setText("0");

        tableModel.setRowCount(0);

        recommendationsArea.setText(
                "Your security recommendations will appear here."
        );
    }

    // =====================================================
    // STATISTICS CARD
    // =====================================================

    private JLabel addStat(
            JPanel parent,
            String title,
            Color accent
    ) {

        JPanel card = createCard();

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBorder(
                new EmptyBorder(14, 16, 14, 16)
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        titleLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel("0");

        valueLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );

        valueLabel.setForeground(accent);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        parent.add(card);

        return valueLabel;
    }

    // =====================================================
    // PANEL STYLE
    // =====================================================

    private JPanel createCard() {

        JPanel panel = new JPanel();

        panel.setBackground(PANEL);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(52, 52, 57)
                        ),
                        new EmptyBorder(5, 5, 5, 5)
                )
        );

        return panel;
    }

    // =====================================================
    // BUTTON STYLE
    // =====================================================

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
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                new EmptyBorder(10, 16, 10, 16)
        );

        return button;
    }
}
