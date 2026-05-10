package ui.core;

import abstracts.AbstractUser;
import service_layer.*;
import javax.swing.JFrame;

/**
 * Shared interface for all portal contexts, providing access to services and shared state.
 */
public interface PortalContext {
    JFrame owner();
    AbstractUser currentUser();
    UserService userService();
    AppointmentService appointmentService();
    PaymentService paymentService();
    ReviewService reviewService();
    VehicleService vehicleService();
    ServiceService serviceService();
    RegistrationService registrationService();
    FeedbackService feedbackService();
    Runnable refreshAction();
}
