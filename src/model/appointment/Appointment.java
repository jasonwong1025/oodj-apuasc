package model.appointment;

public class Appointment {
    private String appointmentId;
    private String customerId;
    private String vehicleId;
    private String serviceId;
    private String date;
    private String time;
    private String status;
    private String technicianId;
    private String appointmentType;

    public Appointment() {}

    public Appointment(String appointmentId, String customerId, String vehicleId, String serviceId,
                       String date, String time, String status, String technicianId, String appointmentType) {

        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.serviceId = serviceId;
        this.date = date;
        this.time = time;
        this.status = status;
        this.technicianId = technicianId;
        this.appointmentType = appointmentType;
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

    public String getStatus() { return status; }
    public void setStatus(String status) {
        if ("PENDING".equalsIgnoreCase(status)) {
            this.technicianId = "NONE";
        }
        if ("CONFIRMED".equalsIgnoreCase(status) || "IN PROGRESS".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
            if (technicianId == null || technicianId.trim().isEmpty() || "NONE".equalsIgnoreCase(technicianId)) {
                this.status = "PENDING";
                return;
            }
        }
        this.status = status;
    }

    public String getTechnicianId() { return technicianId; }
    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
        if (technicianId != null && !technicianId.trim().isEmpty() && !"NONE".equalsIgnoreCase(technicianId)) {
            if ("PENDING".equalsIgnoreCase(this.status)) {
                this.status = "CONFIRMED";
            }
        } else {
            if ("CONFIRMED".equalsIgnoreCase(this.status)) {
                this.status = "PENDING";
            }
        }
    }

    public String getAppointmentType() { return appointmentType; }
    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }

    public boolean canTechnicianProvideFeedback() {
        return "IN PROGRESS".equalsIgnoreCase(this.status) || "COMPLETED".equalsIgnoreCase(this.status);
    }

    public boolean canBeAssignedTo(model.users.User technician) {
        if (technician == null || !"Technician".equalsIgnoreCase(technician.getRole())) {
            return false;
        }
        String type = this.appointmentType == null || this.appointmentType.trim().isEmpty() ? "NORMAL" : this.appointmentType;
        String techSvc = technician.getTechnicianServiceType() == null ? "" : technician.getTechnicianServiceType();
        if ("NORMAL".equalsIgnoreCase(type)) {
            return techSvc.toLowerCase().contains("normal");
        }
        if ("MAJOR".equalsIgnoreCase(type)) {
            return techSvc.toLowerCase().contains("major");
        }
        return false;
    }

    @Override
    public String toString() {
        return String.join("|",
                appointmentId,
                customerId,
                vehicleId,
                serviceId,
                date,
                time,
                status,
                (technicianId == null ? "NONE" : technicianId),
                (appointmentType == null || appointmentType.isEmpty()) ? "NORMAL" : appointmentType
        );
    }
}