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
     * Supports:
     * <ul>
     *   <li>7 fields (current): userId|fullName|email|contact|password|role|status</li>
     *   <li>8 fields (legacy): userId|username|fullName|email|contact|password|role|status</li>
     *   <li>6 fields (legacy): userId|fullName|email|contact|password|role — status defaults to ACTIVE</li>
     * </ul>
     */
    private User parseUserLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length == 8) {
            boolean active = "ACTIVE".equalsIgnoreCase(parts[7]);
            return buildUser(
                    parts[0].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    parts[4].trim(),
                    parts[5].trim(),
                    parts[6].trim(),
                    active);
        }
        if (parts.length == 7) {
            boolean active = "ACTIVE".equalsIgnoreCase(parts[6]);
            return buildUser(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    parts[4].trim(),
                    parts[5].trim(),
                    active);
        }
        if (parts.length == 6) {
            return buildUser(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    parts[4].trim(),
                    parts[5].trim(),
                    true);
        }
        return null;
    }

    private User buildUser(String userId, String fullName, String email,
                           String contact, String password, String role, boolean active) {
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
