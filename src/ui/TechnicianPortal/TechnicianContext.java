package ui.TechnicianPortal;

import abstracts.AbstractUser;
import service_layer.AppointmentService;
import service_layer.ReviewService;
import service_layer.UserService;
import service_layer.VehicleService;
import javax.swing.JFrame;

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
    Runnable refreshAction
) {}
