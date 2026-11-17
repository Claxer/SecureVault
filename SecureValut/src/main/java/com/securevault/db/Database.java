package com.securevault.db;

import com.securevault.crypto.CryptoService;
import com.securevault.model.VaultEntry;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class Database {

    private final Connection connection;
    private final Path databasePath;

    public Database() {
        try {
            Path directory = Path.of(
                    System.getProperty("user.home"),
                    ".securevault"
            );

            Files.createDirectories(directory);

            databasePath = directory.resolve("securevault.db");

            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + databasePath
            );

            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA busy_timeout = 5000");
            }

            createTables();
            migrateDatabase();

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

    /*
     * Adds new database columns without deleting existing records.
     */
    private void migrateDatabase() throws SQLException {
        addColumnIfMissing(
                "credentials",
                "favorite",
                "INTEGER NOT NULL DEFAULT 0"
        );
    }

    private void addColumnIfMissing(
            String table,
            String column,
            String definition
    ) throws SQLException {

        boolean columnExists = false;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "PRAGMA table_info(" + table + ")"
             )) {

            while (result.next()) {
                if (column.equalsIgnoreCase(result.getString("name"))) {
                    columnExists = true;
                    break;
                }
            }
        }

        if (!columnExists) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(
                        "ALTER TABLE " + table
                                + " ADD COLUMN " + column
                                + " " + definition
                );
            }
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
            throw new IllegalStateException(
                    "Vault is already initialized."
            );
        }

        byte[] salt = CryptoService.generateSalt();

        try {
            SecretKey key = CryptoService.deriveKey(password, salt);

            String sql = """
                INSERT INTO vault_settings (id, salt, verifier)
                VALUES (1, ?, ?)
            """;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(
                        1,
                        Base64.getEncoder().encodeToString(salt)
                );

                statement.setString(
                        2,
                        CryptoService.createVerifier(key)
                );

                statement.executeUpdate();

                logEvent("Vault master password created");

                return key;
            }

        } catch (SQLException e) {
            throw databaseError(e);

        } finally {
            Arrays.fill(salt, (byte) 0);
        }
    }

    public SecretKey unlockMasterPassword(char[] password) {
        if (password == null) {
            throw new IllegalArgumentException(
                    "Master password is required."
            );
        }

        String sql = """
            SELECT salt, verifier
            FROM vault_settings
            WHERE id = 1
        """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            if (!result.next()) {
                throw new IllegalStateException(
                        "Vault has not been initialized."
                );
            }

            byte[] salt = Base64.getDecoder().decode(
                    result.getString("salt")
            );

            try {
                String verifier = result.getString("verifier");

                SecretKey key = CryptoService.deriveKey(password, salt);

                if (!CryptoService.verifyKey(key, verifier)) {
                    throw new SecurityException(
                            "Incorrect master password."
                    );
                }

                logEvent("Vault unlocked");

                return key;

            } finally {
                Arrays.fill(salt, (byte) 0);
            }

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

        try (PreparedStatement statement =
                     connection.prepareStatement(sql);
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
                        result.getString("created_at"),
                        result.getInt("favorite") == 1
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
            (
                title, username, password,
                url, category, notes, created_at, favorite
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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

            statement.setInt(8, entry.favorite() ? 1 : 0);

            statement.executeUpdate();

            logEvent("Credential added");

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public void update(VaultEntry entry, SecretKey key) {
        String sql = """
            UPDATE credentials
            SET title = ?, username = ?, password = ?,
                url = ?, category = ?, notes = ?, favorite = ?
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
            statement.setInt(7, entry.favorite() ? 1 : 0);
            statement.setLong(8, entry.id());

            int updated = statement.executeUpdate();

            if (updated > 0) {
                logEvent("Credential updated");
            }

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM credentials WHERE id = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            int deleted = statement.executeUpdate();

            if (deleted > 0) {
                logEvent("Credential deleted");
            }

        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    public void setFavorite(long id, boolean favorite) {
        String sql = """
            UPDATE credentials
            SET favorite = ?
            WHERE id = ?
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, favorite ? 1 : 0);
            statement.setLong(2, id);

            int updated = statement.executeUpdate();

            if (updated > 0) {
                logEvent(
                        favorite
                                ? "Credential added to favorites"
                                : "Credential removed from favorites"
                );
            }

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

    /*
     * Creates a consistent SQLite snapshot for backups.
     */
    public void createSnapshot(Path destination) {

        if (destination == null) {
            throw new IllegalArgumentException(
                    "Backup destination cannot be empty."
            );
        }

        Path absolutePath = destination.toAbsolutePath().normalize();

        try {
            Path parent = absolutePath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (Files.exists(absolutePath)) {
                throw new IllegalStateException(
                        "The snapshot destination already exists."
                );
            }

            String safePath = absolutePath.toString().replace("'", "''");

            try (Statement statement = connection.createStatement()) {
                statement.execute(
                        "VACUUM INTO '" + safePath + "'"
                );
            }

        } catch (SQLException e) {
            throw databaseError(e);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not create the database snapshot.", e
            );
        }
    }

    /*
     * Validates a SQLite database before restoration.
     */
    private static void validateDatabaseFile(Path file)
            throws IOException {

        if (file == null || !Files.isRegularFile(file)) {
            throw new IOException(
                    "The database backup file does not exist."
            );
        }

        try (Connection testConnection = DriverManager.getConnection(
                "jdbc:sqlite:" + file.toAbsolutePath()
        )) {

            try (Statement statement = testConnection.createStatement();
                 ResultSet result = statement.executeQuery(
                         "PRAGMA integrity_check"
                 )) {

                boolean foundResult = false;

                while (result.next()) {
                    foundResult = true;

                    if (!"ok".equalsIgnoreCase(result.getString(1))) {
                        throw new IOException(
                                "Database integrity check failed: "
                                        + result.getString(1)
                        );
                    }
                }

                if (!foundResult) {
                    throw new IOException(
                            "Database integrity check returned no result."
                    );
                }
            }

            try (Statement statement = testConnection.createStatement()) {

                statement.executeQuery(
                        "SELECT id, salt, verifier FROM vault_settings LIMIT 0"
                ).close();

                statement.executeQuery("""
                        SELECT title, username, password, url,
                               category, notes, created_at
                        FROM credentials
                        LIMIT 0
                        """).close();

                statement.executeQuery(
                        "SELECT timestamp, description FROM activity LIMIT 0"
                ).close();

                try (ResultSet result = statement.executeQuery(
                        "SELECT COUNT(*) FROM vault_settings WHERE id = 1"
                )) {

                    if (!result.next() || result.getInt(1) != 1) {
                        throw new IOException(
                                "The backup does not contain an initialized vault."
                        );
                    }
                }
            }

        } catch (SQLException e) {
            throw new IOException(
                    "The selected file is not a valid SecureVault database.",
                    e
            );
        }
    }

    /*
     * Installs a validated SQLite snapshot.
     *
     * The active connection is closed after validation.
     * A new Database instance must be created afterward.
     */
    public void restoreSnapshot(Path snapshot) throws IOException {

        if (snapshot == null) {
            throw new IllegalArgumentException(
                    "A database snapshot is required."
            );
        }

        if (isClosed()) {
            throw new IllegalStateException(
                    "The database connection is already closed."
            );
        }

        Path source = snapshot.toAbsolutePath().normalize();

        if (source.equals(databasePath.toAbsolutePath().normalize())) {
            throw new IOException(
                    "Cannot restore the active database file directly."
            );
        }

        validateDatabaseFile(source);

        Path directory = databasePath.getParent();

        Path stagedFile = Files.createTempFile(
                directory,
                ".securevault-restore-",
                ".db"
        );

        Path rollbackFile = null;

        boolean originalMoved = false;
        boolean newDatabaseInstalled = false;

        try {
            Files.copy(
                    source,
                    stagedFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            validateDatabaseFile(stagedFile);

            connection.close();

            rollbackFile = Files.createTempFile(
                    directory,
                    ".securevault-rollback-",
                    ".db"
            );

            Files.deleteIfExists(rollbackFile);

            if (Files.exists(databasePath)) {
                moveFile(databasePath, rollbackFile);
                originalMoved = true;
            }

            try {
                moveFile(stagedFile, databasePath);
                newDatabaseInstalled = true;

            } catch (IOException installError) {

                if (originalMoved) {
                    moveFile(rollbackFile, databasePath);
                    originalMoved = false;
                }

                throw installError;
            }

            if (rollbackFile != null) {
                Files.deleteIfExists(rollbackFile);
            }

        } finally {

            Files.deleteIfExists(stagedFile);

            if (newDatabaseInstalled && rollbackFile != null) {
                Files.deleteIfExists(rollbackFile);
            }
        }
    }

    private static void moveFile(Path source, Path destination)
            throws IOException {

        try {
            Files.move(
                    source,
                    destination,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (java.nio.file.AtomicMoveNotSupportedException e) {

            Files.move(source, destination);
        }
    }

    public boolean isClosed() {
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            throw databaseError(e);
        }
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
