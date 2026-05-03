package model.feedback;

public class Feedback {
    private String feedbackId;
    private String appointmentId;
    private String description;
    private String type; // e.g., "Technician Feedback"

    public Feedback() {}

    public Feedback(String feedbackId, String appointmentId, String description, String type) {
        this.feedbackId = feedbackId;
        this.appointmentId = appointmentId;
        this.description = description;
        this.type = type;
    }

    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return String.join("|", feedbackId, appointmentId, description, type);
    }
}
