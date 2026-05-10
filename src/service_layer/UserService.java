package service_layer;

import abstracts.AbstractUser;
import model.users.*;
import repository.UserFileRepository;
import utils.IdGenerator;
import utils.ValidationUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    private final UserFileRepository userRepository;

    public UserService() {
        this.userRepository = new UserFileRepository();
    }

    public utils.Result<AbstractUser> login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return utils.Result.failure("Email cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            return utils.Result.failure("Password cannot be empty.");
        }

        AbstractUser user = userRepository.authenticateUser(email, password);
        if (user == null) {
            return utils.Result.failure("Invalid email or password, or account is inactive.");
        }

        // Migration: If password is not hashed, hash it now
        if (!utils.PasswordHasher.isHashed(user.getPassword())) {
            user.setPassword(utils.PasswordHasher.hashPassword(password));
            userRepository.updateUser((User) user);
        }

        return utils.Result.success(user);
    }

    public List<User> listAllUsers() {
        return userRepository.getAllUsers();
    }

    public User findByUserId(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * Adds a new user with the specified role and details.
     */
    public utils.Result<User> addUser(String role, String fullName, String email, String contact, String password,
                                     String technicianServiceType) {
        if (!ValidationUtil.isNotEmpty(fullName)
                || !ValidationUtil.isNotEmpty(email) || !ValidationUtil.isNotEmpty(contact)
                || !ValidationUtil.isNotEmpty(password)) {
            return utils.Result.failure("All fields are required.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return utils.Result.failure(ValidationUtil.invalidEmailMessage());
        }
        if (!ValidationUtil.isValidContact(contact)) {
            return utils.Result.failure(ValidationUtil.invalidContactMessage());
        }
        if (!ValidationUtil.isValidPassword(password)) {
            return utils.Result.failure(ValidationUtil.passwordRequirementsMessage());
        }
        if (!isAllowedManagedRole(role)) {
            return utils.Result.failure("Invalid role selected.");
        }
        if ("Technician".equals(role)) {
            if (!"Normal Service".equals(technicianServiceType) && !"Major Service".equals(technicianServiceType)) {
                return utils.Result.failure("Technician service type is required.");
            }
        } else {
            technicianServiceType = "-";
        }
        List<User> all = listAllUsers();
        for (User u : all) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return utils.Result.failure("Email is already in use.");
            }
        }

        String newId = IdGenerator.generateNextIdForRole(role);
        String hashedPassword = utils.PasswordHasher.hashPassword(password);
        User created = instantiateUser(newId, fullName.trim(), email.trim(),
                contact.trim(), hashedPassword, role, technicianServiceType);
        created.setActive(true);

        all.add(created);
        try {
            userRepository.writeAllUsers(all);
        } catch (IOException e) {
            return utils.Result.failure("Failed to save users: " + e.getMessage());
        }
        return utils.Result.success(created);
    }

    /**
     * Updates an existing user's details.
     * @param updated The user object with updated information.
     * @param managerUserId The ID of the manager performing the update (to prevent self-deactivation).
     *                      Pass null if not applicable (e.g. self-update).
     */
    public utils.Result<Void> updateUser(User updated, String managerUserId) {
        if (updated == null) return utils.Result.failure("No user to update.");
        if (!ValidationUtil.isNotEmpty(updated.getFullName())
                || !ValidationUtil.isNotEmpty(updated.getEmail()) || !ValidationUtil.isNotEmpty(updated.getContact())) {
            return utils.Result.failure("Full name, email, and contact are required.");
        }
        if (!ValidationUtil.isValidEmail(updated.getEmail())) {
            return utils.Result.failure(ValidationUtil.invalidEmailMessage());
        }
        if (!ValidationUtil.isValidContact(updated.getContact())) {
            return utils.Result.failure(ValidationUtil.invalidContactMessage());
        }
        
        // Only validate password if it's being changed
        if (ValidationUtil.isNotEmpty(updated.getPassword())) {
            User existing = findByUserId(updated.getUserId());
            if (existing != null && !updated.getPassword().equals(existing.getPassword())) {
                if (!ValidationUtil.isValidPassword(updated.getPassword())) {
                    return utils.Result.failure(ValidationUtil.passwordRequirementsMessage());
                }
            }
        }

        List<User> all = listAllUsers();
        int idx = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getUserId().equals(updated.getUserId())) {
                idx = i;
                break;
            }
        }
        if (idx < 0) return utils.Result.failure("User not found.");

        for (User u : all) {
            if (u.getUserId().equals(updated.getUserId())) continue;
            if (u.getEmail().equalsIgnoreCase(updated.getEmail())) {
                return utils.Result.failure("Email is already in use.");
            }
        }

        User existing = all.get(idx);
        if (managerUserId != null && managerUserId.equals(existing.getUserId()) && !updated.isActive()) {
            return utils.Result.failure("You cannot deactivate your own account.");
        }

        existing.setFullName(updated.getFullName().trim());
        existing.setEmail(updated.getEmail().trim());
        existing.setContact(updated.getContact().trim());
        if (ValidationUtil.isNotEmpty(updated.getPassword())) {
            // If the incoming password is not already hashed (it's new plaintext), hash it
            if (!utils.PasswordHasher.isHashed(updated.getPassword())) {
                existing.setPassword(utils.PasswordHasher.hashPassword(updated.getPassword()));
            } else {
                existing.setPassword(updated.getPassword());
            }
        }
        if ("Technician".equals(existing.getRole())) {
            String serviceType = updated.getTechnicianServiceType();
            if (!"Normal Service".equals(serviceType) && !"Major Service".equals(serviceType)) {
                return utils.Result.failure("Technician service type is required.");
            }
            existing.setTechnicianServiceType(serviceType);
        } else {
            existing.setTechnicianServiceType("-");
        }
        existing.setActive(updated.isActive());

        try {
            userRepository.writeAllUsers(all);
        } catch (IOException e) {
            return utils.Result.failure("Failed to save users: " + e.getMessage());
        }
        return utils.Result.success(null);
    }

    public utils.Result<Void> setUserActive(String userId, boolean active, String managerUserId) {
        if (managerUserId != null && managerUserId.equals(userId) && !active) {
            return utils.Result.failure("You cannot deactivate your own account.");
        }
        List<User> all = listAllUsers();
        boolean found = false;
        for (User u : all) {
            if (u.getUserId().equals(userId)) {
                u.setActive(active);
                found = true;
                break;
            }
        }
        if (!found) return utils.Result.failure("User not found.");
        try {
            userRepository.writeAllUsers(all);
        } catch (IOException e) {
            return utils.Result.failure("Failed to save users: " + e.getMessage());
        }
        return utils.Result.success(null);
    }

    public utils.Result<Void> updateUser(model.users.User user) {
        if (user == null) return utils.Result.failure("User cannot be null.");
        
        // Manual validation to bypass hashed password regex issues
        if (!ValidationUtil.isNotEmpty(user.getFullName())) return utils.Result.failure("Full name is required.");
        if (!ValidationUtil.isValidEmail(user.getEmail())) return utils.Result.failure(ValidationUtil.invalidEmailMessage());
        if (!ValidationUtil.isValidContact(user.getContact())) return utils.Result.failure(ValidationUtil.invalidContactMessage());
        
        // Password validation: only if NOT hashed
        if (user.getPassword() != null && !utils.PasswordHasher.isHashed(user.getPassword())) {
            if (!ValidationUtil.isValidPassword(user.getPassword())) {
                return utils.Result.failure(ValidationUtil.passwordRequirementsMessage());
            }
        }
        
        userRepository.updateUser(user);
        return utils.Result.success(null);
    }

    public utils.Result<Void> deleteUser(String userId) {
        if (userId == null || userId.isEmpty()) return utils.Result.failure("User ID cannot be empty.");
        userRepository.deleteUser(userId);
        return utils.Result.success(null);
    }

    /**
     * Permanently removes a user from storage.
     */
    public utils.Result<Void> deleteUser(String userId, String managerUserId) {
        if (!ValidationUtil.isNotEmpty(userId)) {
            return utils.Result.failure("User ID is required.");
        }
        if (managerUserId != null && managerUserId.equals(userId)) {
            return utils.Result.failure("You cannot delete your own account.");
        }
        List<User> all = listAllUsers();
        boolean removed = all.removeIf(u -> u.getUserId().equals(userId.trim()));
        if (!removed) {
            return utils.Result.failure("User not found.");
        }
        try {
            userRepository.writeAllUsers(all);
        } catch (IOException e) {
            return utils.Result.failure("Failed to save users: " + e.getMessage());
        }
        return utils.Result.success(null);
    }

    public List<User> filterUsers(String searchText, String roleFilter) {
        String q = searchText == null ? "" : searchText.trim().toLowerCase();
        return listAllUsers().stream()
                .filter(u -> roleFilter == null || "ALL".equals(roleFilter) || roleFilter.equals(u.getRole()))
                .filter(u -> q.isEmpty()
                        || u.getUserId().toLowerCase().contains(q)
                        || u.getFullName().toLowerCase().contains(q)
                        || u.getEmail().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public String importUsersFromCsvLines(List<String> lines, boolean replaceAll) {
        if (lines == null || lines.isEmpty()) {
            return "No data to import.";
        }
        List<User> imported = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            if (line.toUpperCase().startsWith("USERID,") || line.toUpperCase().startsWith("\"USERID\"")) {
                continue;
            }
            String[] p = splitCsvLine(line);
            if (p.length < 7) {
                return "Invalid CSV at line " + (i + 1) + " (expected 7 columns, or 8 with legacy username column).";
            }
            String userId = p[0].trim();
            String fullName;
            String email;
            String contact;
            String password;
            String role;
            boolean active;
            if (p.length >= 8) {
                fullName = p[2].trim();
                email = p[3].trim();
                contact = p[4].trim();
                password = p[5].trim();
                role = p[6].trim();
                active = "ACTIVE".equalsIgnoreCase(p[7].trim());
            } else {
                fullName = p[1].trim();
                email = p[2].trim();
                contact = p[3].trim();
                password = p[4].trim();
                role = p[5].trim();
                active = "ACTIVE".equalsIgnoreCase(p[6].trim());
            }
            if (!ValidationUtil.isNotEmpty(userId) || !ValidationUtil.isNotEmpty(fullName)
                    || !ValidationUtil.isNotEmpty(password)) {
                return "Invalid row " + (i + 1) + ": user id, full name, and password required.";
            }
            if (!ValidationUtil.isValidPassword(password)) {
                return "Invalid row " + (i + 1) + ":\n" + ValidationUtil.passwordRequirementsMessage();
            }
            if (!ValidationUtil.isValidContact(contact)) {
                return "Invalid row " + (i + 1) + ":\n" + ValidationUtil.invalidContactMessage();
            }
            if (!ValidationUtil.isValidEmail(email)) {
                return "Invalid row " + (i + 1) + ":\n" + ValidationUtil.invalidEmailMessage();
            }
            if (!isAllowedManagedRole(role)) {
                return "Invalid row " + (i + 1) + ": unknown role.";
            }
            User u = instantiateUser(userId, fullName, email, contact, password, role, "-");
            u.setActive(active);
            imported.add(u);
        }
        try {
            if (replaceAll) {
                userRepository.writeAllUsers(imported);
            } else {
                List<User> merged = new ArrayList<>(listAllUsers());
                for (User nu : imported) {
                    merged.removeIf(u -> u.getUserId().equals(nu.getUserId()));
                    merged.add(nu);
                }
                userRepository.writeAllUsers(merged);
            }
        } catch (IOException e) {
            return "Failed to write file: " + e.getMessage();
        }
        return null;
    }

    public List<String> exportUsersToCsvLines() {
        List<String> lines = new ArrayList<>();
        lines.add("userId,fullName,email,contact,password,role,serviceType,status");
        for (User u : listAllUsers()) {
            lines.add(String.join(",",
                    escapeCsv(u.getUserId()),
                    escapeCsv(u.getFullName()),
                    escapeCsv(u.getEmail()),
                    escapeCsv(u.getContact()),
                    escapeCsv(u.getPassword()),
                    escapeCsv(u.getRole()),
                    escapeCsv("Technician".equals(u.getRole()) ? u.getTechnicianServiceType() : "-"),
                    u.isActive() ? "ACTIVE" : "INACTIVE"));
        }
        return lines;
    }

    /** Minimal CSV split supporting quoted fields. */
    private static String[] splitCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static boolean isAllowedManagedRole(String role) {
        return "Manager".equals(role) || "CounterStaff".equals(role) || "Technician".equals(role)
                || "Customer".equals(role);
    }

    private static User instantiateUser(String userId, String fullName, String email,
                                        String contact, String password, String role, String technicianServiceType) {
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
        if ("Technician".equals(role)) {
            user.setTechnicianServiceType(technicianServiceType);
        } else {
            user.setTechnicianServiceType("-");
        }
        return user;
    }
    public java.util.List<model.users.User> getAllCustomers() {
        java.util.List<model.users.User> all = userRepository.getAllUsers();
        java.util.List<model.users.User> customers = new java.util.ArrayList<>();

        for (model.users.User u : all) {
            if ("Customer".equals(u.getRole())) {
                customers.add(u);
            }
        }
        return customers;
    }

    public void addCustomer(model.users.Customer customer) {
        userRepository.saveUser(customer);
    }
}
