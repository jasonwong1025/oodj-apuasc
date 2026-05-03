package service_layer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.appointment.Appointment;
import model.users.User;
import model.vehicle.Vehicle;
import repository.AppointmentRepository;
import repository.ServiceFileRepository;
import repository.UserFileRepository;
import repository.VehicleRepository;
import utils.IdGenerator;

public class AppointmentService {
    // Capacities are computed dynamically from active technicians' `technicianServiceType`.
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MAJOR_DURATION_SLOTS = 3;
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
    private final ServiceFileRepository serviceRepository;
    private final UserFileRepository userRepository;

    public AppointmentService() {
        this.appointmentRepository = new AppointmentRepository();
        this.vehicleRepository = new VehicleRepository();
        this.serviceRepository = new ServiceFileRepository();
        this.userRepository = new UserFileRepository();
    }

    public enum SlotType {
        NORMAL,
        MAJOR
    }

    public static final class SlotCapacity {
        private final int majorCount;
        private final int normalCount;

        public SlotCapacity(int majorCount, int normalCount) {
            this.majorCount = majorCount;
            this.normalCount = normalCount;
        }

        public int getMajorCount() {
            return majorCount;
        }

        public int getNormalCount() {
            return normalCount;
        }

        public int getTotalCount() {
            return majorCount + normalCount;
        }
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

    public SlotCapacity getSlotCapacity(String date, String time) {
        Set<String> majorServiceIds = getMajorServiceIds();
        int majorCount = 0;
        int normalCount = 0;

        for (Appointment a : appointmentRepository.getAllAppointments()) {
            if (!"PENDING".equalsIgnoreCase(a.getStatus())) continue;
            if (!date.equals(a.getDate())) continue;

            SlotType type = classifyAppointmentType(a.getServiceId(), majorServiceIds);
            if (type == SlotType.MAJOR) {
                if (isMajorAppointmentCoveringSlot(a.getTime(), time)) {
                    majorCount++;
                }
            } else {
                if (time.equals(a.getTime())) {
                    normalCount++;
                }
            }
        }

        return new SlotCapacity(majorCount, normalCount);
    }

    public int countPendingAppointmentsForSlot(String date, String time) {
        return getSlotCapacity(date, time).getTotalCount();
    }

    public boolean isSlotAvailable(String date, String time) {
        return isSlotAvailable(date, time, SlotType.NORMAL) || isSlotAvailable(date, time, SlotType.MAJOR);
    }

    public boolean isSlotAvailable(String date, String time, SlotType slotType) {
        if (slotType == SlotType.MAJOR) {
            return isMajorSlotWindowAvailable(date, time);
        }
        return isNormalSlotAvailable(date, time);
    }

    private boolean isNormalSlotAvailable(String date, String time) {
        SlotCapacity capacity = getSlotCapacity(date, time);
        int majorLimit = getCapacityLimitForSlotType(SlotType.MAJOR);
        int normalLimit = getCapacityLimitForSlotType(SlotType.NORMAL);
        int totalLimit = majorLimit + normalLimit;

        if (capacity.getTotalCount() >= totalLimit) {
            return false;
        }
        return capacity.getNormalCount() < normalLimit;
    }

    private boolean isMajorSlotWindowAvailable(String date, String startTime) {
        List<String> slots = getAllowedSlotTimes();
        int startIndex = slots.indexOf(startTime);
        if (startIndex < 0 || startIndex + MAJOR_DURATION_SLOTS > slots.size()) {
            return false;
        }

        int majorLimit = getCapacityLimitForSlotType(SlotType.MAJOR);
        int normalLimit = getCapacityLimitForSlotType(SlotType.NORMAL);
        int totalLimit = majorLimit + normalLimit;

        for (int i = 0; i < MAJOR_DURATION_SLOTS; i++) {
            String time = slots.get(startIndex + i);
            SlotCapacity capacity = getSlotCapacity(date, time);
            if (capacity.getTotalCount() >= totalLimit) {
                return false;
            }
            if (capacity.getMajorCount() >= majorLimit) {
                return false;
            }
        }
        return true;
    }

