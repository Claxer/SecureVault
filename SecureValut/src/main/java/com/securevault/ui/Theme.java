package com.securevault.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class Theme {

    public static final Color BG = new Color(20, 20, 22);
    public static final Color PANEL = new Color(30, 30, 33);
    public static final Color LIGHT = new Color(43, 43, 47);
    public static final Color FG = new Color(240, 240, 242);
    public static final Color MUTED = new Color(155, 155, 163);
    public static final Color ACCENT = new Color(105, 145, 255);
    public static final Color DANGER = new Color(220, 75, 75);
    public static final Color SUCCESS = new Color(70, 190, 125);

    private Theme() {
    }

    public static JPanel panel() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return panel;
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(FG);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        return label;
    }

    public static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(FG);
        button.setBackground(LIGHT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 14, 9, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void style(JComponent component) {
        component.setForeground(FG);
        component.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        if (component instanceof JTextField
                || component instanceof JPasswordField
                || component instanceof JTextArea
                || component instanceof JComboBox<?>) {

            component.setBackground(LIGHT);
            component.setBorder(new EmptyBorder(9, 10, 9, 10));
        }

        if (component instanceof JTable table) {
            table.setBackground(PANEL);
            table.setForeground(FG);
            table.setGridColor(LIGHT);
            table.setSelectionBackground(ACCENT);
            table.setSelectionForeground(Color.WHITE);
            table.setRowHeight(30);
            table.setShowVerticalLines(false);

            table.getTableHeader().setBackground(LIGHT);
            table.getTableHeader().setForeground(FG);
            table.getTableHeader().setFont(
                    new Font("Segoe UI", Font.BOLD, 12)
            );
        }

        if (component instanceof JScrollPane scrollPane) {
            scrollPane.getViewport().setBackground(PANEL);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
        }
    }
}
