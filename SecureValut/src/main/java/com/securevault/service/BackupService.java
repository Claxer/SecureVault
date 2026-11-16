package com.securevault.service;

import com.securevault.crypto.CryptoService;
import com.securevault.db.Database;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

    private BackupService() {
    }

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

        boolean backupCreated = false;

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
                    parent != null ? parent : Path.of(".").toAbsolutePath();

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

            try (DataOutputStream output = new DataOutputStream(
                    new BufferedOutputStream(
                            Files.newOutputStream(
                                    absoluteDestination,
                                    StandardOpenOption.CREATE_NEW,
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

            backupCreated = true;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Could not create SecureVault backup: " +
                            e.getMessage(),
                    e
            );

        } finally {

            if (snapshot != null) {
                try {
                    Files.deleteIfExists(snapshot);
                } catch (IOException ignored) {
                }
            }

            if (snapshotData != null) {
                Arrays.fill(snapshotData, (byte) 0);
            }

            if (salt != null) {
                Arrays.fill(salt, (byte) 0);
            }

            if (encryptedData != null) {
                Arrays.fill(encryptedData, (byte) 0);
            }

            Arrays.fill(backupPassword, '\0');

            if (!backupCreated) {
                try {
                    Files.deleteIfExists(absoluteDestination);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /*
     * Reads and validates the backup container.
     * Full password verification and restoration will be implemented next.
     */
    public static void validateBackupFile(Path backupFile)
            throws IOException {

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
                throw new IOException("Invalid backup salt.");
            }

            byte[] salt = input.readNBytes(saltLength);

            if (salt.length != SALT_LENGTH) {
                throw new IOException("Backup file is incomplete.");
            }

            int encryptedLength = input.readInt();

            if (encryptedLength <= NONCE_LENGTH + GCM_TAG_LENGTH
                    || encryptedLength > 500_000_000) {
                throw new IOException(
                        "Invalid backup data length."
                );
            }

            byte[] encryptedData =
                    input.readNBytes(encryptedLength);

            if (encryptedData.length != encryptedLength) {
                throw new IOException("Backup file is incomplete.");
            }

            if (input.read() != -1) {
                throw new IOException(
                        "Backup file contains unexpected extra data."
                );
            }

            Arrays.fill(salt, (byte) 0);
            Arrays.fill(encryptedData, (byte) 0);
        }
    }
}
