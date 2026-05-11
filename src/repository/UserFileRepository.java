package repository;

import model.users.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserFileRepository {
    private static final String FILE_PATH = "data/users.txt";

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return users;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                User user = parseUserLine(line);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (IOException e) {
            utils.Logger.error("Repository", "I/O Error", e);
        }
        return users;
    }

    /**
     * Supports legacy formats and normalizes them to the 8-field format:
     * userId|fullName|email|contact|password|role|technicianServiceType|status
     */
    private User parseUserLine(String line) {
        String[] parts = line.split("\\|", -1);
        int len = parts.length;

        if (len < 6) return null;

        String id = utils.FileStorageHelper.unescape(parts[0].trim());
        String name, email, contact, pass, role, svc;
        boolean active;

        if (len == 8) {
            name = utils.FileStorageHelper.unescape(parts[1].trim());
            email = utils.FileStorageHelper.unescape(parts[2].trim());
            contact = utils.FileStorageHelper.unescape(parts[3].trim());
            pass = parts[4].trim(); // Passwords are Base64 encoded or raw, no need to unescape normally but safe if we do
            role = utils.FileStorageHelper.unescape(parts[5].trim());
            svc = utils.FileStorageHelper.unescape(parts[6].trim());
            active = "ACTIVE".equalsIgnoreCase(parts[7].trim());
        } else if (len == 7) {
            name = utils.FileStorageHelper.unescape(parts[1].trim());
            email = utils.FileStorageHelper.unescape(parts[2].trim());
            contact = utils.FileStorageHelper.unescape(parts[3].trim());
            pass = parts[4].trim();
            role = utils.FileStorageHelper.unescape(parts[5].trim());
            svc = "-";
            active = "ACTIVE".equalsIgnoreCase(parts[6].trim());
        } else { // len == 6
            name = utils.FileStorageHelper.unescape(parts[1].trim());
            email = utils.FileStorageHelper.unescape(parts[2].trim());
            contact = utils.FileStorageHelper.unescape(parts[3].trim());
            pass = parts[4].trim();
            role = utils.FileStorageHelper.unescape(parts[5].trim());
            svc = "-";
            active = true;
        }

        return buildUser(id, name, email, contact, pass, model.users.Role.fromString(role), svc, active);
    }

    public User findById(String userId) {
        if (userId == null) return null;
        for (User u : getAllUsers()) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }

    private User buildUser(String userId, String fullName, String email, String contact, String password, model.users.Role role, String technicianServiceType, boolean active) {
        if (role == null) role = model.users.Role.CUSTOMER;
        User user;
        switch (role) {
            case MANAGER:
                user = new Manager(userId, fullName, email, contact, password);
                break;
            case COUNTERSTAFF:
                user = new CounterStaff(userId, fullName, email, contact, password);
                break;
            case TECHNICIAN:
                user = new Technician(userId, fullName, email, contact, password);
                user.setTechnicianServiceType(technicianServiceType);
                break;
            case CUSTOMER:
            default:
                user = new Customer(userId, fullName, email, contact, password);
                break;
        }
        String normalizedServiceType = (role == model.users.Role.TECHNICIAN)
                ? (technicianServiceType == null || technicianServiceType.trim().isEmpty() ? "-" : technicianServiceType.trim())
                : "-";
        user.setTechnicianServiceType(normalizedServiceType);
        user.setActive(active);
        return user;
    }

    public abstracts.AbstractUser authenticateUser(String email, String password) {
        List<User> all = getAllUsers();
        for (User candidate : all) {
            if (!candidate.isActive()) continue;
            if (candidate.getEmail().equalsIgnoreCase(email.trim())) {
                boolean authenticated = false;
                
                // 1. Check if it's a valid hash
                if (utils.PasswordHasher.isHashed(candidate.getPassword())) {
                    authenticated = utils.PasswordHasher.verifyPassword(password, candidate.getPassword());
                } else {
                    // 2. Fallback for legacy/reverted plaintext passwords
                    authenticated = candidate.getPassword().equals(password);
                    if (authenticated) {
                        // 3. Auto-migrate to hashed for next time
                        candidate.setPassword(utils.PasswordHasher.hashPassword(password));
                        try {
                            writeAllUsers(all);
                        } catch (java.io.IOException e) {
                            utils.Logger.error("UserFileRepository", "Failed to auto-migrate password hash", e);
                        }
                    }
                }

                if (authenticated) return candidate;
            }
        }
        return null;
    }

    public void saveUser(User user) {
        try {
            utils.FileStorageHelper.appendLine(FILE_PATH, user.toString());
        } catch (IOException e) {
            utils.Logger.error("Repository", "I/O Error", e);
        }
    }

    public void updateUser(User updatedUser) {
        List<User> users = getAllUsers();
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            if (user.getUserId().equals(updatedUser.getUserId())) {
                lines.add(updatedUser.toString());
            } else {
                lines.add(user.toString());
            }
        }
        try {
            utils.FileStorageHelper.writeAtomic(FILE_PATH, lines);
        } catch (IOException e) {
            utils.Logger.error("Repository", "I/O Error", e);
        }
    }

    public void deleteUser(String userId) {
        List<User> users = getAllUsers();
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            if (!user.getUserId().equals(userId)) {
                lines.add(user.toString());
            }
        }
        try {
            utils.FileStorageHelper.writeAtomic(FILE_PATH, lines);
        } catch (IOException e) {
            utils.Logger.error("Repository", "I/O Error", e);
        }
    }

    public void writeAllUsers(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(u.toString());
        }
        utils.FileStorageHelper.writeAtomic(FILE_PATH, lines);
    }
}
