package com.securevault.ui;

import com.securevault.db.Database;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final Database database;
    private final SecretKey secretKey;
    private final Runnable lockAction;

    private final JPanel contentPanel;
    private final CardLayout cardLayout;

    private DashboardPanel dashboardPanel;
    private VaultPanel vaultPanel;
    private GeneratorPanel generatorPanel;
    private SecurityPanel securityPanel;
    private CategoriesPanel categoriesPanel;
    private HistoryPanel historyPanel;
    private SettingsPanel settingsPanel;

    public MainFrame(Database database, SecretKey secretKey, Runnable lockAction) {

        this.database = database;
        this.secretKey = secretKey;
        this.lockAction = lockAction;

        setTitle("SecureVault");
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.BG);

        initializePanels();
        createSidebar();

        add(contentPanel, BorderLayout.CENTER);

        showPage("Dashboard");

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                lockVault();
            }
        });
    }

    private void initializePanels() {

        dashboardPanel = new DashboardPanel(database, secretKey);
        vaultPanel = new VaultPanel(database, secretKey);
        generatorPanel = new GeneratorPanel();
        securityPanel = new SecurityPanel(database, secretKey);
        categoriesPanel = new CategoriesPanel(database, secretKey);
        historyPanel = new HistoryPanel(database);
        settingsPanel = new SettingsPanel(database, lockAction);

        contentPanel.add(dashboardPanel, "Dashboard");
        contentPanel.add(vaultPanel, "Vault");
        contentPanel.add(generatorPanel, "Generator");
        contentPanel.add(securityPanel, "Security");
        contentPanel.add(categoriesPanel, "Categories");
        contentPanel.add(historyPanel, "History");
        contentPanel.add(settingsPanel, "Settings");
    }

    private void createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(Theme.PANEL);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(25, 15, 20, 15));

        JLabel logo = new JLabel("SECUREVAULT");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 21));
        logo.setForeground(Theme.LIGHT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Password Manager");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(Theme.MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(35));

        String[] pages = {
                "Dashboard",
                "Vault",
                "Generator",
                "Security",
                "Categories",
                "History",
                "Settings"
        };

        for (String page : pages) {
            JButton button = Theme.button(page);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);

            button.addActionListener(e -> showPage(page));

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel status = new JLabel("● Vault Unlocked");
        status.setForeground(new Color(90, 190, 130));
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(status);

        add(sidebar, BorderLayout.WEST);
    }

    private void showPage(String page) {

        cardLayout.show(contentPanel, page);

        switch (page) {
            case "Dashboard" -> dashboardPanel.refresh();
            case "Vault" -> vaultPanel.refresh();
            case "Security" -> securityPanel.refresh();
            case "Categories" -> categoriesPanel.refresh();
            case "History" -> historyPanel.refresh();
        }
    }

    private void lockVault() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Lock SecureVault and return to the login screen?",
                "Lock Vault",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                database.close();
            } catch (Exception ignored) {
            }

            dispose();
            lockAction.run();
        }
    }
}
