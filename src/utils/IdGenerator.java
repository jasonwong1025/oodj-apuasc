package utils;

import model.users.User;
import repository.UserFileRepository;

import java.util.List;

public class IdGenerator {

    public static String generateNextCustomerId() {
        return generateNextIdForRole("Customer");
    }

    /**
     * Next ID for a role: Manager (M), CounterStaff (S), Technician (T), Customer (C).
     */
    public static String generateNextIdForRole(String role) {
        String prefix = rolePrefix(role);
        UserFileRepository repo = new UserFileRepository();
        List<User> users = repo.getAllUsers();

        int maxId = 0;
        for (User user : users) {
            if (!role.equals(user.getRole())) continue;
            String id = user.getUserId();
            if (id == null || id.length() < 2) continue;
            if (!id.startsWith(prefix)) continue;
            try {
                int currentId = Integer.parseInt(id.substring(1));
                if (currentId > maxId) {
                    maxId = currentId;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return String.format("%s%03d", prefix, maxId + 1);
    }

    private static String rolePrefix(String role) {
        switch (role) {
            case "Manager":
                return "M";
            case "CounterStaff":
                return "S";
            case "Technician":
                return "T";
            case "Customer":
                return "C";
            default:
                return "U";
        }
    }
}
