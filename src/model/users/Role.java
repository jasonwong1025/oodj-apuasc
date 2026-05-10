package model.users;

public enum Role {
    CUSTOMER("Customer"),
    MANAGER("Manager"),
    TECHNICIAN("Technician"),
    COUNTERSTAFF("CounterStaff");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Role fromString(String text) {
        for (Role r : Role.values()) {
            if (r.label.equalsIgnoreCase(text) || r.name().equalsIgnoreCase(text)) {
                return r;
            }
        }
        return null;
    }
}
