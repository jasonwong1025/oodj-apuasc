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
    private String technicianFeedback;
    private String appointmentType;

    public Appointment() {}

    public Appointment(String appointmentId, String customerId, String vehicleId, String serviceId,
                       String date, String time, String status, String technicianId, String technicianFeedback, String appointmentType) {

        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.serviceId = serviceId;
        this.date = date;
        this.time = time;
        this.status = status;
        this.technicianId = technicianId;
        this.technicianFeedback = technicianFeedback;
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
    public void setStatus(String status) { this.status = status; }

    public String getTechnicianId() { return technicianId; }
    public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }

    public String getTechnicianFeedback() { return technicianFeedback; }
    public void setTechnicianFeedback(String technicianFeedback) { this.technicianFeedback = technicianFeedback; }

    public String getAppointmentType() { return appointmentType; }
    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }

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
                (technicianFeedback == null || technicianFeedback.isEmpty()) ? "NONE" : technicianFeedback,
                (appointmentType == null || appointmentType.isEmpty()) ? "NORMAL" : appointmentType
        );
    }
}