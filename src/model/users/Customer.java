package model.users;

public class Customer extends User {
    public Customer() {
        this.setRole(Role.CUSTOMER);
    }

    public Customer(String userId, String fullName, String email, String contact, String password) {
        super(userId, fullName, email, contact, password, Role.CUSTOMER);
    }
}
