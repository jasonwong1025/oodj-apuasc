package repository;

import model.payment.Payment;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
                if (parts.length == 6) {
                    payments.add(new Payment(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]), parts[4], parts[5]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return payments;
    }

    public List<Payment> getPaymentsByCustomer(String customerId) {
        List<Payment> all = getAllPayments();
        List<Payment> filtered = new ArrayList<>();
        for (Payment p : all) {
            if (p.getCustomerId().equals(customerId)) {
                filtered.add(p);
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
}
