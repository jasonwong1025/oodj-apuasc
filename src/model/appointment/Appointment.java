package model.appointment;



public class Appointment {
    private String appointmentId;
    private String customerId;
    private String vehicleId;
    private String serviceId;
    private String date;
    private String time;
    private AppointmentStatus status;
    private String technicianId;
    private AppointmentType appointmentType;
    private String counterStaffId;

    public Appointment() {
        this.status = AppointmentStatus.PENDING;
        this.appointmentType = AppointmentType.NORMAL;
    }

    public Appointment(String appointmentId, String customerId, String vehicleId, String serviceId,
                       String date, String time, String status, String technicianId, String appointmentType, String counterStaffId) {

        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.serviceId = serviceId;
        this.date = date;
        this.time = time;
        this.status = AppointmentStatus.fromString(status);
        this.technicianId = technicianId;
        this.appointmentType = AppointmentType.fromString(appointmentType);
        this.counterStaffId = counterStaffId;
    }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getStatus() { return status.getLabel(); }
    
    /**
     * Explicit state transition to CONFIRMED.
     */
    public void confirm(String technicianId) {
        if (technicianId == null || technicianId.trim().isEmpty() || "NONE".equalsIgnoreCase(technicianId)) {
            throw new IllegalArgumentException("Technician ID is required to confirm an appointment.");
        }
        this.technicianId = technicianId;
        this.status = AppointmentStatus.CONFIRMED;
    }

    /**
     * Explicit state transition to IN_PROGRESS.
     */
    public void startProgress() {
        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Appointment must be CONFIRMED before starting progress.");
        }
        this.status = AppointmentStatus.IN_PROGRESS;
    }

    /**
     * Explicit state transition to COMPLETED.
     */
    public void complete() {
        if (this.status != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Appointment must be IN PROGRESS before completion.");
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    /**
     * Explicit state transition to PENDING (unassign).
     */
    public void unassign() {
        this.technicianId = "NONE";
        this.status = AppointmentStatus.PENDING;
    }

    public void setStatus(String status) {
        this.status = AppointmentStatus.fromString(status);
    }

    public String getTechnicianId() { return technicianId; }
    public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }

    public String getAppointmentType() { return appointmentType.getLabel(); }
    public void setAppointmentType(String appointmentType) { 
        this.appointmentType = AppointmentType.fromString(appointmentType); 
    }

    public String getCounterStaffId() { return counterStaffId; }
    public void setCounterStaffId(String counterStaffId) { this.counterStaffId = counterStaffId; }

    public boolean canTechnicianProvideFeedback() {
        return status == AppointmentStatus.IN_PROGRESS || status == AppointmentStatus.COMPLETED;
    }

    public boolean canBeAssignedTo(model.users.User technician) {
        if (technician == null || technician.getRole() != model.users.Role.TECHNICIAN) {
            return false;
        }
        String techSvc = technician.getTechnicianServiceType() == null ? "" : technician.getTechnicianServiceType().toLowerCase();
        if (appointmentType == AppointmentType.NORMAL) {
            return techSvc.contains("normal");
        }
        if (appointmentType == AppointmentType.MAJOR) {
            return techSvc.contains("major");
        }
        return false;
    }

    @Override
    public String toString() {
        return utils.FileStorageHelper.join(
                appointmentId,
                customerId,
                vehicleId,
                serviceId,
                date,
                time,
                status.getLabel(),
                (technicianId == null ? "NONE" : technicianId),
                appointmentType.getLabel(),
                (counterStaffId == null || counterStaffId.isEmpty()) ? "NONE" : counterStaffId
        );
    }
}