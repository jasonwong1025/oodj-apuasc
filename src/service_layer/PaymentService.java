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
    public java.util.List<model.payment.Payment> getAllPayments() {
    return new repository.PaymentRepository().getAllPayments();
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

    public Payment findByAppointmentId(String appointmentId) {
        for (Payment p : paymentRepository.getAllPayments()) {
            if (p.getAppointmentId().equals(appointmentId)) return p;
        }
        return null;
    }

    public void updatePayment(Payment payment) {
        paymentRepository.update(payment);
    }

    public void generateUnpaidPaymentForCompletedAppointment(model.appointment.Appointment a) {
        if (!"COMPLETED".equalsIgnoreCase(a.getStatus())) return;

        List<Payment> all = paymentRepository.getAllPayments();
        for (Payment p : all) {
            if (p.getAppointmentId().equals(a.getAppointmentId())) {
                return; 
            }
        }
        
        repository.ServiceFileRepository repo = new repository.ServiceFileRepository();
        List<model.service.Service> allServices = repo.getAll();
        double total = 0;
        String[] sIds = a.getServiceId().split(",");
        for (String id : sIds) {
            String idNum = id.replaceAll("\\D", "");
            for (model.service.Service s : allServices) {
                if (s.getServiceId().replaceAll("\\D", "").equals(idNum)) {
                    total += s.getPrice();
                    break;
                }
            }
        }
        
        Payment payment = new Payment(
            utils.IdGenerator.generateId("PAY", "data/payments.txt"),
            a.getAppointmentId(),
            total,
            "NONE",
            "UNPAID"
        );
        processPayment(payment);
    }
}
