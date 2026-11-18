package com.securevault.service;

import com.securevault.db.Database;
import com.securevault.model.SecureNote;

import javax.crypto.SecretKey;
import java.util.List;

public class NoteService {

    private final Database database;
    private final SecretKey secretKey;

    public NoteService(Database database, SecretKey secretKey) {
        if (database == null || secretKey == null) {
            throw new IllegalArgumentException(
                    "Database and encryption key are required."
            );
        }

        this.database = database;
        this.secretKey = secretKey;
    }

    public List<SecureNote> getAllNotes() {
        return database.secureNotes(secretKey);
    }

    public long createNote(
            String title,
            String content,
            String category,
            boolean pinned
    ) {
        validate(title, content);

        SecureNote note = new SecureNote(
                0,
                title.trim(),
                content,
                category,
                "",
                "",
                pinned
        );

        return database.addSecureNote(note, secretKey);
    }

    public void updateNote(
            long id,
            String title,
            String content,
            String category,
            boolean pinned
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid note ID."
            );
        }

        validate(title, content);

        SecureNote note = new SecureNote(
                id,
                title.trim(),
                content,
                category,
                "",
                "",
                pinned
        );

        database.updateSecureNote(note, secretKey);
    }

    public void deleteNote(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid note ID."
            );
        }

        database.deleteSecureNote(id);
    }

    public void setPinned(long id, boolean pinned) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid note ID."
            );
        }

        database.setSecureNotePinned(id, pinned);
    }

    private void validate(String title, String content) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter a note title."
            );
        }

        if (title.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Note title cannot exceed 150 characters."
            );
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter some note content."
            );
        }

        if (content.length() > 1_000_000) {
            throw new IllegalArgumentException(
                    "Note content cannot exceed 1 MB of text."
            );
        }
    }
}
