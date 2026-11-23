package com.securevault.ui;

import com.securevault.db.Database;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.function.IntConsumer;

public class MainFrame extends JFrame {

    private static final Color BACKGROUND = new Color(18, 18, 20);
    private static final Color SIDEBAR = new Color(24, 24, 27);
    private static final Color PANEL = new Color(31, 31, 35);
    private static final Color HOVER = new Color(48, 48, 54);
    private static final Color ACCENT = new Color(105, 145, 255);
    private static final Color TEXT = new Color(240, 240, 245);
    private static final Color MUTED = new Color(155, 155, 165);
    private static final Color BORDER = new Color(55, 55, 62);

    private static final String AUTO_LOCK_KEY = "autoLockMinutes";
    private static final int DEFAULT_AUTO_LOCK = 5;

    private static final Preferences AUTO_LOCK_PREFS =
            Preferences.userNodeForPackage(SettingsPanel.class);

    private final Database database;
    private final SecretKey secretKey;
    private final Runnable lockAction;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final Map<String, JPanel> panels = new LinkedHashMap<>();
    private final Map<String, JButton> navigationButtons = new LinkedHashMap<>();

    private final Timer autoLockTimer;

    private AWTEventListener activityListener;
    private int autoLockMinutes;
    private boolean locking;
    private String currentPage = "Dashboard";

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
        setSize(1250, 780);
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BACKGROUND);

        autoLockTimer = new Timer(
                autoLockMinutes * 60 * 1000,
                event -> lockVault()
        );
        autoLockTimer.setRepeats(false);

        initializePanels();
        initializeInterface();
        installActivityListener();
        resetAutoLockTimer();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                cleanup();
            }
        });
    }

    // --------------------------------------------------
    // PANEL INITIALIZATION
    // --------------------------------------------------

    private void initializePanels() {
        addPage(
                "Dashboard",
                createPanel(
                        "DashboardPanel",
                        new Object[]{database, secretKey},
                        new Object[]{database},
                        new Object[]{}
                )
        );

        addPage(
                "Vault",
                createPanel(
                        "VaultPanel",
                        new Object[]{database, secretKey},
                        new Object[]{database},
                        new Object[]{}
                )
        );

        addPage(
                "Generator",
                createPanel(
                        "GeneratorPanel",
                        new Object[]{database, secretKey},
                        new Object[]{},
                        new Object[]{database}
                )
        );

        addPage(
                "Password Health",
                createPanel(
                        "PasswordHealthPanel",
                        new Object[]{database, secretKey},
                        new Object[]{database},
                        new Object[]{}
                )
        );

        addPage(
                "Secure Notes",
                createPanel(
                        "SecureNotesPanel",
                        new Object[]{database, secretKey},
                        new Object[]{database},
                        new Object[]{}
                )
        );

        addPage(
                "Security",
                createPanel(
                        "SecurityPanel",
                        new Object[]{database, secretKey},
                        new Object[]{database},
                        new Object[]{}
                )
        );

        addPage(
                "Categories",
                createPanel(
                        "CategoriesPanel",
                        new Object[]{database, secretKey},
                        new Object[]{database},
                        new Object[]{}
                )
        );

        addPage(
                "History",
                createPanel(
                        "HistoryPanel",
                        new Object[]{database, secretKey},
                        new Object[]{database},
                        new Object[]{}
                )
        );

        addPage(
                "Recycle Bin",
                createPanel(
                        "RecycleBinPanel",
                        new Object[]{database, secretKey},
                        new Object[]{database},
                        new Object[]{}
                )
        );

        IntConsumer autoLockCallback = this::setAutoLockMinutes;

        addPage(
                "Settings",
                createPanel(
                        "SettingsPanel",
                        new Object[]{
                                database,
                                (Runnable) this::lockVault,
                                autoLockCallback
                        },
                        new Object[]{database, (Runnable) this::lockVault},
                        new Object[]{database},
                        new Object[]{}
                )
        );
    }

    private void addPage(String name, JPanel panel) {
        panels.put(name, panel);
        contentPanel.add(panel, name);
    }

    private JPanel createPanel(
            String className,
            Object[]... constructorArguments
    ) {
        String fullClassName = "com.securevault.ui." + className;

        try {
            Class<?> panelClass = Class.forName(fullClassName);

            for (Object[] arguments : constructorArguments) {
                Constructor<?>[] constructors =
                        panelClass.getDeclaredConstructors();

                for (Constructor<?> constructor : constructors) {
                    if (!matchesConstructor(constructor, arguments)) {
                        continue;
                    }

                    constructor.setAccessible(true);

                    Object instance = constructor.newInstance(arguments);

                    if (instance instanceof JPanel panel) {
                        return panel;
                    }
                }
            }

            return createErrorPanel(
                    className,
                    "No compatible constructor was found."
            );

        } catch (Exception exception) {
            exception.printStackTrace();

            return createErrorPanel(
                    className,
                    exception.getClass().getSimpleName()
                            + ": "
                            + exception.getMessage()
            );
        }
    }

    private boolean matchesConstructor(
            Constructor<?> constructor,
            Object[] arguments
    ) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();

        if (parameterTypes.length != arguments.length) {
            return false;
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (arguments[i] == null) {
                if (parameterTypes[i].isPrimitive()) {
                    return false;
                }
                continue;
            }

            Class<?> parameterType = wrapPrimitive(parameterTypes[i]);

            if (!parameterType.isAssignableFrom(arguments[i].getClass())) {
                return false;
            }
        }

        return true;
    }

    private Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }

        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == boolean.class) return Boolean.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == char.class) return Character.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;

        return type;
    }

    private JPanel createErrorPanel(String panelName, String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);

        JPanel content = new JPanel();
        content.setBackground(PANEL);
        content.setBorder(new EmptyBorder(25, 30, 25, 30));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(panelName);
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel error = new JLabel("Unable to initialize this panel.");
        error.setForeground(MUTED);
        error.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea details = new JTextArea(message);
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setForeground(MUTED);
        details.setBackground(PANEL);
        details.setMaximumSize(new Dimension(500, 100));

        content.add(title);
        content.add(Box.createVerticalStrut(12));
        content.add(error);
        content.add(Box.createVerticalStrut(12));
        content.add(details);

        panel.add(content);

        return panel;
    }

    // --------------------------------------------------
    // MAIN INTERFACE
    // --------------------------------------------------

    private void initializeInterface() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        root.add(createSidebar(), BorderLayout.WEST);

        contentPanel.setBackground(BACKGROUND);
        root.add(contentPanel, BorderLayout.CENTER);

        setContentPane(root);

        showPage("Dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(SIDEBAR);
        sidebar.setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 0, 1, BORDER
                )
        );

        JPanel top = new JPanel();
        top.setBackground(SIDEBAR);
        top.setBorder(new EmptyBorder(28, 18, 20, 18));
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("SecureVault");
        logo.setForeground(TEXT);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 25));

        JLabel subtitle = new JLabel("PASSWORD MANAGER");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        top.add(logo);
        top.add(Box.createVerticalStrut(5));
        top.add(subtitle);

        JPanel navigation = new JPanel();
        navigation.setBackground(SIDEBAR);
        navigation.setBorder(new EmptyBorder(5, 12, 10, 12));
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));

        String[] pages = {
                "Dashboard",
                "Vault",
                "Generator",
                "Password Health",
                "Secure Notes",
                "Security",
                "Categories",
                "History",
                "Recycle Bin",
                "Settings"
        };

        for (String page : pages) {
            JButton button = createNavigationButton(page);

            navigationButtons.put(page, button);
            navigation.add(button);
            navigation.add(Box.createVerticalStrut(5));
        }

        JPanel bottom = new JPanel();
        bottom.setBackground(SIDEBAR);
        bottom.setBorder(new EmptyBorder(15, 12, 20, 12));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        JButton lockButton = new JButton("Lock SecureVault");
        lockButton.setFocusPainted(false);
        lockButton.setForeground(TEXT);
        lockButton.setBackground(PANEL);
        lockButton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(12, 15, 12, 15)
                )
        );
        lockButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        lockButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 45)
        );
        lockButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        lockButton.addActionListener(event -> lockVault());

        bottom.add(lockButton);

        sidebar.add(top, BorderLayout.NORTH);
        sidebar.add(navigation, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavigationButton(String page) {
        JButton button = new JButton(page);

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(MUTED);
        button.setBackground(SIDEBAR);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 15, 12, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addActionListener(event -> showPage(page));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent event) {
                if (!page.equals(currentPage)) {
                    button.setBackground(HOVER);
                    button.setOpaque(true);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent event) {
                updateNavigationSelection();
            }
        });

        return button;
    }

    // --------------------------------------------------
    // NAVIGATION
    // --------------------------------------------------

    private void showPage(String page) {
        if (!panels.containsKey(page)) {
            return;
        }

        currentPage = page;
        cardLayout.show(contentPanel, page);

        updateNavigationSelection();
        refreshPage(page);

        contentPanel.revalidate();
        contentPanel.repaint();

        resetAutoLockTimer();
    }

    private void updateNavigationSelection() {
        for (Map.Entry<String, JButton> entry :
                navigationButtons.entrySet()) {

            JButton button = entry.getValue();
            boolean selected = entry.getKey().equals(currentPage);

            button.setOpaque(true);
            button.setBackground(selected ? HOVER : SIDEBAR);
            button.setForeground(selected ? ACCENT : MUTED);
            button.setFont(
                    new Font(
                            "Segoe UI",
                            selected ? Font.BOLD : Font.PLAIN,
                            14
                    )
            );
        }
    }

    private void refreshPage(String page) {
        JPanel panel = panels.get(page);

        if (panel == null) {
            return;
        }

        if (page.equals("Dashboard")
                || page.equals("Password Health")) {

            try {
                panel.getClass()
                        .getMethod("refresh")
                        .invoke(panel);

            } catch (NoSuchMethodException ignored) {
                // This panel does not expose a refresh method.
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    // --------------------------------------------------
    // AUTOMATIC LOCK
    // --------------------------------------------------

    private void installActivityListener() {
        activityListener = event -> {
            if (locking || !isActive()) {
                return;
            }

            Object source = event.getSource();

            if (!(source instanceof Component component)) {
                return;
            }

            Window sourceWindow =
                    SwingUtilities.getWindowAncestor(component);

            if (sourceWindow == this || component == this) {
                resetAutoLockTimer();
            }
        };

        Toolkit.getDefaultToolkit().addAWTEventListener(
                activityListener,
                AWTEvent.MOUSE_EVENT_MASK
                        | AWTEvent.MOUSE_MOTION_EVENT_MASK
                        | AWTEvent.KEY_EVENT_MASK
                        | AWTEvent.MOUSE_WHEEL_EVENT_MASK
        );
    }

    private void resetAutoLockTimer() {
        if (autoLockTimer == null || locking) {
            return;
        }

        autoLockTimer.stop();

        if (autoLockMinutes > 0) {
            autoLockTimer.setInitialDelay(
                    autoLockMinutes * 60 * 1000
            );
            autoLockTimer.restart();
        }
    }

    public void setAutoLockMinutes(int minutes) {
        if (minutes != 0
                && minutes != 1
                && minutes != 5
                && minutes != 10
                && minutes != 15
                && minutes != 30) {
            return;
        }

        autoLockMinutes = minutes;

        AUTO_LOCK_PREFS.putInt(
                AUTO_LOCK_KEY,
                autoLockMinutes
        );

        resetAutoLockTimer();
    }

    public int getAutoLockMinutes() {
        return autoLockMinutes;
    }

    // --------------------------------------------------
    // MANUAL AND AUTOMATIC LOCK
    // --------------------------------------------------

    private void lockVault() {
        if (locking) {
            return;
        }

        locking = true;

        if (autoLockTimer != null) {
            autoLockTimer.stop();
        }

        if (activityListener != null) {
            Toolkit.getDefaultToolkit()
                    .removeAWTEventListener(activityListener);

            activityListener = null;
        }

        closeDatabase();

        dispose();

        if (lockAction != null) {
            SwingUtilities.invokeLater(lockAction);
        }
    }

    private void closeDatabase() {
        if (database == null) {
            return;
        }

        try {
            database.getClass()
                    .getMethod("close")
                    .invoke(database);

        } catch (NoSuchMethodException ignored) {
            // No public close() method is available.
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void cleanup() {
        if (autoLockTimer != null) {
            autoLockTimer.stop();
        }

        if (activityListener != null) {
            Toolkit.getDefaultToolkit()
                    .removeAWTEventListener(activityListener);

            activityListener = null;
        }

        if (!locking) {
            closeDatabase();
        }
    }
}
