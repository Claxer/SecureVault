package com.securevault.service;

import com.securevault.crypto.CryptoService;
import com.securevault.db.Database;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public final class BackupService {

    private static final int MAGIC = 0x5356424B; // SVBK
    private static final int VERSION = 1;

    private static final int SALT_LENGTH = 16;
    private static final int NONCE_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_ENCRYPTED_LENGTH = 500_000_000;

    private BackupService() {
    }

    // =========================================================
    // CREATE ENCRYPTED BACKUP
    // =========================================================

    public static void createBackup(
            Database database,
            Path destination,
            char[] backupPassword
    ) {

        if (database == null || destination == null) {
            throw new IllegalArgumentException(
                    "Database and backup destination are required."
            );
        }

        if (backupPassword == null
                || backupPassword.length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Backup password must contain at least 12 characters."
            );
        }

        if (database.isClosed()) {
            throw new IllegalStateException(
                    "The database connection is closed."
            );
        }

        Path absoluteDestination =
                destination.toAbsolutePath().normalize();

        Path parent = absoluteDestination.getParent();

        Path snapshot = null;
        Path temporaryBackup = null;

        byte[] snapshotData = null;
        byte[] salt = null;
        byte[] encryptedData = null;

        char[] passwordCopy = backupPassword.clone();

        try {

            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (Files.exists(absoluteDestination)) {
                throw new IOException(
                        "A file already exists at the selected location."
                );
            }

            Path temporaryDirectory = parent != null
                    ? parent
                    : Path.of(".").toAbsolutePath();

            // Create a temporary SQLite snapshot.
            snapshot = Files.createTempFile(
                    temporaryDirectory,
                    ".securevault-snapshot-",
                    ".db"
            );

            Files.deleteIfExists(snapshot);

            database.createSnapshot(snapshot);

            // Read the snapshot.
            snapshotData = Files.readAllBytes(snapshot);

            if (snapshotData.length == 0) {
                throw new IOException(
                        "The database snapshot is empty."
                );
            }

            salt = CryptoService.generateSalt();

            // Encrypt the complete database snapshot.
            encryptedData = CryptoService.encryptBytes(
                    snapshotData,
                    passwordCopy,
                    salt
            );

            if (encryptedData == null
                    || encryptedData.length <= NONCE_LENGTH + GCM_TAG_LENGTH
                    || encryptedData.length > MAX_ENCRYPTED_LENGTH) {
                throw new IOException(
                        "The encrypted backup exceeds the supported size."
                );
            }

            // Write to a temporary file first.
            temporaryBackup = Files.createTempFile(
                    temporaryDirectory,
                    ".securevault-backup-",
                    ".tmp"
            );

            try (DataOutputStream output = new DataOutputStream(
                    new BufferedOutputStream(
                            Files.newOutputStream(
                                    temporaryBackup,
                                    StandardOpenOption.TRUNCATE_EXISTING,
                                    StandardOpenOption.WRITE
                            )
                    )
            )) {

                output.writeInt(MAGIC);
                output.writeInt(VERSION);

                output.writeInt(salt.length);
                output.write(salt);

                output.writeInt(encryptedData.length);
                output.write(encryptedData);

                output.flush();
            }

            // Move the completed backup into its final location.
            // No REPLACE_EXISTING: existing backups are protected.
            try {
                Files.move(
                        temporaryBackup,
                        absoluteDestination
                );

                temporaryBackup = null;

            } catch (IOException e) {
                throw new IOException(
                        "Could not move the completed backup "
                                + "to the selected destination.",
                        e
                );
            }

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Could not create SecureVault backup: "
                            + e.getMessage(),
                    e
            );

        } finally {

            deleteTemporaryFile(snapshot);
            deleteTemporaryFile(temporaryBackup);

            clear(snapshotData);
            clear(salt);
            clear(encryptedData);

            clear(passwordCopy);
        }
    }

    // =========================================================
    // RESTORE ENCRYPTED BACKUP
    // =========================================================

    public static void restoreBackup(
            Database database,
            Path backupFile,
            char[] backupPassword
    ) throws IOException {

        if (database == null || backupFile == null) {
            throw new IllegalArgumentException(
                    "Database and backup file are required."
            );
        }

        if (database.isClosed()) {
            throw new IllegalStateException(
                    "The database connection is closed."
            );
        }

        if (backupPassword == null
                || backupPassword.length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Backup password must contain at least 12 characters."
            );
        }

        byte[] salt = null;
        byte[] encryptedData = null;
        byte[] snapshotData = null;

        Path snapshot = null;

        char[] passwordCopy = backupPassword.clone();

        try {

            // Read and validate the backup container.
            BackupContents contents = readBackup(backupFile);

            salt = contents.salt();
            encryptedData = contents.encryptedData();

            // Decrypt the database snapshot.
            try {

                snapshotData = CryptoService.decryptBytes(
                        encryptedData,
                        passwordCopy,
                        salt
                );

            } catch (Exception e) {

                throw new IOException(
                        "Could not decrypt the backup. "
                                + "The password may be incorrect, "
                                + "or the backup may be damaged.",
                        e
                );
            }

            if (snapshotData == null || snapshotData.length == 0) {
                throw new IOException(
                        "The decrypted backup contains no database data."
                );
            }

            // Create a temporary database file.
            snapshot = Files.createTempFile(
                    "securevault-restored-",
                    ".db"
            );

            Files.write(
                    snapshot,
                    snapshotData,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            // Validate and install the restored database.
            // Database.restoreSnapshot now reopens its connection.
            database.restoreSnapshot(snapshot);

        } finally {

            clear(salt);
            clear(encryptedData);
            clear(snapshotData);

            clear(passwordCopy);

            deleteTemporaryFile(snapshot);
        }
    }

    // =========================================================
    // BACKUP VALIDATION
    // =========================================================

    /*
     * Checks the backup container structure.
     *
     * This does not verify the backup password or decrypt
     * the database contents.
     */
    public static void validateBackupFile(Path backupFile)
            throws IOException {

        BackupContents contents = readBackup(backupFile);

        try {
            // Successful reading confirms the container structure.
        } finally {
            clear(contents.salt());
            clear(contents.encryptedData());
        }
    }

    // =========================================================
    // READ BACKUP FILE
    // =========================================================

    private static BackupContents readBackup(Path backupFile)
            throws IOException {

        if (backupFile == null || !Files.isRegularFile(backupFile)) {
            throw new IOException(
                    "The selected backup file does not exist."
            );
        }

        long fileSize = Files.size(backupFile);

        if (fileSize < 32) {
            throw new IOException(
                    "The backup file is too small or incomplete."
            );
        }

        if (fileSize > MAX_ENCRYPTED_LENGTH + 128L) {
            throw new IOException(
                    "The backup file exceeds the maximum supported size."
            );
        }

        byte[] salt = null;
        byte[] encryptedData = null;

        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(
                        Files.newInputStream(backupFile)
                )
        )) {

            // Read file signature.
            int magic = input.readInt();

            if (magic != MAGIC) {
                throw new IOException(
                        "This is not a SecureVault backup file."
                );
            }

            // Read backup version.
            int version = input.readInt();

            if (version != VERSION) {
                throw new IOException(
                        "This backup uses an unsupported version."
                );
            }

            // Read salt.
            int saltLength = input.readInt();

            if (saltLength != SALT_LENGTH) {
                throw new IOException(
                        "Invalid backup salt."
                );
            }

            salt = input.readNBytes(saltLength);

            if (salt.length != SALT_LENGTH) {
                throw new IOException(
                        "Backup file is incomplete."
                );
            }

            // Read encrypted database length.
            int encryptedLength = input.readInt();

            if (encryptedLength <= NONCE_LENGTH + GCM_TAG_LENGTH
                    || encryptedLength > MAX_ENCRYPTED_LENGTH) {
                throw new IOException(
                        "Invalid backup data length."
                );
            }

            // Ensure the declared length fits inside the file.
            long expectedLength =
                    16L + SALT_LENGTH + encryptedLength;

            if (expectedLength != fileSize) {
                throw new IOException(
                        "Backup file size does not match its contents."
                );
            }

            encryptedData = input.readNBytes(encryptedLength);

            if (encryptedData.length != encryptedLength) {
                throw new IOException(
                        "Backup file is incomplete."
                );
            }

            // Reject unexpected trailing data.
            if (input.read() != -1) {
                throw new IOException(
                        "Backup file contains unexpected extra data."
                );
            }

            return new BackupContents(
                    salt,
                    encryptedData
            );

        } catch (EOFException e) {

            clear(salt);
            clear(encryptedData);

            throw new IOException(
                    "The backup file is incomplete or corrupted.",
                    e
            );

        } catch (IOException e) {

            clear(salt);
            clear(encryptedData);

            throw e;
        }
    }

    // =========================================================
    // TEMPORARY FILE CLEANUP
    // =========================================================

    private static void deleteTemporaryFile(Path file) {

        if (file == null) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // Cleanup failure does not hide the original error.
        }
    }

    // =========================================================
    // MEMORY CLEANUP
    // =========================================================

    private static void clear(byte[] data) {

        if (data != null) {
            Arrays.fill(data, (byte) 0);
        }
    }

    private static void clear(char[] data) {

        if (data != null) {
            Arrays.fill(data, '\0');
        }
    }

    // =========================================================
    // BACKUP DATA
    // =========================================================

    private record BackupContents(
            byte[] salt,
            byte[] encryptedData
    ) {
    }
}
