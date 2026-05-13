package ui.CounterStaffPortal;

import java.awt.*;
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

        String appointmentId =
                table.getValueAt(row, 0).toString();

        String customerId =
                table.getValueAt(row, 1).toString();

        String service =
                table.getValueAt(row, 2).toString();

        String total =
                table.getValueAt(row, 3).toString();

        String status =
                table.getValueAt(row, 4).toString();

        String payDate =
                table.getValueAt(row, 5).toString();

        if (!"PAID".equalsIgnoreCase(status)) {

            SharedStyles.showWarning(
                    this,
                    "Cannot print receipt. Payment not completed."
            );

            return;
        }

        String receipt = """
    ====================================
                APU ASC
    ====================================

    Appointment ID : %s
    Customer ID    : %s
    Payment Date   : %s

    Services:
    %s

    ------------------------------------
    TOTAL PAID : RM %s
    ------------------------------------

    Thank you for choosing APU ASC!
    """
                .formatted(
                        appointmentId,
                        customerId,
                        payDate,
                        service,
                        total
                );

        JTextArea area =
                new JTextArea(receipt);

        area.setEditable(false);

        area.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        14
                )
        );

        area.setBackground(Color.WHITE);

        area.setMargin(
                new Insets(10, 10, 10, 10)
        );

        JScrollPane pane =
                new JScrollPane(area);

        pane.setPreferredSize(
                new Dimension(500, 350)
        );

        JOptionPane.showMessageDialog(
                this,
                pane,
                "Receipt Preview",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
