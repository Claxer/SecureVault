package com.securevault.model;

public record SecureNote(
        long id,
        String title,
        String content,
        String category,
        String createdAt,
        String updatedAt,
        boolean pinned
) {

    public SecureNote {
        title = title == null ? "" : title;
        content = content == null ? "" : content;

        category = category == null || category.isBlank()
                ? "Personal" : category;

        createdAt = createdAt == null ? "" : createdAt;
        updatedAt = updatedAt == null ? "" : updatedAt;
    }
}
