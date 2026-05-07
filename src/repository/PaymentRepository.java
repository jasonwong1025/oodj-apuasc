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
                            parts[0],
                            parts[1],
                            Double.parseDouble(parts[2]),
                            parts[3],
                            parts[4]
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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(payment.toString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(Payment payment) {
        List<Payment> all = getAllPayments();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Payment p : all) {
                if (p.getPaymentId().equals(payment.getPaymentId())) {
                    bw.write(payment.toString());
                } else {
                    bw.write(p.toString());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}