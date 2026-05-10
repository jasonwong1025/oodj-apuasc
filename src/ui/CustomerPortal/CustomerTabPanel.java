package ui.CustomerPortal;

import abstracts.AbstractUser;
import java.awt.Frame;
import java.util.List;
import javax.swing.JPanel;
import model.appointment.Appointment;
import model.service.Service;
import model.vehicle.Vehicle;
import service_layer.AppointmentService;
import service_layer.PaymentService;
import service_layer.ReviewService;
import service_layer.ServiceService;
import service_layer.UserService;
import service_layer.VehicleService;
import ui.core.Refreshable;
import ui.shared.SharedStyles;

public abstract class CustomerTabPanel extends JPanel implements Refreshable {
    protected final CustomerContext context;

    protected CustomerTabPanel(CustomerContext context) {
        this.context = context;
        setBackground(SharedStyles.MAIN_BG);
    }

    protected AbstractUser currentUser() {
        return context.getCurrentUser();
    }

    protected AppointmentService appointmentService() {
        return context.getAppointmentService();
    }

    protected ServiceService serviceLookup() {
        return context.getServiceLookup();
    }

    protected VehicleService vehicleService() {
        return context.getVehicleService();
    }

    protected PaymentService paymentService() {
        return context.getPaymentService();
    }

    protected ReviewService reviewService() {
        return context.getReviewService();
    }

    protected UserService userService() {
        return context.getUserService();
    }

    protected String resolveServiceNames(String serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty() || serviceIds.equals("NONE")) return "N/A";
        String[] ids = serviceIds.split(",");
        List<String> names = new java.util.ArrayList<>();
        for (String id : ids) {
            String rawId = id.trim();
            Service s = serviceLookup().findById(rawId);
            if (s == null) {
                String normalizedId = normalizeLegacyServiceId(rawId);
                if (!normalizedId.equals(rawId)) {
                    s = serviceLookup().findById(normalizedId);
                }
            }

            if (s != null) names.add(s.getServiceName());
            else names.add("Unknown Service (" + rawId + ")");
        }
        return String.join(", ", names);
    }

    protected String resolveVehicleInfo(String vehicleId) {
        if (vehicleId == null || vehicleId.isEmpty()) return "N/A";
        Vehicle v = context.getVehicleService().findById(vehicleId);
        if (v != null) {
            return v.getPlateNumber() + " (" + v.getBrand() + " " + v.getModel() + ")";
        }
        return "Unknown Vehicle (" + vehicleId + ")";
    }

    protected String normalizeLegacyServiceId(String serviceId) {
        if (serviceId == null) return "";
        String id = serviceId.trim().toUpperCase();
        if (id.startsWith("SEV") && id.length() > 3) {
            return "SV" + id.substring(3);
        }
        return id;
    }

    protected String getAppointmentCategory(Appointment appointment) {
        if (appointment == null) return "NORMAL";
        String serviceIds = appointment.getServiceId();
        if (serviceIds == null || serviceIds.isEmpty() || serviceIds.equals("NONE")) return "NORMAL";
        
        String[] ids = serviceIds.split(",");
        // If more than 3 services, it's MAJOR
        if (ids.length > 3) return "MAJOR";
        
        for (String id : ids) {
            String rawId = id.trim();
            Service s = serviceLookup().findById(rawId);
            if (s == null) {
                String normalizedId = normalizeLegacyServiceId(rawId);
                if (!normalizedId.equals(rawId)) {
                    s = serviceLookup().findById(normalizedId);
                }
            }
            // If any service is NOT included in normal service, the whole appointment is MAJOR
            if (s != null && !s.isIncludedInNormalService()) return "MAJOR";
            // If service not found, default to MAJOR for safety (or handle as unknown)
            if (s == null) return "MAJOR";
        }
        return "NORMAL";
    }

    protected Appointment findAppointmentById(List<Appointment> list, String appointmentId) {
        if (list == null || appointmentId == null) return null;
        for (Appointment a : list) {
            if (appointmentId.equals(a.getAppointmentId())) return a;
        }
        return null;
    }

    protected String[] showDateRangePicker(String currentFrom, String currentTo) {
        Frame owner = context.getOwner();
        DateRangePickerDialog dialog = new DateRangePickerDialog(owner, currentFrom, currentTo);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) return null;
        return new String[]{dialog.getStartDate(), dialog.getEndDate()};
    }
}
