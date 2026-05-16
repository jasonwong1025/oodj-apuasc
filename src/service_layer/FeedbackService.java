package service_layer;

import model.feedback.Feedback;
import repository.FeedbackRepository;
import utils.Result;
import java.util.List;

public class FeedbackService {
    private final FeedbackRepository feedbackRepository;

    public FeedbackService() {
        this.feedbackRepository = new FeedbackRepository();
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.getAll();
    }

    public Feedback findByAppointmentId(String apptId) {
        if (apptId == null || apptId.trim().isEmpty()) return null;
        return feedbackRepository.findByAppointmentId(apptId.trim());
    }

    public Result<Void> saveFeedback(String apptId, String description, String dateTime) {
        if (apptId == null || apptId.trim().isEmpty()) {
            return Result.failure("Appointment ID is required.");
        }
        if (description == null || description.trim().isEmpty()) {
            return Result.failure("Feedback description cannot be empty.");
        }

        Feedback fb = feedbackRepository.findByAppointmentId(apptId);
        if (fb != null) {
            return Result.failure("Feedback has already been submitted for this appointment.");
        }
        fb = new Feedback(feedbackRepository.generateNextId(), apptId, description, dateTime);

        feedbackRepository.addOrUpdate(fb);
        return Result.success(null);
    }

    public List<String[]> getAllFeedbackRows() {
        List<String[]> rows = new java.util.ArrayList<>();
        for (Feedback fb : feedbackRepository.getAll()) {
            rows.add(new String[]{
                fb.getFeedbackId(),
                fb.getAppointmentId(),
                fb.getDescription(),
                fb.getDateTime()
            });
        }
        return rows;
    }
}
