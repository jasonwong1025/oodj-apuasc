package service_layer;

import model.users.User;
import repository.UserFileRepository;
import utils.ValidationUtil;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PasswordResetService — Service layer class responsible for the "Forgot Password" workflow.
 *
 * OOP Design:
 *  - Single Responsibility: handles only OTP generation, validation, and password update.
 *  - Encapsulation: OTP store and expiry are private; exposed only through public methods.
 *  - Depends on UserFileRepository for persistence (Dependency Inversion principle).
 */
public class PasswordResetService {

    /** In-memory OTP store: email → {otp, expiry} */
    private static final Map<String, OtpEntry> OTP_STORE = new HashMap<>();

    private static final int OTP_LENGTH = 6;
    /** OTP is valid for 10 minutes */
    private static final int OTP_EXPIRY_MINUTES = 10;

    private final UserFileRepository userRepository;

    public PasswordResetService() {
        this.userRepository = new UserFileRepository();
    }

    // -------------------------------------------------------------------------
    // Inner class: encapsulates OTP entry (value + expiry)
    // -------------------------------------------------------------------------
    private static class OtpEntry {
        final String otp;
        final LocalDateTime expiry;

        OtpEntry(String otp) {
            this.otp = otp;
            this.expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Looks up a user by email.
     * @return the User if found and active, null otherwise.
     */
    public User findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        for (User u : userRepository.getAllUsers()) {
            if (u.getEmail().equalsIgnoreCase(email.trim()) && u.isActive()) {
                return u;
            }
        }
        return null;
    }

    /**
     * Generates a 6-digit OTP for the given email and stores it in memory.
     * @return the generated OTP string.
     */
    public String generateOtp(String email) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        String otp = sb.toString();
        OTP_STORE.put(email.toLowerCase(), new OtpEntry(otp));
        return otp;
    }

    /**
     * Validates the entered OTP for the given email.
     * @return null on success, error message on failure.
     */
    public String validateOtp(String email, String enteredOtp) {
        OtpEntry entry = OTP_STORE.get(email.toLowerCase());
        if (entry == null) {
            return "No OTP was requested for this email.";
        }
        if (entry.isExpired()) {
            OTP_STORE.remove(email.toLowerCase());
            return "OTP has expired. Please request a new one.";
        }
        if (!entry.otp.equals(enteredOtp.trim())) {
            return "Invalid OTP. Please try again.";
        }
        return null; // success
    }

    /**
     * Updates the password for the user with the given email.
     * Clears the OTP from the store after a successful update.
     * @return null on success, error message on failure.
     */
    public String resetPassword(String email, String newPassword) {
        if (!ValidationUtil.isValidPassword(newPassword)) {
            return ValidationUtil.passwordRequirementsMessage();
        }
        List<User> all = userRepository.getAllUsers();
        boolean found = false;
        for (User u : all) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) {
                u.setPassword(newPassword);
                found = true;
                break;
            }
        }
        if (!found) return "User not found.";
        try {
            userRepository.writeAllUsers(all);
            OTP_STORE.remove(email.toLowerCase()); // clear OTP after successful reset
            return null;
        } catch (IOException e) {
            return "Failed to save new password: " + e.getMessage();
        }
    }
}
