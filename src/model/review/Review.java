package model.review;

public class Review {
    private String reviewId;
    private String appointmentId;
    private String customerId;
    private int rating; // 1-5
    private String description;
    private String date;

    public Review() {}

    public Review(String reviewId, String appointmentId, String customerId, int rating, String description, String date) {
        this.reviewId = reviewId;
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.rating = rating;
        this.description = description;
        this.date = date;
    }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    @Override
    public String toString() {
        return String.join("|", reviewId, appointmentId, customerId, String.valueOf(rating), description, date);
    }
}