    public int getCapacityLimitForSlotType(SlotType slotType) {
        List<User> users = userRepository.getAllUsers();
        int count = 0;
        for (User u : users) {
            if (u == null) continue;
            if (u.getRole() == null) continue;
            if (!"Technician".equalsIgnoreCase(u.getRole())) continue;
            if (!u.isActive()) continue;
            String svc = u.getTechnicianServiceType();
            if (svc == null) continue;
            String norm = svc.trim().toLowerCase();
            if (slotType == SlotType.MAJOR) {
                if (norm.contains("major")) count++;
            } else {
                if (norm.contains("normal")) count++;
            }
        }
        // Fallback to at least 1 to avoid blocking all bookings accidentally
        return Math.max(1, count);
    }

    public int getTotalCapacityLimit() {
        return getCapacityLimitForSlotType(SlotType.MAJOR) + getCapacityLimitForSlotType(SlotType.NORMAL);
    }

    public String validateSchedule(String date, String time) {
        return validateSchedule(date, time, SlotType.NORMAL);
    }

    public String validateSchedule(String date, String time, SlotType slotType) {
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

        if (slotType == SlotType.MAJOR && !isMajorSlotWindowAvailable(date, time)) {
            return "Error: Major service requires a 3-hour block. Please choose an earlier slot.";
        }

        LocalDateTime picked = LocalDateTime.of(pickedDate, pickedTime);
        if (picked.isBefore(LocalDateTime.now())) {
            return "Error: You cannot book an appointment in the past.";
        }

        if (!isSlotAvailable(date, time, slotType)) {
            return "Error: This time slot is full. Please choose another slot.";
        }

        return null;
    }

    private boolean isMajorAppointmentCoveringSlot(String startTime, String slotTime) {
        List<String> slots = getAllowedSlotTimes();
        int startIndex = slots.indexOf(startTime);
        int slotIndex = slots.indexOf(slotTime);
        if (startIndex < 0 || slotIndex < 0) return false;
        return slotIndex >= startIndex && slotIndex < startIndex + MAJOR_DURATION_SLOTS;
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

        SlotType requestedType = determineRequestedSlotType(serviceIds);

        String scheduleError = validateSchedule(date, time, requestedType);
        if (scheduleError != null) {
            return scheduleError;
        }

        String appointmentId = IdGenerator.generateId("APT", "data/appointments.txt");
        // Join multiple service IDs with commas
        String serviceIdStr = String.join(",", serviceIds);
        
        Appointment appointment = new Appointment(appointmentId, customerId, vehicleId, serviceIdStr, date, time, "PENDING", "NONE", requestedType.name());
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

    public SlotType determineRequestedSlotType(List<String> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return SlotType.NORMAL;
        }
        if (serviceIds.size() > 3) {
            return SlotType.MAJOR;
        }
        Set<String> majorServiceIds = getMajorServiceIds();
        for (String id : serviceIds) {
            if (majorServiceIds.contains(id)) {
                return SlotType.MAJOR;
            }
        }
        return SlotType.NORMAL;
    }

    private SlotType classifyAppointmentType(String serviceIdCsv, Set<String> majorServiceIds) {
        if (serviceIdCsv == null || serviceIdCsv.isBlank() || "NONE".equalsIgnoreCase(serviceIdCsv)) {
            return SlotType.NORMAL;
        }
        String[] ids = serviceIdCsv.split(",");
        if (ids.length > 3) {
            return SlotType.MAJOR;
        }
        for (String rawId : ids) {
            String id = rawId.trim();
            if (majorServiceIds.contains(id)) {
                return SlotType.MAJOR;
            }
        }
        return SlotType.NORMAL;
    }

    private Set<String> getMajorServiceIds() {
        Set<String> major = new HashSet<>();
        serviceRepository.getAll().stream()
                .filter(s -> !s.isIncludedInNormalService())
                .forEach(s -> major.add(s.getServiceId()));
        return major;
    }
}
