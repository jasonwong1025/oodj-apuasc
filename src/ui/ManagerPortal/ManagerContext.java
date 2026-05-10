package ui.ManagerPortal;

import abstracts.AbstractUser;
import service_layer.*;
import javax.swing.JFrame;
import ui.core.PortalContext;

/**
 * Context object for Manager portal components.
 */
public record ManagerContext(
    JFrame owner,
    AbstractUser currentUser,
    UserService userService,
    AppointmentService appointmentService,
    PaymentService paymentService,
    ReviewService reviewService,
    VehicleService vehicleService,
    ServiceService serviceService,
    RegistrationService registrationService,
    FeedbackService feedbackService,
    Runnable refreshAction
) implements PortalContext {}
