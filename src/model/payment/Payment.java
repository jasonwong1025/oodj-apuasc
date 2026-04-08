package model.payment;

public class Payment {
    private String paymentId;
    private String appointmentId;
    private String customerId;
    private double amount;
    private String date;
    private String status; // PAID, UNPAID

    public Payment() {}

    public Payment(String paymentId, String appointmentId, String customerId, double amount, String date, String status) {
        this.paymentId = paymentId;
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.amount = amount;
        this.date = date;
        this.status = status;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.join("|", paymentId, appointmentId, customerId, String.valueOf(amount), date, status);
    }
}
