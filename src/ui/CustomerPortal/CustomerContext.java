package ui.CustomerPortal;

import abstracts.AbstractUser;
import javax.swing.JFrame;
import service_layer.*;
import ui.core.PortalContext;

public class CustomerContext implements PortalContext {
    private final JFrame owner;
    private final AbstractUser currentUser;
    private final VehicleService vehicleService;
    private final AppointmentService appointmentService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final ServiceService serviceLookup;
    private final UserService userService;
    private final Runnable refreshAction;
    private final CustomerNavigator navigator;

    public CustomerContext(JFrame owner,
                           AbstractUser currentUser,
                           VehicleService vehicleService,
                           AppointmentService appointmentService,
                           PaymentService paymentService,
                           ReviewService reviewService,
                           ServiceService serviceLookup,
                           UserService userService,
                           Runnable refreshAction,
                           CustomerNavigator navigator) {
        this.owner = owner;
        this.currentUser = currentUser;
        this.vehicleService = vehicleService;
        this.appointmentService = appointmentService;
        this.paymentService = paymentService;
        this.reviewService = reviewService;
        this.serviceLookup = serviceLookup;
        this.userService = userService;
        this.refreshAction = refreshAction;
        this.navigator = navigator;
    }

    @Override public JFrame owner() { return owner; }
    @Override public AbstractUser currentUser() { return currentUser; }
    @Override public UserService userService() { return userService; }
    @Override public AppointmentService appointmentService() { return appointmentService; }
    @Override public PaymentService paymentService() { return paymentService; }
    @Override public ReviewService reviewService() { return reviewService; }
    @Override public VehicleService vehicleService() { return vehicleService; }
    @Override public ServiceService serviceService() { return serviceLookup; }
    @Override public RegistrationService registrationService() { return new RegistrationService(); }
    @Override public FeedbackService feedbackService() { return new FeedbackService(); }
    @Override public Runnable refreshAction() { return refreshAction; }

    // Legacy getters for backward compatibility
    public JFrame getOwner() { return owner; }
    public AbstractUser getCurrentUser() { return currentUser; }
    public VehicleService getVehicleService() { return vehicleService; }
    public AppointmentService getAppointmentService() { return appointmentService; }
    public PaymentService getPaymentService() { return paymentService; }
    public ReviewService getReviewService() { return reviewService; }
    public ServiceService getServiceLookup() { return serviceLookup; }
    public UserService getUserService() { return userService; }
    public Runnable getRefreshAction() { return refreshAction; }
    public CustomerNavigator getNavigator() { return navigator; }
}
