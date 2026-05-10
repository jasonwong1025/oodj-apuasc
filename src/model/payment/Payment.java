package model.payment;

public class Payment {
    private String paymentId;
    private String appointmentId;
    private double amount;
    private String date;
    private String status; // PAID, UNPAID

    public Payment() {}

    public Payment(String paymentId, String appointmentId, double amount, String date, String status) {
        this.paymentId = paymentId;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.date = date;
        this.status = status;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return utils.FileStorageHelper.join(
            paymentId,
            appointmentId,
            String.valueOf(amount),
            date,
            status
        ); 
    } 
}
