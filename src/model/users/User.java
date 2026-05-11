package model.users;

import abstracts.AbstractUser;

public class User extends AbstractUser {
    public User() {
        super();
    }

    public User(String userId, String fullName, String email, String contact, String password, model.users.Role role) {
        super(userId, fullName, email, contact, password, role);
    }
}
