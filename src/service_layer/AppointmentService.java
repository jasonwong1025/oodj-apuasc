package service_layer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.appointment.Appointment;
import model.vehicle.Vehicle;
import repository.AppointmentRepository;
import repository.VehicleRepository;
import utils.IdGenerator;

public class AppointmentService {
    public static final int SLOT_CAPACITY = 5;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> ALLOWED_SLOT_TIMES;

    static {
        List<String> slots = new ArrayList<>();
        LocalTime start = LocalTime.of(8, 30);
        for (int i = 0; i < 9; i++) {
            slots.add(start.plusHours(i).format(TIME_FORMATTER));
        }
        ALLOWED_SLOT_TIMES = Collections.unmodifiableList(slots);
    }

    private final AppointmentRepository appointmentRepository;
    private final VehicleRepository vehicleRepository;

    public AppointmentService() {
        this.appointmentRepository = new AppointmentRepository();
        this.vehicleRepository = new VehicleRepository();
    }

    public List<Appointment> getCustomerAppointments(String customerId) {
        return appointmentRepository.getAppointmentsByCustomer(customerId);
    }

    public static List<String> getAllowedSlotTimes() {
        return ALLOWED_SLOT_TIMES;
    }

    public boolean isAllowedSlotTime(String time) {
        return time != null && ALLOWED_SLOT_TIMES.contains(time.trim());
    }

    public int countPendingAppointmentsForSlot(String date, String time) {
        int count = 0;
        for (Appointment a : appointmentRepository.getAllAppointments()) {
            if (!"PENDING".equalsIgnoreCase(a.getStatus())) continue;
            if (date.equals(a.getDate()) && time.equals(a.getTime())) {
                count++;
            }
        }
        return count;
    }

    public boolean isSlotAvailable(String date, String time) {
        return countPendingAppointmentsForSlot(date, time) < SLOT_CAPACITY;
    }

    public String validateSchedule(String date, String time) {
        LocalDate pickedDate;
        LocalTime pickedTime;
        try {
            pickedDate = LocalDate.parse(date, DATE_FORMATTER);
            pickedTime = LocalTime.parse(time, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return "Error: Invalid date/time format.";
        }

        if (!isAllowedSlotTime(time)) {
            return "Error: Appointment time must be one of the allowed slots (08:30 to 16:30).";
        }

        LocalDateTime picked = LocalDateTime.of(pickedDate, pickedTime);
        if (picked.isBefore(LocalDateTime.now())) {
            return "Error: You cannot book an appointment in the past.";
        }

        if (!isSlotAvailable(date, time)) {
            return "Error: This time slot is full. Please choose another slot.";
        }

        return null;
    }

    public String bookAppointment(String customerId, String vehicleId, List<String> serviceIds, String date, String time) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return "Error: You must select at least one service.";
        }
        
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

        String scheduleError = validateSchedule(date, time);
        if (scheduleError != null) {
            return scheduleError;
        }

        String appointmentId = IdGenerator.generateId("APT", "data/appointments.txt");
        // Join multiple service IDs with commas
        String serviceIdStr = String.join(",", serviceIds);
        
        Appointment appointment = new Appointment(appointmentId, customerId, vehicleId, serviceIdStr, date, time, "PENDING", "NONE", "NONE");
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
    public java.util.List<model.appointment.Appointment> getAllAppointments() {
    return appointmentRepository.getAllAppointments();
}
}
