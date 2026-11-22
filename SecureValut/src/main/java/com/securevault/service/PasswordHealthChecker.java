package com.securevault.service;

import com.securevault.model.VaultEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class PasswordHealthChecker {

    private static final int MINIMUM_LENGTH = 12;

    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password",
            "password123",
            "123456",
            "12345678",
            "123456789",
            "1234567890",
            "qwerty",
            "qwerty123",
            "admin",
            "admin123",
            "letmein",
            "welcome",
            "welcome123",
            "iloveyou",
            "abc123",
            "monkey",
            "dragon",
            "111111",
            "000000",
            "changeme",
            "pass1234",
            "secret",
            "secret123"
    );

    private PasswordHealthChecker() {
    }

    // Represents the security analysis of one password.
    public record PasswordResult(
            long entryId,
            String title,
            int score,
            String strength,
            boolean weak,
            boolean reused,
            boolean common,
            boolean shortPassword,
            List<String> issues,
            List<String> recommendations
    ) {
        public PasswordResult {
            issues = List.copyOf(issues);
            recommendations = List.copyOf(recommendations);
        }
    }

    // Represents the overall password health of the vault.
    public record HealthReport(
            int totalPasswords,
            int securityScore,
            int strongPasswords,
            int mediumPasswords,
            int weakPasswords,
            int reusedPasswords,
            int commonPasswords,
            int shortPasswords,
            List<PasswordResult> results,
            List<String> recommendations
    ) {
        public HealthReport {
            results = List.copyOf(results);
            recommendations = List.copyOf(recommendations);
        }
    }

    // Analyzes all credentials in the vault.
    public static HealthReport analyze(List<VaultEntry> entries) {

        if (entries == null || entries.isEmpty()) {
            return new HealthReport(
                    0, 0, 0, 0, 0,
                    0, 0, 0,
                    List.of(),
                    List.of("Add credentials to begin your password health analysis.")
            );
        }

        Map<String, Integer> passwordFrequency = new HashMap<>();

        // Identify passwords used by multiple accounts.
        for (VaultEntry entry : entries) {

            if (entry == null || entry.password().isBlank()) {
                continue;
            }

            String password = entry.password();

            passwordFrequency.merge(password, 1, Integer::sum);
        }

        List<PasswordResult> results = new ArrayList<>();

        int totalScore = 0;
        int strong = 0;
        int medium = 0;
        int weak = 0;
        int reused = 0;
        int common = 0;
        int shortCount = 0;

        for (VaultEntry entry : entries) {

            if (entry == null) {
                continue;
            }

            String password = entry.password();

            boolean isReused =
                    !password.isBlank()
                    && passwordFrequency.getOrDefault(password, 0) > 1;

            PasswordResult result = analyzePassword(
                    entry,
                    isReused
            );

            results.add(result);

            totalScore += result.score();

            switch (result.strength()) {
                case "Strong" -> strong++;
                case "Medium" -> medium++;
                default -> weak++;
            }

            if (result.reused()) reused++;
            if (result.common()) common++;
            if (result.shortPassword()) shortCount++;
        }

        int analyzed = results.size();

        int overallScore = analyzed == 0
                ? 0
                : (int) Math.round((double) totalScore / analyzed);

        List<String> recommendations = new ArrayList<>();

        if (weak > 0) {
            recommendations.add(
                    "Improve your " + weak + " weak password(s)."
            );
        }

        if (reused > 0) {
            recommendations.add(
                    "Replace reused passwords with unique passwords."
            );
        }

        if (common > 0) {
            recommendations.add(
                    "Avoid commonly used and predictable passwords."
            );
        }

        if (shortCount > 0) {
            recommendations.add(
                    "Use passwords with at least 12 characters."
            );
        }

        if (recommendations.isEmpty()) {
            recommendations.add(
                    "Your passwords have passed the current local checks."
            );
        }

        return new HealthReport(
                analyzed,
                overallScore,
                strong,
                medium,
                weak,
                reused,
                common,
                shortCount,
                results,
                recommendations
        );
    }

    // Analyzes an individual vault entry.
    private static PasswordResult analyzePassword(
            VaultEntry entry,
            boolean reused
    ) {

        String password = entry.password();

        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (password.isBlank()) {

            issues.add("Password is empty.");

            recommendations.add(
                    "Add a secure password to this account."
            );

            return new PasswordResult(
                    entry.id(),
                    entry.title(),
                    0,
                    "Weak",
                    true,
                    reused,
                    false,
                    true,
                    issues,
                    recommendations
            );
        }

        int score = calculateStrengthScore(password);

        boolean common = isCommonPassword(password);
        boolean shortPassword = password.length() < MINIMUM_LENGTH;

        if (password.length() < 8) {
            issues.add("Password is shorter than 8 characters.");
        } else if (shortPassword) {
            issues.add("Password is shorter than 12 characters.");
        }

        if (!containsUppercase(password)) {
            issues.add("No uppercase letters.");
        }

        if (!containsLowercase(password)) {
            issues.add("No lowercase letters.");
        }

        if (!containsDigit(password)) {
            issues.add("No numbers.");
        }

        if (!containsSpecialCharacter(password)) {
            issues.add("No special characters.");
        }

        if (common) {
            issues.add("Password matches a common password.");
        }

        if (reused) {
            issues.add("Password is reused across multiple accounts.");
        }

        // Apply security penalties.
        if (common) {
            score = Math.min(score, 10);
        }

        if (reused) {
            score -= 25;
        }

        if (shortPassword) {
            score -= 15;
        }

        score = Math.max(0, Math.min(100, score));

        String strength;

        if (score >= 80) {
            strength = "Strong";
        } else if (score >= 50) {
            strength = "Medium";
        } else {
            strength = "Weak";
        }

        if (common) {
            recommendations.add(
                    "Replace this password with a unique password."
            );
        }

        if (reused) {
            recommendations.add(
                    "Create a different password for this account."
            );
        }

        if (shortPassword) {
            recommendations.add(
                    "Use at least 12 characters."
            );
        }

        if (!containsUppercase(password)
                || !containsLowercase(password)) {

            recommendations.add(
                    "Use a combination of uppercase and lowercase letters."
            );
        }

        if (!containsDigit(password)) {
            recommendations.add(
                    "Include numbers in your password."
            );
        }

        if (!containsSpecialCharacter(password)) {
            recommendations.add(
                    "Include special characters."
            );
        }

        if (recommendations.isEmpty()) {
            recommendations.add(
                    "No immediate improvements identified."
            );
        }

        return new PasswordResult(
                entry.id(),
                entry.title(),
                score,
                strength,
                score < 50,
                reused,
                common,
                shortPassword,
                issues,
                recommendations
        );
    }

    // Calculates the initial password strength score.
    private static int calculateStrengthScore(String password) {

        int score = 0;

        int length = password.length();

        if (length >= 8) score += 15;
        if (length >= 12) score += 15;
        if (length >= 16) score += 10;
        if (length >= 20) score += 5;

        if (containsUppercase(password)) score += 15;
        if (containsLowercase(password)) score += 15;
        if (containsDigit(password)) score += 15;
        if (containsSpecialCharacter(password)) score += 15;

        return Math.min(score, 100);
    }

    private static boolean containsUppercase(String password) {
        return password.chars().anyMatch(Character::isUpperCase);
    }

    private static boolean containsLowercase(String password) {
        return password.chars().anyMatch(Character::isLowerCase);
    }

    private static boolean containsDigit(String password) {
        return password.chars().anyMatch(Character::isDigit);
    }

    private static boolean containsSpecialCharacter(String password) {
        return password.chars().anyMatch(
                character -> !Character.isLetterOrDigit(character)
        );
    }

    private static boolean isCommonPassword(String password) {

        return COMMON_PASSWORDS.contains(
                password.toLowerCase(Locale.ROOT)
        );
    }
}
