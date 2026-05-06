package service_layer;

import repository.ReviewRepository;
import utils.IdGenerator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.appointment.Appointment;

import model.feedback.Review;

public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PaymentService paymentService;
    private final AppointmentService appointmentService;

    public ReviewService() {
        this.reviewRepository = new ReviewRepository();
        this.paymentService = new PaymentService();
        this.appointmentService = new AppointmentService();
    }

    public List<Review> getCustomerReviews(String customerId) {
        List<Appointment> customerAppts = appointmentService.getCustomerAppointments(customerId);
        List<String> apptIds = new ArrayList<>();
        for (Appointment a : customerAppts) apptIds.add(a.getAppointmentId());

        List<Review> all = reviewRepository.getAllReviews();
        List<Review> filtered = new ArrayList<>();
        for (Review r : all) {
            if (apptIds.contains(r.getAppointmentId())) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    public List<Review> getAllReviews() {
        return reviewRepository.getAllReviews();
    }

    public List<String[]> getAllReviewRows() {
        List<String[]> rows = new java.util.ArrayList<>();
        for (Review review : reviewRepository.getAllReviews()) {
            rows.add(new String[]{
                    review.getReviewId(),
                    review.getAppointmentId(),
                    review.getCustomerId(),
                    String.valueOf(review.getRating()),
                    review.getDescription() == null ? "" : review.getDescription(),
                    review.getDate() == null ? "" : review.getDate()
            });
        }
        return rows;
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
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Review review = new Review(reviewId, appointmentId, rating, description, dateTime);
        reviewRepository.save(review);
        return "Success: Review submitted.";
    }
}
