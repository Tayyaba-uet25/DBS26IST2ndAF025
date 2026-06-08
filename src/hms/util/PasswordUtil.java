package hms.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashes passwords with SHA-256 and returns lowercase hex.
 * This matches MySQL's SHA2(value,256) used in the seed data,
 * so seeded users can log in immediately.
 * SOFTWARE CLASS #3
 */
public final class PasswordUtil {

    private PasswordUtil() { }

    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    public static boolean matches(String plain, String storedHash) {
        return hash(plain).equalsIgnoreCase(storedHash);
    }
}
