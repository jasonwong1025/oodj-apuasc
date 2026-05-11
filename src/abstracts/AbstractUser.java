package abstracts;

import utils.validation.NotBlank;

public abstract class AbstractUser {
    private String userId;

    @NotBlank(message = "is required")
    private String fullName;

    @NotBlank(message = "is required")
    @utils.validation.Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "must be a valid email address")
    private String email;

    @NotBlank(message = "is required")
    @utils.validation.Pattern(regexp = "^\\d{10,11}$", message = "must be 10-11 digits")
    private String contact;

    @NotBlank(message = "is required")
    private String password;

    private model.users.Role role;
    private String technicianServiceType = "-";
    private boolean active = true;

    public AbstractUser() {
    }

    public AbstractUser(String userId, String fullName, String email, String contact, String password, model.users.Role role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.contact = contact;
        this.password = password;
        this.role = role;
        this.technicianServiceType = (role == model.users.Role.TECHNICIAN) ? "" : "-";
        this.active = true;
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

    public model.users.Role getRole() {
        return role;
    }

    public void setRole(model.users.Role role) {
        this.role = role;
    }

    public String getTechnicianServiceType() {
        return technicianServiceType;
    }

    public void setTechnicianServiceType(String technicianServiceType) {
        this.technicianServiceType = technicianServiceType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        String serviceType = technicianServiceType == null || technicianServiceType.trim().isEmpty()
                ? "-"
                : technicianServiceType.trim();
        return utils.FileStorageHelper.join(
                userId, fullName, email, contact, password, role != null ? role.getLabel() : "", serviceType, active ? "ACTIVE" : "INACTIVE"
        );
    }
}
