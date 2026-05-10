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
import ui.shared.SharedStyles;
import utils.IdGenerator;

public class ProcessPaymentTabPanel extends CounterStaffTabPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private final JComboBox<String> filterBox;

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
        List<Appointment> appointments = context.appointmentService().getAllAppointments();
        model.setRowCount(0);

        for (Appointment a : appointments) {
            if (!"COMPLETED".equalsIgnoreCase(a.getStatus())) continue;

            double totalPrice = context.appointmentService().calculateTotalPrice(a.getServiceId());
            
            // Resolve service names for display
            List<String> serviceNames = new ArrayList<>();
            if (a.getServiceId() != null && !a.getServiceId().trim().isEmpty()) {
                for (String sid : a.getServiceId().split(",")) {
                    Service s = context.serviceService().findById(sid.trim());
                    if (s != null) serviceNames.add(s.getServiceName());
                }
            }

            Payment p = context.paymentService().findByAppointmentId(a.getAppointmentId());
            String paymentStatus = (p != null) ? p.getStatus() : "UNPAID";
            
            String filter = filterBox.getSelectedItem().toString();
            if (filter.equals("Unpaid") && !paymentStatus.equalsIgnoreCase("UNPAID")) continue;
            if (filter.equals("Paid") && !paymentStatus.equalsIgnoreCase("PAID")) continue;

            String payDate = (p != null) ? p.getDate() : "NONE";

            model.addRow(new Object[]{
                a.getAppointmentId(),
                a.getCustomerId(),
                String.join(", ", serviceNames),
                totalPrice,
                paymentStatus,
                payDate
            });
        }
    }

    private void processPayment() {
        int row = table.getSelectedRow();
        if (row == -1) {
            SharedStyles.showSelectionError(this);
            return;
        }

        String appointmentId = table.getValueAt(row, 0).toString();
        String status = table.getValueAt(row, 4).toString();

        if ("PAID".equalsIgnoreCase(status)) {
            SharedStyles.showWarning(this, "Already fully paid!");
            return;
        }

        double total = (Double) table.getValueAt(row, 3);
        if (SharedStyles.showConfirm(this, "Confirm full payment of RM " + total + " ?")) {
            Payment payment = new Payment(
                IdGenerator.generateId("PAY", "data/payments.txt"),
                appointmentId,
                total,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "PAID"
            );
            context.paymentService().processPayment(payment);
            SharedStyles.showMessage(this, "Full payment recorded!");
            context.refreshAction().run();
        }
    }

    private void printReceipt() {
        int row = table.getSelectedRow();
        if (row == -1) {
            SharedStyles.showSelectionError(this);
            return;
        }

        String appointmentId = table.getValueAt(row, 0).toString();
        String customerId = table.getValueAt(row, 1).toString();
        String service = table.getValueAt(row, 2).toString();
        String total = table.getValueAt(row, 3).toString();
        String status = table.getValueAt(row, 4).toString();
        String payDate = table.getValueAt(row, 5).toString();

        if (!"PAID".equalsIgnoreCase(status)) {
            SharedStyles.showWarning(this, "Cannot print receipt. Payment not completed.");
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
            SharedStyles.showMessage(this, "Receipt generated: " + fileName);
            utils.FileUtil.openFile(new File(fileName));
        } catch (Exception ex) {
            SharedStyles.showError(this, "Error generating receipt: " + ex.getMessage());
        }
    }
}
