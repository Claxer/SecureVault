package com.securevault.ui;

import com.securevault.db.Database;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.util.prefs.Preferences;

public class MainFrame extends JFrame {

    private static final Preferences AUTO_LOCK_PREFS =
            Preferences.userNodeForPackage(SettingsPanel.class);

    private static final String AUTO_LOCK_KEY = "autoLockMinutes";
    private static final int DEFAULT_AUTO_LOCK = 5;

    private int autoLockMinutes;

    private final Database database;
    private final SecretKey secretKey;
    private final Runnable lockAction;

    private final JPanel contentPanel;
    private final CardLayout cardLayout;

    private final Timer inactivityTimer;
    private final AWTEventListener activityListener;

    private boolean locked = false;

    private DashboardPanel dashboardPanel;
    private VaultPanel vaultPanel;
    private GeneratorPanel generatorPanel;
    private SecurityPanel securityPanel;
    private CategoriesPanel categoriesPanel;
    private HistoryPanel historyPanel;
    private SettingsPanel settingsPanel;
    private PasswordHealthPanel passwordHealthPanel;
    private SecureNotesPanel secureNotesPanel;
    private RecycleBinPanel recycleBinPanel;

    public MainFrame(
            Database database,
            SecretKey secretKey,
            Runnable lockAction
    ) {

        this.database = database;
        this.secretKey = secretKey;
        this.lockAction = lockAction;

        this.autoLockMinutes = AUTO_LOCK_PREFS.getInt(
                AUTO_LOCK_KEY,
                DEFAULT_AUTO_LOCK
        );

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

        inactivityTimer = new Timer(
                Math.max(1000, autoLockMinutes * 60 * 1000),
                e -> lockVaultAutomatically()
        );

        inactivityTimer.setRepeats(false);

        if (autoLockMinutes == 0) {
            inactivityTimer.stop();
        }

        activityListener = event -> {
            if (belongsToVault(event.getSource())) {
                resetInactivityTimer();
            }
        };

        Toolkit.getDefaultToolkit().addAWTEventListener(
                activityListener,
                AWTEvent.MOUSE_EVENT_MASK
                        | AWTEvent.MOUSE_MOTION_EVENT_MASK
                        | AWTEvent.MOUSE_WHEEL_EVENT_MASK
                        | AWTEvent.KEY_EVENT_MASK
        );

        resetInactivityTimer();

        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                lockVault();
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                inactivityTimer.stop();

                Toolkit.getDefaultToolkit()
                        .removeAWTEventListener(activityListener);
            }
        });
    }

    // =========================================================
    // INITIALIZE PANELS
    // =========================================================

    private void initializePanels() {

        dashboardPanel = new DashboardPanel(database, secretKey);

        vaultPanel = new VaultPanel(database, secretKey);

        generatorPanel = new GeneratorPanel();

        securityPanel = new SecurityPanel(database, secretKey);

        categoriesPanel = new CategoriesPanel(database, secretKey);

        historyPanel = new HistoryPanel(database);

        settingsPanel = new SettingsPanel(
                database,
                lockAction,
                this::setAutoLockMinutes
        );

        passwordHealthPanel = new PasswordHealthPanel(
                database,
                secretKey
        );

        secureNotesPanel = new SecureNotesPanel(
                database,
                secretKey
        );

        recycleBinPanel = new RecycleBinPanel(
                database,
                secretKey
        );

        contentPanel.add(dashboardPanel, "Dashboard");

        contentPanel.add(vaultPanel, "Vault");

        contentPanel.add(generatorPanel, "Generator");

        contentPanel.add(securityPanel, "Security");

        contentPanel.add(categoriesPanel, "Categories");

        contentPanel.add(historyPanel, "History");

        contentPanel.add(settingsPanel, "Settings");

        contentPanel.add(passwordHealthPanel, "Password Health");

        contentPanel.add(secureNotesPanel, "Secure Notes");

        contentPanel.add(recycleBinPanel, "Recycle Bin");
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private void createSidebar() {

        JPanel sidebar = new JPanel();

        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(Theme.PANEL);

        sidebar.setLayout(new BoxLayout(
                sidebar,
                BoxLayout.Y_AXIS
        ));

        sidebar.setBorder(BorderFactory.createEmptyBorder(
                25, 15, 20, 15
        ));

        JLabel logo = new JLabel("SECUREVAULT");

        logo.setFont(new Font(
                "Segoe UI",
                Font.BOLD,
                21
        ));

        logo.setForeground(Theme.LIGHT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Password Manager");

        subtitle.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                11
        ));

        subtitle.setForeground(Theme.MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(35));

        String[] pages = {
                "Dashboard",
                "Vault",
                "Recycle Bin",
                "Generator",
                "Password Health",
                "Secure Notes",
                "Security",
                "Categories",
                "History",
                "Settings"
        };

        for (String page : pages) {

            JButton button = Theme.button(page);

            button.setHorizontalAlignment(
                    SwingConstants.LEFT
            );

            button.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 42)
            );

            button.setAlignmentX(
                    Component.LEFT_ALIGNMENT
            );

            button.addActionListener(e -> showPage(page));

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel status = new JLabel("● Vault Unlocked");

        status.setForeground(
                new Color(90, 190, 130)
        );

        status.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                12
        ));

        status.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(status);

        JScrollPane sidebarScroll = new JScrollPane(sidebar);

        sidebarScroll.setPreferredSize(new Dimension(235, 0));
        sidebarScroll.setBorder(null);
        sidebarScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        sidebarScroll.getViewport().setBackground(Theme.PANEL);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(sidebarScroll, BorderLayout.WEST);
    }

    // =========================================================
    // PAGE NAVIGATION
    // =========================================================

    private void showPage(String page) {

        cardLayout.show(contentPanel, page);

        switch (page) {

            case "Dashboard" ->
                    dashboardPanel.refresh();

            case "Vault" ->
                    vaultPanel.refresh();

            case "Security" ->
                    securityPanel.refresh();

            case "Categories" ->
                    categoriesPanel.refresh();

            case "History" ->
                    historyPanel.refresh();

            case "Password Health" ->
                    passwordHealthPanel.refresh();

            case "Secure Notes" ->
                    secureNotesPanel.refresh();

            case "Recycle Bin" ->
                    recycleBinPanel.refresh();
        }
    }

    // =========================================================
    // INACTIVITY DETECTION
    // =========================================================

    private boolean belongsToVault(Object source) {

        if (!(source instanceof Component component)) {
            return false;
        }

        Window window = component instanceof Window
                ? (Window) component
                : SwingUtilities.getWindowAncestor(component);

        while (window != null) {

            if (window == this) {
                return true;
            }

            window = window.getOwner();
        }

        return false;
    }

    private void resetInactivityTimer() {

        if (locked || inactivityTimer == null) {
            return;
        }

        if (autoLockMinutes <= 0) {
            inactivityTimer.stop();
            return;
        }

        inactivityTimer.setInitialDelay(
                autoLockMinutes * 60 * 1000
        );

        inactivityTimer.restart();
    }

    private void setAutoLockMinutes(int minutes) {

        if (minutes < 0) {
            return;
        }

        autoLockMinutes = minutes;

        if (locked || inactivityTimer == null) {
            return;
        }

        inactivityTimer.stop();

        if (autoLockMinutes > 0) {

            inactivityTimer.setInitialDelay(
                    autoLockMinutes * 60 * 1000
            );

            inactivityTimer.restart();
        }
    }

    // =========================================================
    // MANUAL LOCK
    // =========================================================

    private void lockVault() {

        if (locked) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Lock SecureVault and return to the login screen?",
                "Lock Vault",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            closeAndLock();
        }
    }

    // =========================================================
    // AUTOMATIC LOCK
    // =========================================================

    private void lockVaultAutomatically() {

        if (locked) {
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "SecureVault has been locked due to inactivity.\n"
                        + "Enter your master password to continue.",
                "Automatic Vault Lock",
                JOptionPane.INFORMATION_MESSAGE
        );

        closeAndLock();
    }

    // =========================================================
    // CLOSE AND RETURN TO LOGIN
    // =========================================================

    private void closeAndLock() {

        if (locked) {
            return;
        }

        locked = true;

        inactivityTimer.stop();

        Toolkit.getDefaultToolkit()
                .removeAWTEventListener(activityListener);

        try {
            database.close();
        } catch (Exception ignored) {
        }

        dispose();

        lockAction.run();
    }
}
