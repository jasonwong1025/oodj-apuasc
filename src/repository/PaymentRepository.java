package repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import model.payment.Payment;

public class PaymentRepository {
    private static final String FILE_PATH = "data/payments.txt";

    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return payments;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|", -1);

                if (parts.length >= 5) {
                    payments.add(new Payment(
                            utils.FileStorageHelper.unescape(parts[0]),
                            utils.FileStorageHelper.unescape(parts[1]),
                            Double.parseDouble(parts[2]),
                            utils.FileStorageHelper.unescape(parts[3]),
                            utils.FileStorageHelper.unescape(parts[4])
                    ));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return payments;
    }

    public List<Payment> getPaymentsByCustomer(String customerId) {
        List<Payment> all = getAllPayments();
        List<Payment> filtered = new ArrayList<>();
        List<model.appointment.Appointment> appts = new AppointmentRepository().getAllAppointments();

        for (Payment p : all) {
            for (model.appointment.Appointment a : appts) {
                if (a.getAppointmentId().equals(p.getAppointmentId()) && a.getCustomerId().equals(customerId)) {
                    filtered.add(p);
                    break;
                }
            }
        }

        return filtered;
    }

    public void save(Payment payment) {
        try {
            utils.FileStorageHelper.appendLine(FILE_PATH, payment.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(Payment payment) {
        List<Payment> all = getAllPayments();
        List<String> lines = new ArrayList<>();
        for (Payment p : all) {
            if (p.getPaymentId().equals(payment.getPaymentId())) {
                lines.add(payment.toString());
            } else {
                lines.add(p.toString());
            }
        }
        try {
            utils.FileStorageHelper.writeAtomic(FILE_PATH, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}