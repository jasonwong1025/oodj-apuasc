package model.users;

public class Customer extends User {
    public Customer() {
        super();
        this.setRole("Customer");
    }

    public Customer(String userId, String fullName, String email, String contact, String password) {
        super(userId, fullName, email, contact, password, "Customer");
    }
}
