package model.feedback;

public class Review {
    private String reviewId;
    private String appointmentId;
    private int rating; // 1-5
    private String description;
    private String dateTime;

    public Review() {}

    public Review(String reviewId, String appointmentId, int rating, String description, String dateTime) {
        this.reviewId = reviewId;
        this.appointmentId = appointmentId;
        this.rating = rating;
        this.description = description;
        this.dateTime = dateTime;
    }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getDate() { return dateTime; }
    public void setDate(String date) { this.dateTime = date; }

    @Override
    public String toString() {
        return utils.FileStorageHelper.join(reviewId, appointmentId, rating, description, dateTime);
    }
}
