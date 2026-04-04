package utils;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    
    // Minimum 6 characters, at least one uppercase, one lowercase, one number, and one special character
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,}$";

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
}
