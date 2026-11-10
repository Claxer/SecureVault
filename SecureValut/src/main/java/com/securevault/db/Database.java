package com.securevault.db;

import com.securevault.crypto.CryptoService;
import com.securevault.model.VaultEntry;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private final Connection connection;

    public Database() {
        try {
            Path directory = Path.of(
                    System.getProperty("user.home"),
                    ".securevault"
            );

            Files.createDirectories(directory);

            Path databasePath = directory.resolve("securevault.db");

            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + databasePath
            );

            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA busy_timeout = 5000");
            }

            createTables();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not initialize SecureVault database.", e
            );
        }
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE IF NOT EXISTS vault_settings (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    salt TEXT NOT NULL,
                    verifier TEXT NOT NULL
                )
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS credentials (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    username TEXT NOT NULL,
                    password TEXT NOT NULL,
                    url TEXT,
                    category TEXT,
                    notes TEXT,
                    created_at TEXT NOT NULL
                )
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS activity (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT NOT NULL,
                    description TEXT NOT NULL
                )
            """);
        }
    }

    public boolean isInitialized() {
        String sql = "SELECT COUNT(*) FROM vault_settings";

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            return result.next() && result.getInt(1) > 0;

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public SecretKey initializeMasterPassword(char[] password) {
        if (password == null || password.length < 10) {
            throw new IllegalArgumentException(
                    "Master password must contain at least 10 characters."
            );
        }

        if (isInitialized()) {
            throw new IllegalStateException("Vault is already initialized.");
        }

        byte[] salt = CryptoService.generateSalt();
        SecretKey key = CryptoService.deriveKey(password, salt);

        String sql = """
            INSERT INTO vault_settings (id, salt, verifier)
            VALUES (1, ?, ?)
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    java.util.Base64.getEncoder().encodeToString(salt)
            );

            statement.setString(
                    2,
                    CryptoService.createVerifier(key)
            );

            statement.executeUpdate();

            logEvent("Vault master password created");

            return key;

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public SecretKey unlockMasterPassword(char[] password) {
        String sql = """
            SELECT salt, verifier
            FROM vault_settings
            WHERE id = 1
        """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            if (!result.next()) {
                throw new IllegalStateException("Vault has not been initialized.");
            }

            byte[] salt = java.util.Base64.getDecoder().decode(
                    result.getString("salt")
            );

            String verifier = result.getString("verifier");

            SecretKey key = CryptoService.deriveKey(password, salt);

            if (!CryptoService.verifyKey(key, verifier)) {
                throw new SecurityException("Incorrect master password.");
            }

            logEvent("Vault unlocked");

            return key;

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public List<VaultEntry> entries(SecretKey key) {
        List<VaultEntry> entries = new ArrayList<>();

        String sql = """
            SELECT *
            FROM credentials
            ORDER BY id DESC
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                entries.add(new VaultEntry(
                        result.getLong("id"),
                        decryptField(result.getString("title"), key),
                        decryptField(result.getString("username"), key),
                        decryptField(result.getString("password"), key),
                        decryptField(result.getString("url"), key),
                        decryptField(result.getString("category"), key),
                        decryptField(result.getString("notes"), key),
                        result.getString("created_at")
                ));
            }

            return entries;

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public void add(VaultEntry entry, SecretKey key) {
        String sql = """
            INSERT INTO credentials
            (title, username, password, url, category, notes, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, encryptField(entry.title(), key));
            statement.setString(2, encryptField(entry.username(), key));
            statement.setString(3, encryptField(entry.password(), key));
            statement.setString(4, encryptField(entry.url(), key));
            statement.setString(5, encryptField(entry.category(), key));
            statement.setString(6, encryptField(entry.notes(), key));

            statement.setString(
                    7,
                    entry.createdAt().isBlank()
                            ? LocalDateTime.now().toString()
                            : entry.createdAt()
            );

            statement.executeUpdate();

            logEvent("Credential added: " + entry.title());

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public void update(VaultEntry entry, SecretKey key) {
        String sql = """
            UPDATE credentials
            SET title = ?, username = ?, password = ?,
                url = ?, category = ?, notes = ?
            WHERE id = ?
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, encryptField(entry.title(), key));
            statement.setString(2, encryptField(entry.username(), key));
            statement.setString(3, encryptField(entry.password(), key));
            statement.setString(4, encryptField(entry.url(), key));
            statement.setString(5, encryptField(entry.category(), key));
            statement.setString(6, encryptField(entry.notes(), key));
            statement.setLong(7, entry.id());

            statement.executeUpdate();

            logEvent("Credential updated: " + entry.title());

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public void delete(long id) {
        String title = "ID " + id;

        String lookup = "SELECT title FROM credentials WHERE id = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(lookup)) {

            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    title = "credential ID " + id;
                }
            }

        } catch (SQLException e) {
            throw databaseError(e);
        }

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             "DELETE FROM credentials WHERE id = ?"
                     )) {

            statement.setLong(1, id);
            statement.executeUpdate();

            logEvent("Credential deleted: " + title);

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public List<String[]> activity() {
        List<String[]> records = new ArrayList<>();

        String sql = """
            SELECT timestamp, description
            FROM activity
            ORDER BY id ASC
        """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                records.add(new String[]{
                        result.getString("timestamp"),
                        result.getString("description")
                });
            }

            return records;

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public void logEvent(String description) {
        String sql = """
            INSERT INTO activity (timestamp, description)
            VALUES (?, ?)
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, LocalDateTime.now().toString());
            statement.setString(2, description);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private String encryptField(String value, SecretKey key) {
        return CryptoService.encrypt(
                value == null ? "" : value,
                key
        );
    }

    private String decryptField(String value, SecretKey key) {
        return CryptoService.decrypt(
                value == null ? "" : value,
                key
        );
    }

    private IllegalStateException databaseError(SQLException e) {
        return new IllegalStateException(
                "Database operation failed: " + e.getMessage(), e
        );
    }

    public void close() {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }
}
