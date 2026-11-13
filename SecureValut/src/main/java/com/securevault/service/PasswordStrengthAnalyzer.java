package com.securevault.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PasswordStrengthAnalyzer {

    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password",
            "password123",
            "123456",
            "12345678",
            "123456789",
            "qwerty",
            "qwerty123",
            "admin",
            "admin123",
            "letmein",
            "welcome",
            "iloveyou",
            "abc123",
            "111111",
            "000000",
            "password1"
    );

    private PasswordStrengthAnalyzer() {
    }

    public static Result analyze(String password) {

        if (password == null || password.isEmpty()) {
            return new Result(
                    0,
                    "Very Weak",
                    List.of("Enter a password to analyze.")
            );
        }

        int score = 0;
        List<String> suggestions = new ArrayList<>();

        int length = password.length();

        boolean hasLowercase = password.matches("(?s).*\\p{Ll}.*");
        boolean hasUppercase = password.matches("(?s).*\\p{Lu}.*");
        boolean hasNumber = password.matches("(?s).*\\d.*");
        boolean hasSymbol = password.matches("(?s).*[\\p{P}\\p{S}].*");

        // Password length
        if (length >= 8) {
            score += 15;
        } else {
            suggestions.add("Use at least 8 characters.");
        }

        if (length >= 12) {
            score += 10;
        } else {
            suggestions.add("Aim for at least 12 characters.");
        }

        if (length >= 16) {
            score += 10;
        }

        if (length >= 20) {
            score += 5;
        }

        // Character variety
        if (hasLowercase) {
            score += 10;
        } else {
            suggestions.add("Include lowercase letters.");
        }

        if (hasUppercase) {
            score += 10;
        } else {
            suggestions.add("Include uppercase letters.");
        }

        if (hasNumber) {
            score += 10;
        } else {
            suggestions.add("Include numbers.");
        }

        if (hasSymbol) {
            score += 15;
        } else {
            suggestions.add("Include symbols such as !, @, or #.");
        }

        // Detect common passwords
        String normalized = password.toLowerCase();

        if (COMMON_PASSWORDS.contains(normalized)) {
            score -= 35;
            suggestions.add(
                    "Avoid commonly used passwords."
            );
        }

        // Detect repeated characters
        if (hasRepeatedCharacters(password)) {
            score -= 15;
            suggestions.add(
                    "Avoid repeating the same character too often."
            );
        }

        // Detect simple sequences
        if (hasSequentialCharacters(password)) {
            score -= 15;
            suggestions.add(
                    "Avoid predictable sequences such as abc or 123."
            );
        }

        // Detect passwords containing only one character type
        if (countCharacterTypes(
                hasLowercase,
                hasUppercase,
                hasNumber,
                hasSymbol
        ) <= 1) {
            score -= 15;
            suggestions.add(
                    "Combine different types of characters."
            );
        }

        score = Math.max(0, Math.min(100, score));

        String strength;

        if (score < 25) {
            strength = "Very Weak";
        } else if (score < 45) {
            strength = "Weak";
        } else if (score < 65) {
            strength = "Fair";
        } else if (score < 85) {
            strength = "Strong";
        } else {
            strength = "Very Strong";
        }

        if (suggestions.isEmpty()) {
            suggestions.add(
                    "Good password structure. Keep it unique to this account."
            );
        }

        return new Result(
                score,
                strength,
                List.copyOf(suggestions)
        );
    }

    private static boolean hasRepeatedCharacters(String password) {

        int repeatedCount = 1;

        for (int i = 1; i < password.length(); i++) {

            if (password.charAt(i) == password.charAt(i - 1)) {
                repeatedCount++;

                if (repeatedCount >= 3) {
                    return true;
                }

            } else {
                repeatedCount = 1;
            }
        }

        return false;
    }

    private static boolean hasSequentialCharacters(String password) {

        String lower = password.toLowerCase();

        for (int i = 0; i < lower.length() - 2; i++) {

            char first = lower.charAt(i);
            char second = lower.charAt(i + 1);
            char third = lower.charAt(i + 2);

            if (second == first + 1 && third == second + 1) {
                return true;
            }

            if (second == first - 1 && third == second - 1) {
                return true;
            }
        }

        return false;
    }

    private static int countCharacterTypes(
            boolean lowercase,
            boolean uppercase,
            boolean number,
            boolean symbol
    ) {

        int count = 0;

        if (lowercase) count++;
        if (uppercase) count++;
        if (number) count++;
        if (symbol) count++;

        return count;
    }

    public record Result(
            int score,
            String strength,
            List<String> suggestions
    ) {
    }
}
