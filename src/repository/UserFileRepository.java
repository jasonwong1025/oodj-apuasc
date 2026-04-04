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
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    String userId = parts[0];
                    String fullName = parts[1];
                    String email = parts[2];
                    String contact = parts[3];
                    String password = parts[4];
                    String role = parts[5];

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
                    users.add(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public abstracts.AbstractUser authenticateUser(String email, String password) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    String recordEmail = parts[2];
                    String recordPassword = parts[4];
                    
                    if (recordEmail.equalsIgnoreCase(email) && recordPassword.equals(password)) {
                        String userId = parts[0];
                        String fullName = parts[1];
                        String contact = parts[3];
                        String role = parts[5];

                        switch (role) {
                            case "Customer": return new Customer(userId, fullName, email, contact, password);
                            case "Manager": return new Manager(userId, fullName, email, contact, password);
                            case "Technician": return new Technician(userId, fullName, email, contact, password);
                            case "CounterStaff": return new CounterStaff(userId, fullName, email, contact, password);
                            default: return new User(userId, fullName, email, contact, password, role);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveUser(User user) {
        File file = new File(FILE_PATH);
        // Ensure data directory exists
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
}
