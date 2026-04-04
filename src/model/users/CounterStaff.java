package model.users;

public class CounterStaff extends User {
    public CounterStaff() {
        super();
        this.setRole("CounterStaff");
    }

    public CounterStaff(String userId, String fullName, String email, String contact, String password) {
        super(userId, fullName, email, contact, password, "CounterStaff");
    }
}
