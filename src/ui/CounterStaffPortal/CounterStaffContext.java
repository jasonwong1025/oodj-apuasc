package ui.CounterStaffPortal;

import abstracts.AbstractUser;
import service_layer.*;
import javax.swing.JFrame;

/**
 * Context object to share state and services among Counter Staff portal components.
 */
public record CounterStaffContext(
    JFrame owner,
    AbstractUser currentUser,
    UserService userService,
    AppointmentService appointmentService,
    PaymentService paymentService,
    ReviewService reviewService,
    VehicleService vehicleService,
    ServiceService serviceService,
    RegistrationService registrationService,
    Runnable refreshAction
) {}
