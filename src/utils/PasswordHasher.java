package utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Secure password hashing using PBKDF2 with SHA-256.
 * Format: iterations:salt:hash
 */
public class PasswordHasher {
    private static final int ITERATIONS = 10000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;

    private PasswordHasher() {}

    public static String hashPassword(String password) {
        char[] chars = password.toCharArray();
        byte[] salt = getSalt();

        byte[] hash = pbkdf2(chars, salt, ITERATIONS, HASH_BYTES);
        return ITERATIONS + ":" + toBase64(salt) + ":" + toBase64(hash);
    }

    public static boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) return false;
        
        // Handle legacy plaintext passwords (non-colon format)
        if (!storedHash.contains(":")) {
            return password.equals(storedHash);
        }

        String[] parts = storedHash.split(":");
        if (parts.length != 3) return false;

        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = fromBase64(parts[1]);
            byte[] hash = fromBase64(parts[2]);

            byte[] testHash = pbkdf2(password.toCharArray(), salt, iterations, hash.length);
            return slowEquals(hash, testHash);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a password string is already hashed.
     */
    public static boolean isHashed(String password) {
        return password != null && password.contains(":") && password.split(":").length == 3;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private static byte[] getSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        sr.nextBytes(salt);
        return salt;
    }

    private static String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] fromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
