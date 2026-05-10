package model.appointment;

public enum AppointmentType {
    NORMAL("NORMAL"),
    MAJOR("MAJOR");

    private final String label;

    AppointmentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AppointmentType fromString(String text) {
        for (AppointmentType t : AppointmentType.values()) {
            if (t.label.equalsIgnoreCase(text) || t.name().equalsIgnoreCase(text)) {
                return t;
            }
        }
        return NORMAL;
    }
}
