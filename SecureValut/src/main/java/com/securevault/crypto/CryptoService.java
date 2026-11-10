package com.securevault.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class CryptoService {

    private static final int ITERATIONS = 600_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private static final SecureRandom RANDOM = new SecureRandom();

    private CryptoService() {
    }

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public static SecretKey deriveKey(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(
                password,
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] keyBytes = factory.generateSecret(spec).getEncoded();

            return new SecretKeySpec(keyBytes, "AES");

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not derive encryption key.", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static String createVerifier(SecretKey key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getEncoded(), "HmacSHA256"));

            byte[] result = mac.doFinal(
                    "SecureVault master password verification".getBytes(
                            java.nio.charset.StandardCharsets.UTF_8
                    )
            );

            return Base64.getEncoder().encodeToString(result);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not verify master key.", e);
        }
    }

    public static boolean verifyKey(SecretKey key, String storedVerifier) {
        if (key == null || storedVerifier == null) {
            return false;
        }

        try {
            byte[] expected = Base64.getDecoder().decode(storedVerifier);
            byte[] actual = Base64.getDecoder().decode(createVerifier(key));

            return java.security.MessageDigest.isEqual(expected, actual);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String encrypt(String plainText, SecretKey key) {
        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            RANDOM.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    new GCMParameterSpec(TAG_LENGTH, nonce)
            );

            byte[] encrypted = cipher.doFinal(
                    plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            byte[] combined = new byte[nonce.length + encrypted.length];

            System.arraycopy(nonce, 0, combined, 0, nonce.length);
            System.arraycopy(
                    encrypted, 0,
                    combined, nonce.length,
                    encrypted.length
            );

            return Base64.getEncoder().encodeToString(combined);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Encryption failed.", e);
        }
    }

    public static String decrypt(String encryptedText, SecretKey key) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            if (combined.length <= NONCE_LENGTH) {
                throw new GeneralSecurityException("Invalid encrypted data.");
            }

            byte[] nonce = Arrays.copyOfRange(
                    combined, 0, NONCE_LENGTH
            );

            byte[] encrypted = Arrays.copyOfRange(
                    combined, NONCE_LENGTH, combined.length
            );

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    new GCMParameterSpec(TAG_LENGTH, nonce)
            );

            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(
                    decrypted,
                    java.nio.charset.StandardCharsets.UTF_8
            );

        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Decryption failed. The vault key or stored data may be invalid.",
                    e
            );
        }
    }
}
