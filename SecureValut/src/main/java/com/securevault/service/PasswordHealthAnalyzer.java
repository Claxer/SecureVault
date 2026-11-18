package com.securevault.service;

import com.securevault.model.VaultEntry;

import java.util.*;

public class PasswordHealthAnalyzer {

    public record PasswordResult(
            String title,
            String category,
            String strength,
            int score,
            String recommendation,
            boolean reused
    ) {}

    public record HealthReport(
            int overallScore,
            int totalPasswords,
            int strongPasswords,
            int mediumPasswords,
            int weakPasswords,
            int reusedPasswords,
            List<PasswordResult> results
    ) {}

    public HealthReport analyze(List<VaultEntry> entries) {

        List<PasswordResult> results = new ArrayList<>();

        Map<String, Integer> passwordCounts = new HashMap<>();

        for (VaultEntry entry : entries) {
            String password = entry.password() == null
                    ? ""
                    : entry.password();

            if (!password.isEmpty()) {
                passwordCounts.merge(password, 1, Integer::sum);
            }
        }

        int strong = 0;
        int medium = 0;
        int weak = 0;
        int reused = 0;
        int totalScore = 0;

        for (VaultEntry entry : entries) {

            String password = entry.password() == null
                    ? ""
                    : entry.password();

            int score = calculateStrength(password);

            boolean isReused = !password.isEmpty()
                    && passwordCounts.getOrDefault(password, 0) > 1;

            String strength;
            String recommendation;

            if (score >= 4) {
                strength = "Strong";
                strong++;
            } else if (score >= 2) {
                strength = "Medium";
                medium++;
            } else {
                strength = "Weak";
                weak++;
            }

            if (isReused) {
                reused++;
                recommendation = "Replace this reused password.";
            } else if (score < 2) {
                recommendation = "Create a longer, stronger password.";
            } else if (score < 4) {
                recommendation = "Improve password complexity.";
            } else {
                recommendation = "No immediate action required.";
            }

            if (password.isEmpty()) {
                recommendation = "Password is missing.";
            }

            totalScore += score;

            results.add(new PasswordResult(
                    entry.title(),
                    entry.category(),
                    strength,
                    score,
                    recommendation,
                    isReused
            ));
        }

        int overallScore = entries.isEmpty()
                ? 0
                : (int) Math.round(
                        (totalScore / (double) (entries.size() * 5)) * 100
                );

        return new HealthReport(
                overallScore,
                entries.size(),
                strong,
                medium,
                weak,
                reused,
                results
        );
    }

    private int calculateStrength(String password) {

        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[^a-zA-Z0-9].*")) score++;

        if (password.length() < 8) {
            score = Math.min(score, 1);
        }

        // Normalize to a 0-5 scale.
        return Math.min(5, score);
    }
}
