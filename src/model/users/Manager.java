package model.users;

public class Manager extends User {
    public Manager() {
        super();
        this.setRole("Manager");
    }

    public Manager(String userId, String fullName, String email, String contact, String password) {
        super(userId, fullName, email, contact, password, "Manager");
    }
}
