package ui.TechnicianPortal;

import abstracts.AbstractUser;
import service_layer.*;
import javax.swing.JFrame;
import ui.core.PortalContext;

/**
 * Context object to share state and services among Technician portal components.
 */
public record TechnicianContext(
    JFrame owner,
    AbstractUser currentUser,
    UserService userService,
    AppointmentService appointmentService,
    ReviewService reviewService,
    VehicleService vehicleService,
    ServiceService serviceService,
    PaymentService paymentService,
    RegistrationService registrationService,
    FeedbackService feedbackService,
    Runnable refreshAction
) implements PortalContext {}
