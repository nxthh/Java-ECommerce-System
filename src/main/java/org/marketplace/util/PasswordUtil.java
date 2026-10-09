package org.marketplace.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Simple password utility that salts and hashes passwords with SHA-256.
 * <p>
 * The stored format is: {@code <base64-salt>:<base64-hash>}
 * </p>
 */
public class PasswordUtil {

    private static final int    SALT_BYTES = 16;
    private static final String ALGORITHM  = "SHA-256";

    private PasswordUtil() {}

    /**
     * Hashes {@code plainPassword} with a fresh random salt.
     *
     * @return the storable hash string in the format {@code salt:hash}.
     */
    public static String hash(String plainPassword) {
        SecureRandom rng  = new SecureRandom();
        byte[]       salt = new byte[SALT_BYTES];
        rng.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + digest(salt, plainPassword);
    }

    /**
     * Verifies a plaintext password against the stored hash.
     *
     * @param plainPassword the password to check.
     * @param storedHash    the value returned by {@link #hash(String)}.
     * @return {@code true} if the password matches.
     */
    public static boolean verify(String plainPassword, String storedHash) {
        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) return false;
        byte[] salt    = Base64.getDecoder().decode(parts[0]);
        String expected = parts[1];
        return expected.equals(digest(salt, plainPassword));
    }

    // -----------------------------------------------------------------------

    private static String digest(byte[] salt, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
