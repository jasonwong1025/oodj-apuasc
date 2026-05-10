package model.feedback;

public class Feedback {
    private String feedbackId;
    private String appointmentId;
    private String description;
    private String dateTime;

    public Feedback() {}

    public Feedback(String feedbackId, String appointmentId, String description, String dateTime) {
        this.feedbackId = feedbackId;
        this.appointmentId = appointmentId;
        this.description = description;
        this.dateTime = dateTime;
    }

    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    @Override
    public String toString() {
        return utils.FileStorageHelper.join(feedbackId, appointmentId, description, dateTime);
    }
}
