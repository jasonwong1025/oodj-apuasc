package utils;

import utils.validation.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ValidationUtil {

    private ValidationUtil() {}

    /**
     * Checks if a string is not null and not empty.
     */
    public static boolean isNotEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Validates an object based on its field annotations.
     * Returns a comma-separated list of violations, or null if valid.
     */
    public static <T> String getViolations(T object) {
        if (object == null) return "Object cannot be null";
        List<String> violations = new ArrayList<>();
        
        Class<?> clazz = object.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(object);
                    String fieldName = field.getName();
                    // Basic CamelCase to Title Case for field names
                    String displayName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    
                    if (field.isAnnotationPresent(NotNull.class) && value == null) {
                        violations.add(displayName + " " + field.getAnnotation(NotNull.class).message());
                    }
                    
                    if (field.isAnnotationPresent(NotBlank.class)) {
                        if (value == null || value.toString().trim().isEmpty()) {
                            violations.add(displayName + " " + field.getAnnotation(NotBlank.class).message());
                        }
                    }
                    
                    if (field.isAnnotationPresent(Email.class) && value != null) {
                        if (!value.toString().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                            violations.add(displayName + " " + field.getAnnotation(Email.class).message());
                        }
                    }
                    
                    if (field.isAnnotationPresent(utils.validation.Pattern.class) && value != null) {
                        utils.validation.Pattern p = field.getAnnotation(utils.validation.Pattern.class);
                        if (!value.toString().matches(p.regexp())) {
                            violations.add(displayName + " " + p.message());
                        }
                    }
                    
                    if (field.isAnnotationPresent(Size.class) && value != null) {
                        Size s = field.getAnnotation(Size.class);
                        int len = value.toString().length();
                        if (len < s.min() || len > s.max()) {
                            violations.add(displayName + " " + s.message()
                                    .replace("{min}", String.valueOf(s.min()))
                                    .replace("{max}", String.valueOf(s.max())));
                        }
                    }
                    
                    if (field.isAnnotationPresent(Min.class) && value != null) {
                        long min = field.getAnnotation(Min.class).value();
                        if (value instanceof Number && ((Number) value).longValue() < min) {
                            violations.add(displayName + " " + field.getAnnotation(Min.class).message()
                                    .replace("{value}", String.valueOf(min)));
                        }
                    }

                    if (field.isAnnotationPresent(Max.class) && value != null) {
                        long max = field.getAnnotation(Max.class).value();
                        if (value instanceof Number && ((Number) value).longValue() > max) {
                            violations.add(displayName + " " + field.getAnnotation(Max.class).message()
                                    .replace("{value}", String.valueOf(max)));
                        }
                    }
                    
                } catch (IllegalAccessException e) {
                    // Ignore private access issues
                }
            }
            clazz = clazz.getSuperclass();
        }
        
        if (violations.isEmpty()) return null;
        return String.join("\n", violations);
    }
    
    // Legacy support for manual checks (used in Login/Register)
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidPassword(String password) {
        // Minimum 6 characters, at least one uppercase, one lowercase, one number, and one special character
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,}$";
        return password != null && password.matches(regex);
    }

    public static boolean isValidContact(String contact) {
        return contact != null && contact.trim().matches("^\\d{10,11}$");
    }

    public static String invalidEmailMessage() {
        return "Please enter a valid email address (e.g. user@mail.com).";
    }

    public static String invalidContactMessage() {
        return "Contact number must be 10-11 digits.";
    }

    public static String passwordRequirementsMessage() {
        return "Password must have at least 6 characters, including uppercase, lowercase, number, and special character.";
    }
}
