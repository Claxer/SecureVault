package com.securevault.ui;

import com.securevault.service.PasswordStrengthAnalyzer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneratorPanel extends JPanel {

    private final SecureRandom random = new SecureRandom();

    private JTextField passwordField;
    private JSpinner lengthSpinner;

    private JCheckBox uppercaseCheck;
    private JCheckBox lowercaseCheck;
    private JCheckBox numbersCheck;
    private JCheckBox symbolsCheck;

    private JLabel strengthLabel;
    private JLabel scoreLabel;
    private JTextArea recommendationArea;

    private static final String UPPERCASE =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String LOWERCASE =
            "abcdefghijklmnopqrstuvwxyz";

    private static final String NUMBERS =
            "0123456789";

    private static final String SYMBOLS =
            "!@#$%^&*()-_=+[]{};:,.?/";

    public GeneratorPanel() {

        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        createHeader();
        createGeneratorContent();
    }

    private void createHeader() {

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.BG);

        JLabel title = new JLabel("Password Generator");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Theme.LIGHT);

        JLabel subtitle = new JLabel(
                "Create strong and random passwords for your accounts."
        );

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);
    }

    private void createGeneratorContent() {

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Theme.BG);

        JPanel passwordCard = Theme.panel();
        passwordCard.setLayout(new BoxLayout(
                passwordCard,
                BoxLayout.Y_AXIS
        ));

        passwordCard.setBorder(
                new EmptyBorder(25, 25, 25, 25)
        );

        JLabel passwordTitle = new JLabel("Generated Password");
        passwordTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        passwordTitle.setForeground(Theme.LIGHT);

        passwordField = new JTextField();
        passwordField.setEditable(false);
        passwordField.setFont(
                new Font("Consolas", Font.BOLD, 18)
        );
        passwordField.setBackground(Theme.BG);
        passwordField.setForeground(Theme.LIGHT);
        passwordField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.MUTED),
                        new EmptyBorder(12, 12, 12, 12)
                )
        );

        passwordField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 50)
        );

        strengthLabel = new JLabel(
                "Password strength: Not generated"
        );

        strengthLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        strengthLabel.setForeground(Theme.MUTED);

        scoreLabel = new JLabel("Score: —");
        scoreLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        scoreLabel.setForeground(Theme.MUTED);

        JPanel passwordActions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );

        passwordActions.setBackground(Theme.PANEL);

        JButton generateButton = Theme.button("Generate Password");
        JButton copyButton = Theme.button("Copy Password");

        generateButton.addActionListener(e -> generatePassword());
        copyButton.addActionListener(e -> copyPassword());

        passwordActions.add(generateButton);
        passwordActions.add(copyButton);

        passwordCard.add(passwordTitle);
        passwordCard.add(Box.createVerticalStrut(12));
        passwordCard.add(passwordField);
        passwordCard.add(Box.createVerticalStrut(12));
        passwordCard.add(strengthLabel);
        passwordCard.add(Box.createVerticalStrut(5));
        passwordCard.add(scoreLabel);
        passwordCard.add(Box.createVerticalStrut(15));
        passwordCard.add(passwordActions);

        container.add(passwordCard);
        container.add(Box.createVerticalStrut(20));

        JPanel optionsCard = Theme.panel();

        optionsCard.setLayout(new BoxLayout(
                optionsCard,
                BoxLayout.Y_AXIS
        ));

        optionsCard.setBorder(
                new EmptyBorder(25, 25, 25, 25)
        );

        JLabel optionsTitle = new JLabel("Password Options");
        optionsTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 17)
        );
        optionsTitle.setForeground(Theme.LIGHT);

        JLabel lengthLabel = new JLabel("Password Length");
        lengthLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        lengthLabel.setForeground(Theme.MUTED);

        lengthSpinner = new JSpinner(
                new SpinnerNumberModel(16, 8, 64, 1)
        );

        lengthSpinner.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        lengthSpinner.setMaximumSize(
                new Dimension(120, 35)
        );

        JPanel lengthPanel = new JPanel(
                new BorderLayout(15, 0)
        );

        lengthPanel.setBackground(Theme.PANEL);
        lengthPanel.add(lengthLabel, BorderLayout.CENTER);
        lengthPanel.add(lengthSpinner, BorderLayout.EAST);

        uppercaseCheck = createOption(
                "Uppercase Letters (A-Z)", true
        );

        lowercaseCheck = createOption(
                "Lowercase Letters (a-z)", true
        );

        numbersCheck = createOption(
                "Numbers (0-9)", true
        );

        symbolsCheck = createOption(
                "Symbols (!@#$%)", true
        );

        optionsCard.add(optionsTitle);
        optionsCard.add(Box.createVerticalStrut(20));
        optionsCard.add(lengthPanel);
        optionsCard.add(Box.createVerticalStrut(20));
        optionsCard.add(uppercaseCheck);
        optionsCard.add(Box.createVerticalStrut(10));
        optionsCard.add(lowercaseCheck);
        optionsCard.add(Box.createVerticalStrut(10));
        optionsCard.add(numbersCheck);
        optionsCard.add(Box.createVerticalStrut(10));
        optionsCard.add(symbolsCheck);

        container.add(optionsCard);
        container.add(Box.createVerticalStrut(20));

        JPanel recommendationsCard = Theme.panel();

        recommendationsCard.setLayout(new BorderLayout(0, 10));
        recommendationsCard.setBorder(
                new EmptyBorder(20, 22, 20, 22)
        );

        JLabel recommendationTitle = new JLabel(
                "Password Recommendations"
        );

        recommendationTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        recommendationTitle.setForeground(Theme.LIGHT);

        recommendationArea = new JTextArea(
                "Generate a password to see its security recommendations."
        );

        recommendationArea.setEditable(false);
        recommendationArea.setLineWrap(true);
        recommendationArea.setWrapStyleWord(true);
        recommendationArea.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        recommendationArea.setBackground(Theme.PANEL);
        recommendationArea.setForeground(Theme.MUTED);

        recommendationsCard.add(
                recommendationTitle,
                BorderLayout.NORTH
        );

        recommendationsCard.add(
                recommendationArea,
                BorderLayout.CENTER
        );

        container.add(recommendationsCard);

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JCheckBox createOption(
            String text,
            boolean selected
    ) {

        JCheckBox checkBox = new JCheckBox(text, selected);

        checkBox.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        checkBox.setForeground(Theme.LIGHT);
        checkBox.setBackground(Theme.PANEL);
        checkBox.setFocusPainted(false);

        return checkBox;
    }

    private void generatePassword() {

        List<String> selectedGroups = new ArrayList<>();

        StringBuilder availableCharacters = new StringBuilder();

        if (uppercaseCheck.isSelected()) {
            selectedGroups.add(UPPERCASE);
            availableCharacters.append(UPPERCASE);
        }

        if (lowercaseCheck.isSelected()) {
            selectedGroups.add(LOWERCASE);
            availableCharacters.append(LOWERCASE);
        }

        if (numbersCheck.isSelected()) {
            selectedGroups.add(NUMBERS);
            availableCharacters.append(NUMBERS);
        }

        if (symbolsCheck.isSelected()) {
            selectedGroups.add(SYMBOLS);
            availableCharacters.append(SYMBOLS);
        }

        if (selectedGroups.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select at least one character type.",
                    "Generator Error",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int length = (Integer) lengthSpinner.getValue();

        if (selectedGroups.size() > length) {

            JOptionPane.showMessageDialog(
                    this,
                    "The password length is too short for the selected options.",
                    "Generator Error",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        List<Character> passwordCharacters = new ArrayList<>();

        // Guarantee at least one character from each selected group.
        for (String group : selectedGroups) {

            char selectedCharacter =
                    group.charAt(random.nextInt(group.length()));

            passwordCharacters.add(selectedCharacter);
        }

        // Fill the remaining password length.
        for (int i = passwordCharacters.size(); i < length; i++) {

            int index = random.nextInt(
                    availableCharacters.length()
            );

            passwordCharacters.add(
                    availableCharacters.charAt(index)
            );
        }

        // Shuffle the characters to avoid predictable group positions.
        Collections.shuffle(passwordCharacters, random);

        StringBuilder password = new StringBuilder();

        for (char character : passwordCharacters) {
            password.append(character);
        }

        String generatedPassword = password.toString();

        passwordField.setText(generatedPassword);

        updateStrength(generatedPassword);
    }

    private void copyPassword() {

        String password = passwordField.getText();

        if (password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Generate a password first.",
                    "Nothing to Copy",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        try {

            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(
                            new StringSelection(password),
                            null
                    );

            JOptionPane.showMessageDialog(
                    this,
                    "Password copied to clipboard.",
                    "Copied",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to access clipboard.",
                    "Clipboard Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateStrength(String password) {

        PasswordStrengthAnalyzer.Result result =
                PasswordStrengthAnalyzer.analyze(password);

        strengthLabel.setText(
                "Password strength: " + result.strength()
        );

        scoreLabel.setText(
                "Security score: " + result.score() + "/100"
        );

        if (result.score() < 25) {
            strengthLabel.setForeground(
                    new Color(230, 90, 90)
            );
        } else if (result.score() < 45) {
            strengthLabel.setForeground(
                    new Color(230, 130, 80)
            );
        } else if (result.score() < 65) {
            strengthLabel.setForeground(
                    new Color(230, 180, 80)
            );
        } else if (result.score() < 85) {
            strengthLabel.setForeground(
                    new Color(100, 180, 220)
            );
        } else {
            strengthLabel.setForeground(
                    new Color(90, 190, 130)
            );
        }

        StringBuilder recommendations = new StringBuilder();

        for (String suggestion : result.suggestions()) {

            recommendations.append("• ")
                    .append(suggestion)
                    .append("\n");
        }

        recommendationArea.setText(
                recommendations.toString()
        );

        recommendationArea.setCaretPosition(0);
    }
}
