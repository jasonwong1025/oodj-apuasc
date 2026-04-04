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
    public String registerCustomer(String fullName, String email, String contact, String password, String confirmPassword) {
        // 1. Validate empty fields
        if (!ValidationUtil.isNotEmpty(fullName) ||
            !ValidationUtil.isNotEmpty(email) || !ValidationUtil.isNotEmpty(contact) ||
            !ValidationUtil.isNotEmpty(password) || !ValidationUtil.isNotEmpty(confirmPassword)) {
            return "All fields are required.";
        }

        // 2. Validate email format
        if (!ValidationUtil.isValidEmail(email)) {
            return "Please enter a valid email address.";
        }

        // 3. Validate password strength
        if (!ValidationUtil.isValidPassword(password)) {
            return "Password must be at least 6 characters long, contain an uppercase letter, a lowercase letter, a number, and a special character.\n(Example: Password1!)";
        }

        // 4. Validate password and confirm password match
        if (!password.equals(confirmPassword)) {
            return "Password and Confirm Password do not match.";
        }

        // 5. Check uniqueness (email)
        List<User> existingUsers = userRepository.getAllUsers();
        for (User u : existingUsers) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return "Email is already registered.";
            }
        }

        // 6. Generate ID
        String newId = IdGenerator.generateNextCustomerId();

        // 7. Create Customer
        Customer newCustomer = new Customer(newId, fullName, email, contact, password);

        // 8. Save
        userRepository.saveUser(newCustomer);

        return null; // Return null on success
    }
}
