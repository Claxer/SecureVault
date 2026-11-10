package com.securevault.model;

public record VaultEntry(
        long id,
        String title,
        String username,
        String password,
        String url,
        String category,
        String notes,
        String createdAt
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
}
