package service_layer;

import model.appointment.Appointment;
import model.vehicle.Vehicle;
import repository.AppointmentRepository;
import repository.VehicleRepository;
import utils.IdGenerator;
import java.util.List;

public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final VehicleRepository vehicleRepository;

    public AppointmentService() {
        this.appointmentRepository = new AppointmentRepository();
        this.vehicleRepository = new VehicleRepository();
    }

    public List<Appointment> getCustomerAppointments(String customerId) {
        return appointmentRepository.getAppointmentsByCustomer(customerId);
    }

    public String bookAppointment(String customerId, String vehicleId, String serviceId, String date, String time) {
        // Check if vehicle exists and belongs to customer
        List<Vehicle> customerVehicles = vehicleRepository.getVehiclesByOwner(customerId);
        boolean ownsVehicle = false;
        for (Vehicle v : customerVehicles) {
            if (v.getVehicleId().equals(vehicleId)) {
                ownsVehicle = true;
                break;
            }
        }

        if (!ownsVehicle) {
            return "Error: You must select a registered vehicle.";
        }

        String appointmentId = IdGenerator.generateId("APT", "data/appointments.txt");
        Appointment appointment = new Appointment(appointmentId, customerId, vehicleId, serviceId, date, time, "PENDING", "NONE");
        appointmentRepository.save(appointment);
        return "Success: Appointment booked.";
    }

    public void cancelAppointment(String appointmentId) {
        List<Appointment> all = appointmentRepository.getAllAppointments();
        for (Appointment a : all) {
            if (a.getAppointmentId().equals(appointmentId) && a.getStatus().equals("PENDING")) {
                a.setStatus("CANCELLED");
                appointmentRepository.update(a);
                break;
            }
        }
    }
}
