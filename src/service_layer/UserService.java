package service_layer;

import abstracts.AbstractUser;
import repository.UserFileRepository;

public class UserService {
    private UserFileRepository userRepository;

    public UserService() {
        this.userRepository = new UserFileRepository();
    }

    public AbstractUser login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Email cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("Password cannot be empty.");
        }

        AbstractUser user = userRepository.authenticateUser(email, password);
        if (user == null) {
            throw new Exception("Invalid email or password.");
        }
        return user;
    }
    //Sang Yew Changes - Edit Profile
    public void updateUser(model.users.User user) {
    userRepository.updateUser(user);
}
    //Sang Yew Changes - Manage Customer
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

public void deleteUser(String userId) {
    userRepository.deleteUser(userId);
}
}
