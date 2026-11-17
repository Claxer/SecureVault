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

    /*
     * Creates an encrypted SecureVault backup.
     */
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

        Path absoluteDestination =
                destination.toAbsolutePath().normalize();

        Path parent = absoluteDestination.getParent();

        Path snapshot = null;

        byte[] snapshotData = null;
        byte[] salt = null;
        byte[] encryptedData = null;

        boolean outputFileCreated = false;
        boolean backupCompleted = false;

        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (Files.exists(absoluteDestination)) {
                throw new IOException(
                        "A file already exists at the selected location."
                );
            }

            Path temporaryDirectory =
                    parent != null
                            ? parent
                            : Path.of(".").toAbsolutePath();

            snapshot = Files.createTempFile(
                    temporaryDirectory,
                    ".securevault-snapshot-",
                    ".db"
            );

            Files.deleteIfExists(snapshot);

            database.createSnapshot(snapshot);

            snapshotData = Files.readAllBytes(snapshot);

            salt = CryptoService.generateSalt();

            encryptedData = CryptoService.encryptBytes(
                    snapshotData,
                    backupPassword,
                    salt
            );

            try (var fileOutput = Files.newOutputStream(
                    absoluteDestination,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            )) {

                outputFileCreated = true;

                try (DataOutputStream output = new DataOutputStream(
                        new BufferedOutputStream(fileOutput)
                )) {

                    output.writeInt(MAGIC);
                    output.writeInt(VERSION);

                    output.writeInt(salt.length);
                    output.write(salt);

                    output.writeInt(encryptedData.length);
                    output.write(encryptedData);

                    output.flush();
                }
            }

            backupCompleted = true;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Could not create SecureVault backup: "
                            + e.getMessage(),
                    e
            );

        } finally {

            if (snapshot != null) {
                try {
                    Files.deleteIfExists(snapshot);
                } catch (IOException ignored) {
                }
            }

            clear(snapshotData);
            clear(salt);
            clear(encryptedData);

            Arrays.fill(backupPassword, '\0');

            if (outputFileCreated && !backupCompleted) {
                try {
                    Files.deleteIfExists(absoluteDestination);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /*
     * Restores an encrypted SecureVault backup.
     *
     * The backup password decrypts the database snapshot.
     * Database validates the SQLite file before replacing
     * the active database.
     *
     * The Database connection is closed after a successful
     * restoration. The application must reopen it.
     */
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

        try {
            BackupContents contents = readBackup(backupFile);

            salt = contents.salt();
            encryptedData = contents.encryptedData();

            // Decrypt the database snapshot.
            try {
                snapshotData = CryptoService.decryptBytes(
                        encryptedData,
                        backupPassword,
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

            // Write decrypted data to a temporary database file.
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

            // Database validates and installs the snapshot.
            database.restoreSnapshot(snapshot);

        } finally {

            clear(salt);
            clear(encryptedData);
            clear(snapshotData);

            Arrays.fill(backupPassword, '\0');

            if (snapshot != null) {
                try {
                    Files.deleteIfExists(snapshot);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /*
     * Validates the backup container structure.
     * Does not verify the password or decrypt the contents.
     */
    public static void validateBackupFile(Path backupFile)
            throws IOException {

        BackupContents contents = readBackup(backupFile);

        clear(contents.salt());
        clear(contents.encryptedData());
    }

    /*
     * Reads and checks the encrypted backup container.
     */
    private static BackupContents readBackup(Path backupFile)
            throws IOException {

        if (backupFile == null || !Files.isRegularFile(backupFile)) {
            throw new IOException(
                    "The selected backup file does not exist."
            );
        }

        long fileSize = Files.size(backupFile);

        if (fileSize > MAX_ENCRYPTED_LENGTH + 128L) {
            throw new IOException(
                    "The backup file exceeds the maximum supported size."
            );
        }

        byte[] salt = null;
        byte[] encryptedData = null;

        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(backupFile))
        )) {

            int magic = input.readInt();
            int version = input.readInt();

            if (magic != MAGIC) {
                throw new IOException(
                        "This is not a SecureVault backup file."
                );
            }

            if (version != VERSION) {
                throw new IOException(
                        "This backup uses an unsupported version."
                );
            }

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

            int encryptedLength = input.readInt();

            if (encryptedLength <= NONCE_LENGTH + GCM_TAG_LENGTH
                    || encryptedLength > MAX_ENCRYPTED_LENGTH) {
                throw new IOException(
                        "Invalid backup data length."
                );
            }

            encryptedData = input.readNBytes(encryptedLength);

            if (encryptedData.length != encryptedLength) {
                throw new IOException(
                        "Backup file is incomplete."
                );
            }

            if (input.read() != -1) {
                throw new IOException(
                        "Backup file contains unexpected extra data."
                );
            }

            return new BackupContents(salt, encryptedData);

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

    private static void clear(byte[] data) {
        if (data != null) {
            Arrays.fill(data, (byte) 0);
        }
    }

    private record BackupContents(
            byte[] salt,
            byte[] encryptedData
    ) {
    }
}
