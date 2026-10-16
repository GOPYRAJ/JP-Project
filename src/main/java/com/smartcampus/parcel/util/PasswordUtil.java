package com.smartcampus.parcel.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil provides cryptographic hashing for user passwords.
 * Passwords must never be stored in plain text.
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // Prevent instantiation
    }

    /**
     * Hashes a plain-text password using SHA-256.
     *
     * @param plainText the raw password string
     * @return 64-character hexadecimal SHA-256 hash string
     */
    public static String hash(String plainText) {
        if (plainText == null) {
            throw new IllegalArgumentException("Password to hash cannot be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    /**
     * Verifies if a raw password matches an existing hash.
     *
     * @param rawPassword the plain-text password to test
     * @param storedHash the expected hash from the database
     * @return true if the hash of rawPassword matches storedHash
     */
    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        return hash(rawPassword).equalsIgnoreCase(storedHash);
    }
}
