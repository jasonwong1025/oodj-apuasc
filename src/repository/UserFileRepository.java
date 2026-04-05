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

    private User parseUserLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length == 6) {
            return buildUser(
                    parts[0], parts[2], parts[1], parts[2], parts[3], parts[4], parts[5], true);
        }
        if (parts.length == 8) {
            boolean active = "ACTIVE".equalsIgnoreCase(parts[7]);
            return buildUser(
                    parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], active);
        }
        return null;
    }

    private User buildUser(String userId, String username, String fullName, String email,
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
        user.setUsername(username);
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
