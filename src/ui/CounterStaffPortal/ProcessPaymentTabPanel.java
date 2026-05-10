package ui.CounterStaffPortal;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.appointment.Appointment;
import model.payment.Payment;
import model.service.Service;
import ui.SharedStyles;
import utils.IdGenerator;

public class ProcessPaymentTabPanel extends CounterStaffTabPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private final JComboBox<String> filterBox;
    private List<Appointment> appointments;

    public ProcessPaymentTabPanel(CounterStaffContext context) {
        super(context);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setOpaque(false);

        filterBox = new JComboBox<>(new String[]{"All", "Unpaid", "Paid"});
        filterBox.setPreferredSize(new Dimension(120, 25));

        top.add(new JLabel("Filter:"));
        top.add(filterBox);

        JButton payBtn = SharedStyles.createActionButton("Process Payment", SharedStyles.BTN_GREEN);
        JButton receiptBtn = SharedStyles.createActionButton("Print Receipt", SharedStyles.BTN_BLUE);

        top.add(payBtn);
        top.add(receiptBtn);

        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Appointment ID", "Customer", "Service", "Price", "Payment Status", "Payment Date & Time"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        filterBox.addActionListener(e -> refresh());
        payBtn.addActionListener(e -> processPayment());
        receiptBtn.addActionListener(e -> printReceipt());

        add(root, BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        appointments = context.appointmentService().getAllAppointments();
        List<Payment> payments = context.paymentService().getAllPayments();
        model.setRowCount(0);

        for (Appointment a : appointments) {
            if (!"COMPLETED".equalsIgnoreCase(a.getStatus())) continue;

            String serviceIds = a.getServiceId();
            double totalPrice = 0;
            List<String> serviceNames = new ArrayList<>();

            if (serviceIds != null && !serviceIds.trim().isEmpty()) {
                String[] ids = serviceIds.split(",");
                for (String sid : ids) {
                    Service s = context.serviceService().findById(sid.trim());
                    if (s != null) {
                        serviceNames.add(s.getServiceName());
                        totalPrice += s.getPrice();
                    }
                }
            }

            String finalService = String.join(", ", serviceNames);
            Payment found = null;
            for (Payment p : payments) {
                if (p.getAppointmentId().equals(a.getAppointmentId())) {
                    found = p;
                    break;
                }
            }

            String paymentStatus = (found != null) ? found.getStatus() : "UNPAID";
            String filter = filterBox.getSelectedItem().toString();
            if (filter.equals("Unpaid") && !paymentStatus.equalsIgnoreCase("UNPAID")) continue;
            if (filter.equals("Paid") && !paymentStatus.equalsIgnoreCase("PAID")) continue;

            String payDate = (found != null) ? found.getDate() : "NONE";

            model.addRow(new Object[]{
                a.getAppointmentId(),
                a.getCustomerId(),
                finalService,
                totalPrice,
                paymentStatus,
                payDate
            });
        }
    }

    private void processPayment() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select appointment!");
            return;
        }

        String appointmentId = table.getValueAt(row, 0).toString();
        String status = table.getValueAt(row, 4).toString();

        if ("PAID".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Already fully paid!");
            return;
        }

        double total = Double.parseDouble(table.getValueAt(row, 3).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Confirm full payment of RM " + total + " ?", "Process Payment", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Payment payment = new Payment(
                IdGenerator.generateId("PAY", "data/payments.txt"),
                appointmentId,
                total,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "PAID"
            );
            context.paymentService().processPayment(payment);
            JOptionPane.showMessageDialog(this, "Full payment recorded!");
            context.refreshAction().run();
        }
    }

    private void printReceipt() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row!");
            return;
        }

        String appointmentId = table.getValueAt(row, 0).toString();
        String customerId = table.getValueAt(row, 1).toString();
        String service = table.getValueAt(row, 2).toString();
        String total = table.getValueAt(row, 3).toString();
        String status = table.getValueAt(row, 4).toString();
        String payDate = table.getValueAt(row, 5).toString();

        if (!"PAID".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Cannot print receipt. Payment not completed.");
            return;
        }

        try {
            new File("data/receipts").mkdirs();
            String fileName = "data/receipts/receipt_" + appointmentId + ".html";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write("<html><head><title>Receipt</title><style>");
                writer.write("body { font-family: Arial; background:#f4f4f4; padding:20px; }");
                writer.write(".receipt { width:400px; margin:auto; background:#fff; padding:20px; border-radius:10px; box-shadow:0 0 10px rgba(0,0,0,0.1);} ");
                writer.write("h2 { text-align:center; margin-bottom:10px; }");
                writer.write(".line { border-bottom:1px dashed #aaa; margin:10px 10px; }");
                writer.write(".item { display:flex; justify-content:space-between; margin:5px 10px; }");
                writer.write(".total { font-weight:bold; font-size:1.2em; margin-top:10px; }");
                writer.write("</style></head><body>");
                writer.write("<div class='receipt'>");
                writer.write("<h2>APU-ASC Receipt</h2>");
                writer.write("<div class='line'></div>");
                writer.write("<div class='item'><span>Appointment ID:</span><span>" + appointmentId + "</span></div>");
                writer.write("<div class='item'><span>Customer ID:</span><span>" + customerId + "</span></div>");
                writer.write("<div class='item'><span>Date:</span><span>" + payDate + "</span></div>");
                writer.write("<div class='line'></div>");
                writer.write("<div class='item'><span>Services:</span><span style='text-align:right'>" + service + "</span></div>");
                writer.write("<div class='line'></div>");
                writer.write("<div class='item total'><span>Total Paid:</span><span>RM " + total + "</span></div>");
                writer.write("<div class='line'></div>");
                writer.write("<p style='text-align:center; color:#888;'>Thank you for choosing APU-ASC!</p>");
                writer.write("</div></body></html>");
            }
            JOptionPane.showMessageDialog(this, "Receipt generated: " + fileName);
            utils.FileUtil.openFile(new File(fileName));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating receipt: " + ex.getMessage());
        }
    }
}
