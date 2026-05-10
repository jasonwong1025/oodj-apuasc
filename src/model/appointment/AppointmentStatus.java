package model.appointment;

public enum AppointmentStatus {
    PENDING("PENDING"),
    CONFIRMED("CONFIRMED"),
    IN_PROGRESS("IN PROGRESS"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED");

    private final String label;

    AppointmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AppointmentStatus fromString(String text) {
        for (AppointmentStatus s : AppointmentStatus.values()) {
            if (s.label.equalsIgnoreCase(text) || s.name().equalsIgnoreCase(text)) {
                return s;
            }
        }
        return PENDING;
    }
}
