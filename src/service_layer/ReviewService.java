package service_layer;

import repository.ReviewRepository;
import utils.IdGenerator;
import java.time.LocalDate;
import java.util.List;

import model.feedback.Review;

public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PaymentService paymentService;

    public ReviewService() {
        this.reviewRepository = new ReviewRepository();
        this.paymentService = new PaymentService();
    }

    public List<Review> getCustomerReviews(String customerId) {
        return reviewRepository.getReviewsByCustomer(customerId);
    }

    public String submitReview(String customerId, String appointmentId, int rating, String description) {
        if (!paymentService.isPaid(appointmentId)) {
            return "Error: You can only provide a review after payment is made.";
        }

        // Check if review already exists
        List<Review> existing = reviewRepository.getAllReviews();
        for (Review r : existing) {
            if (r.getAppointmentId().equals(appointmentId)) {
                return "Error: Review already submitted for this appointment.";
            }
        }

        String reviewId = IdGenerator.generateId("REV", "data/reviews.txt");
        String date = LocalDate.now().toString();
        Review review = new Review(reviewId, appointmentId, customerId, rating, description, date);
        reviewRepository.save(review);
        return "Success: Review submitted.";
    }
}
