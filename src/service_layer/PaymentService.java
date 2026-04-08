package service_layer;

import model.payment.Payment;
import repository.PaymentRepository;
import java.util.List;

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
}
