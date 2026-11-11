package com.securevault.ui;

import com.securevault.db.Database;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class HistoryPanel extends JPanel {

    private final Database database;

    private JPanel historyList;
    private JLabel activityCountLabel;

    public HistoryPanel(Database database) {

        this.database = database;

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

        JLabel title = new JLabel("Activity History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Theme.LIGHT);

        JLabel subtitle = new JLabel(
                "Review recent activities performed in your vault."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);
    }

    private void createContent() {

        JPanel mainCard = Theme.panel();
        mainCard.setLayout(new BorderLayout(0, 15));
        mainCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Theme.PANEL);

        activityCountLabel = new JLabel("0 activities");
        activityCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        activityCountLabel.setForeground(Theme.MUTED);

        JButton refreshButton = Theme.button("Refresh");
        refreshButton.addActionListener(e -> refresh());

        toolbar.add(activityCountLabel, BorderLayout.WEST);
        toolbar.add(refreshButton, BorderLayout.EAST);

        historyList = new JPanel();
        historyList.setLayout(new BoxLayout(
                historyList,
                BoxLayout.Y_AXIS
        ));
        historyList.setBackground(Theme.PANEL);

        mainCard.add(toolbar, BorderLayout.NORTH);
        mainCard.add(historyList, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(mainCard);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void refresh() {

        try {

            List<String[]> activities = database.activity();

            historyList.removeAll();

            activityCountLabel.setText(
                    activities.size() +
                            (activities.size() == 1
                                    ? " activity"
                                    : " activities")
            );

            if (activities.isEmpty()) {

                JLabel emptyLabel = new JLabel(
                        "No activity records available."
                );

                emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                emptyLabel.setForeground(Theme.MUTED);

                historyList.add(emptyLabel);

            } else {

                for (int i = activities.size() - 1; i >= 0; i--) {

                    String[] activity = activities.get(i);

                    historyList.add(createActivityCard(activity));
                    historyList.add(Box.createVerticalStrut(10));
                }
            }

            historyList.revalidate();
            historyList.repaint();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load activity history:\n" + e.getMessage(),
                    "History Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel createActivityCard(String[] activity) {

        String timestamp = activity.length > 0
                ? activity[0]
                : "Unknown date";

        String description = activity.length > 1
                ? activity[1]
                : "Unknown activity";

        JPanel card = new JPanel(new BorderLayout(12, 5));
        card.setBackground(Theme.BG);
        card.setBorder(new EmptyBorder(14, 15, 14, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setPreferredSize(new Dimension(35, 35));
        iconPanel.setBackground(Theme.PANEL);

        JLabel icon = new JLabel("•");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        icon.setForeground(Theme.ACCENT);

        iconPanel.add(icon);

        JPanel information = new JPanel();
        information.setLayout(new BoxLayout(
                information,
                BoxLayout.Y_AXIS
        ));
        information.setBackground(Theme.BG);

        JLabel actionLabel = new JLabel(description);
        actionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        actionLabel.setForeground(Theme.LIGHT);

        JLabel timestampLabel = new JLabel(timestamp);
        timestampLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timestampLabel.setForeground(Theme.MUTED);

        information.add(actionLabel);
        information.add(Box.createVerticalStrut(5));
        information.add(timestampLabel);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(information, BorderLayout.CENTER);

        return card;
    }
}
