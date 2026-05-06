package service_layer;

import model.feedback.Feedback;
import repository.FeedbackRepository;

import java.util.ArrayList;
import java.util.List;

public class FeedbackService {
    private final FeedbackRepository feedbackRepository;

    public FeedbackService() {
        this.feedbackRepository = new FeedbackRepository();
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.getAll();
    }

    /**
     * Row order: feedbackId, appointmentId, description, type (for UI callers that avoid {@link Feedback}).
     */
    public List<String[]> getAllFeedbackRows() {
        List<String[]> rows = new ArrayList<>();
        for (Feedback f : feedbackRepository.getAll()) {
            rows.add(new String[]{
                    f.getFeedbackId(),
                    f.getAppointmentId(),
                    f.getDescription() == null ? "" : f.getDescription(),
                    f.getType() == null ? "" : f.getType()
            });
        }
        return rows;
    }
}
