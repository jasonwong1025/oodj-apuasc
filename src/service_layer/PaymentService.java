package service_layer;

import java.util.List;
import model.payment.Payment;
import repository.PaymentRepository;

public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
    }

    public List<Payment> getCustomerPayments(String customerId) {
        return paymentRepository.getPaymentsByCustomer(customerId);
    }
    
    public boolean isPaid(String appointmentId) {
        List<Payment> all = paymentRepository.getAllPayments();
        for (Payment p : all) {
            if (p.getAppointmentId().equals(appointmentId) && p.getStatus().equalsIgnoreCase("PAID")) {
                return true;
            }
        }
        return false;
    }
    public void processPayment(Payment payment) {
        paymentRepository.save(payment);
}
}

