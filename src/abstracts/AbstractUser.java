package abstracts;

public abstract class AbstractUser {
    private String userId;
    /** Login name (distinct from email in UI; may match email for legacy rows). */
    private String username;
    private String fullName;
    private String email;
    private String contact;
    private String password;
    private String role;
    private boolean active = true;

    public AbstractUser() {
    }

    public AbstractUser(String userId, String fullName, String email, String contact, String password, String role) {
        this.userId = userId;
        this.username = email;
        this.fullName = fullName;
        this.email = email;
        this.contact = contact;
        this.password = password;
        this.role = role;
        this.active = true;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Pipe-delimited line for {@code data/users.txt} (8 fields).
     */
    @Override
    public String toString() {
        String uname = (username != null && !username.trim().isEmpty()) ? username.trim() : email;
        return String.join("|", userId, uname, fullName, email, contact, password, role,
                active ? "ACTIVE" : "INACTIVE");
    }
}
