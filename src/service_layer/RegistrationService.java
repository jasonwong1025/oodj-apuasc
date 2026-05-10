package service_layer;

import model.users.Customer;
import model.users.User;
import repository.UserFileRepository;
import utils.IdGenerator;
import utils.ValidationUtil;

import java.util.List;

public class RegistrationService {
    private UserFileRepository userRepository;

    public RegistrationService() {
        this.userRepository = new UserFileRepository();
    }

    /**
     * Registers a new customer.
     * @return null if successful, or an error message if validation fails.
     */
    public utils.Result<Customer> registerCustomer(String fullName, String email, String contact, String password, String confirmPassword) {
        // 1. Validate password match first
        if (!ValidationUtil.isNotEmpty(password) || !password.equals(confirmPassword)) {
            return utils.Result.failure("Passwords do not match or are empty.");
        }

        // 2. Hash password and generate real ID
        String hashedPassword = utils.PasswordHasher.hashPassword(password);
        String newId = IdGenerator.generateNextCustomerId();
        Customer newCustomer = new Customer(newId, fullName, email, contact, hashedPassword);

        // 3. Use annotation-based validation
        String violations = ValidationUtil.getViolations(newCustomer);
        if (violations != null) {
            return utils.Result.failure(violations);
        }

        // 4. Manual password strength check (if not fully covered by annotations)
        if (!ValidationUtil.isValidPassword(password)) {
            return utils.Result.failure(ValidationUtil.passwordRequirementsMessage());
        }

        // 5. Check uniqueness (email)
        List<User> existingUsers = userRepository.getAllUsers();
        for (User u : existingUsers) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return utils.Result.failure("Email is already registered.");
            }
        }

        // 7. Save
        userRepository.saveUser(newCustomer);

        return utils.Result.success(newCustomer);
    }
}
