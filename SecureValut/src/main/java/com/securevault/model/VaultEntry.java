package com.securevault.model;

public record VaultEntry(
        long id,
        String title,
        String username,
        String password,
        String url,
        String category,
        String notes,
        String createdAt,
        boolean favorite
) {
    public VaultEntry {
        title = title == null ? "" : title;
        username = username == null ? "" : username;
        password = password == null ? "" : password;
        url = url == null ? "" : url;
        category = category == null || category.isBlank()
                ? "Uncategorized" : category;
        notes = notes == null ? "" : notes;
        createdAt = createdAt == null ? "" : createdAt;
    }

    // Keeps compatibility with existing code using the old constructor.
    public VaultEntry(
            long id,
            String title,
            String username,
            String password,
            String url,
            String category,
            String notes,
            String createdAt
    ) {
        this(
                id,
                title,
                username,
                password,
                url,
                category,
                notes,
                createdAt,
                false
        );
    }
}
