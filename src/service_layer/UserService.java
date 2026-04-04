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
}
