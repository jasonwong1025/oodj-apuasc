package utils;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    
    // Minimum 6 characters, at least one uppercase, one lowercase, one number, and one special character
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,}$";

    /** Digits only, length 10 or 11 (after trim). */
    private static final String CONTACT_REGEX = "^\\d{10,11}$";

    public static boolean isNotEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        if (!isNotEmpty(password)) return false;
        return Pattern.compile(PASSWORD_REGEX).matcher(password).matches();
    }

    public static boolean isValidContact(String contact) {
        if (!isNotEmpty(contact)) return false;
        return Pattern.compile(CONTACT_REGEX).matcher(contact.trim()).matches();
    }

    /** Shown when email format fails validation (same rules as {@link #isValidEmail}). */
    public static String invalidEmailMessage() {
        return "Please enter a valid email address.\n"
                + "Expected format: name@domain (for example, user@mail.com).";
    }

    /** Shown when contact fails {@link #isValidContact}. */
    public static String invalidContactMessage() {
        return "Contact number is invalid.\n"
                + "Use digits only (no letters, spaces, or symbols) and enter 10 or 11 digits.\n"
                + "Examples: 0123456789, 60123456789.";
    }

    /** Shown when password fails {@link #isValidPassword}; lists every requirement explicitly. */
    public static String passwordRequirementsMessage() {
        return "Password does not meet the requirements. Your password must have:\n"
                + "- At least 6 characters\n"
                + "- At least one uppercase letter (A–Z)\n"
                + "- At least one lowercase letter (a–z)\n"
                + "- At least one digit (0–9)\n"
                + "- At least one special character from: ! @ # $ % ^ & * ( ) _ + - = [ ] { } ; ' : \" \\ | , . < > / ?\n"
                + "Example that works: Password1!";
    }
}
