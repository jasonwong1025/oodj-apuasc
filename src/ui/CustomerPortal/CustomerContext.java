package ui.CustomerPortal;

import abstracts.AbstractUser;
import javax.swing.JFrame;
import service_layer.AppointmentService;
import service_layer.PaymentService;
import service_layer.ReviewService;
import service_layer.ServiceService;
import service_layer.UserService;
import service_layer.VehicleService;

public class CustomerContext {
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

    public JFrame getOwner() {
        return owner;
    }

    public AbstractUser getCurrentUser() {
        return currentUser;
    }

    public VehicleService getVehicleService() {
        return vehicleService;
    }

    public AppointmentService getAppointmentService() {
        return appointmentService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public ReviewService getReviewService() {
        return reviewService;
    }

    public ServiceService getServiceLookup() {
        return serviceLookup;
    }

    public UserService getUserService() {
        return userService;
    }

    public Runnable getRefreshAction() {
        return refreshAction;
    }

    public CustomerNavigator getNavigator() {
        return navigator;
    }
}
