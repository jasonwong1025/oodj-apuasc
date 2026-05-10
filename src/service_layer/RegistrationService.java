package service_layer;

import model.users.Customer;
import model.users.User;
import repository.UserFileRepository;
import utils.IdGenerator;
import utils.ValidationUtil;
import utils.PasswordHasher;

import java.util.List;

public class RegistrationService {
    private UserFileRepository userRepository;

    public RegistrationService() {
        this.userRepository = new UserFileRepository();
    }

    /**
     * Registers a new customer.
     * @return Result containing the customer or an error message.
     */
    public utils.Result<Customer> registerCustomer(String fullName, String email, String contact, String password, String confirmPassword) {
        // 1. Basic matching & Presence checks
        if (!ValidationUtil.isNotEmpty(password) || !password.equals(confirmPassword)) {
            return utils.Result.failure("Passwords do not match or are empty.");
        }

        // 2. STRENGTH CHECK (Plaintext) - Done BEFORE hashing
        if (!ValidationUtil.isValidPassword(password)) {
            return utils.Result.failure(ValidationUtil.passwordRequirementsMessage());
        }

        // 3. Email Uniqueness check
        List<User> existingUsers = userRepository.getAllUsers();
        for (User u : existingUsers) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return utils.Result.failure("Email is already registered.");
            }
        }

        // 4. Hash password and generate ID
        String hashedPassword = PasswordHasher.hashPassword(password);
        String newId = IdGenerator.generateNextCustomerId();
        Customer newCustomer = new Customer(newId, fullName, email, contact, hashedPassword);

        // 5. Use annotation-based validation for other fields
        String violations = ValidationUtil.getViolations(newCustomer);
        if (violations != null) {
            return utils.Result.failure(violations);
        }

        // 6. Save
        userRepository.saveUser(newCustomer);

        return utils.Result.success(newCustomer);
    }
}
