package model.users;

public class Manager extends User {
    public Manager() {
        this.setRole(Role.MANAGER);
    }

    public Manager(String userId, String fullName, String email, String contact, String password) {
        super(userId, fullName, email, contact, password, Role.MANAGER);
    }
}
