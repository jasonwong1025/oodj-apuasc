package model.users;

public class Technician extends User {
    public Technician() {
        this.setRole(Role.TECHNICIAN);
    }

    public Technician(String userId, String fullName, String email, String contact, String password) {
        super(userId, fullName, email, contact, password, Role.TECHNICIAN);
    }
}
