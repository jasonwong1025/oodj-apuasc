package model.users;

public class CounterStaff extends User {
    public CounterStaff() {
        this.setRole(Role.COUNTERSTAFF);
    }

    public CounterStaff(String userId, String fullName, String email, String contact, String password) {
        super(userId, fullName, email, contact, password, Role.COUNTERSTAFF);
    }
}
