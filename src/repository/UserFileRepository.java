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
            e.printStackTrace();
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

        String id = parts[0].trim();
        String name, email, contact, pass, role, svc;
        boolean active;

        if (len == 8) {
            // Check if parts[1] is username (legacy) or fullName (current)
            // Current: id|name|email|contact|pass|role|svc|status
            // Legacy: id|user|name|email|contact|pass|role|status
            if ("ACTIVE".equalsIgnoreCase(parts[7].trim()) || "INACTIVE".equalsIgnoreCase(parts[7].trim())) {
                name = parts[1].trim();
                email = parts[2].trim();
                contact = parts[3].trim();
                pass = parts[4].trim();
                role = parts[5].trim();
                svc = parts[6].trim();
                active = "ACTIVE".equalsIgnoreCase(parts[7].trim());
            } else {
                // Legacy with 8 parts but last is not status? Unlikely based on docs, but let's be safe.
                name = parts[2].trim();
                email = parts[3].trim();
                contact = parts[4].trim();
                pass = parts[5].trim();
                role = parts[6].trim();
                svc = "-";
                active = "ACTIVE".equalsIgnoreCase(parts[7].trim());
            }
        } else if (len == 7) {
            // Legacy: id|name|email|contact|pass|role|status
            name = parts[1].trim();
            email = parts[2].trim();
            contact = parts[3].trim();
            pass = parts[4].trim();
            role = parts[5].trim();
            svc = "-";
            active = "ACTIVE".equalsIgnoreCase(parts[6].trim());
        } else { // len == 6
            // Legacy: id|name|email|contact|pass|role
            name = parts[1].trim();
            email = parts[2].trim();
            contact = parts[3].trim();
            pass = parts[4].trim();
            role = parts[5].trim();
            svc = "-";
            active = true;
        }

        return buildUser(id, name, email, contact, pass, role, svc, active);
    }

    public User findById(String userId) {
        if (userId == null) return null;
        for (User u : getAllUsers()) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }

    private User buildUser(String userId, String fullName, String email,
                           String contact, String password, String role, String technicianServiceType, boolean active) {
        User user;
        switch (role) {
            case "Customer":
                user = new Customer(userId, fullName, email, contact, password);
                break;
            case "Manager":
                user = new Manager(userId, fullName, email, contact, password);
                break;
            case "Technician":
                user = new Technician(userId, fullName, email, contact, password);
                break;
            case "CounterStaff":
                user = new CounterStaff(userId, fullName, email, contact, password);
                break;
            default:
                user = new User(userId, fullName, email, contact, password, role);
                break;
        }
        String normalizedServiceType = "Technician".equals(role)
                ? (technicianServiceType == null || technicianServiceType.trim().isEmpty() ? "-" : technicianServiceType.trim())
                : "-";
        user.setTechnicianServiceType(normalizedServiceType);
        user.setActive(active);
        return user;
    }

    public abstracts.AbstractUser authenticateUser(String email, String password) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                User candidate = parseUserLine(line);
                if (candidate == null || !candidate.isActive()) continue;

                if (candidate.getEmail().equalsIgnoreCase(email) && candidate.getPassword().equals(password)) {
                    return candidate;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveUser(User user) {
        File file = new File(FILE_PATH);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(user.toString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Sang Yew Changes - Edit Profile
    public void updateUser(User updatedUser) {
    List<User> users = getAllUsers();

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
        for (User user : users) {
            if (user.getUserId().equals(updatedUser.getUserId())) {
                bw.write(updatedUser.toString());
            } else {
                bw.write(user.toString());
            }
            bw.newLine();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    //Sang Yew Changes - Manage Customer
    public void deleteUser(String userId) {
    List<User> users = getAllUsers();

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
        for (User user : users) {
            if (!user.getUserId().equals(userId)) {
                bw.write(user.toString());
                bw.newLine();
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public void writeAllUsers(List<User> users) throws IOException {
        File file = new File(FILE_PATH);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (User u : users) {
                bw.write(u.toString());
                bw.newLine();
            }
        }
    }
}
