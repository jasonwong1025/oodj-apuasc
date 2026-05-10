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

        // 2. Generate ID temporarily to create object for validation
        String tempId = "TEMP";
        Customer newCustomer = new Customer(tempId, fullName, email, contact, password);

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

        // 6. Generate real ID and update object
        String newId = IdGenerator.generateNextCustomerId();
        newCustomer.setUserId(newId);

        // 7. Save
        userRepository.saveUser(newCustomer);

        return utils.Result.success(newCustomer);
    }
}
