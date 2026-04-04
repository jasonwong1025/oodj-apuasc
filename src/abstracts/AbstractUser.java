package abstracts;

public abstract class AbstractUser {
    private String userId;
    private String fullName;
    private String email;
    private String contact;
    private String password;
    private String role;

    public AbstractUser() {
    }

    public AbstractUser(String userId, String fullName, String email, String contact, String password, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.contact = contact;
        this.password = password;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return String.join("|", userId, fullName, email, contact, password, role);
    }
}
