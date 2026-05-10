package ui.CustomerPortal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.time.LocalDate;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.appointment.Appointment;
import ui.shared.SharedStyles;

public class ServiceHistoryTabPanel extends CustomerTabPanel {
    public ServiceHistoryTabPanel(CustomerContext context) {
        super(context);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(16, 20, 20, 20));
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();

        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("Service History");
        title.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 4, 0));
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Vehicle", "Service Name(s)", "Date", "Status", "Payment", "Tech Feedback"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        java.util.List<Appointment> list = appointmentService().getCustomerAppointments(currentUser().getUserId());
        repository.FeedbackRepository fbRepo = new repository.FeedbackRepository();

        JTable table = new JTable(model);
        table.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        table.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        table.setGridColor(new Color(220, 220, 225));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                                    boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = String.valueOf(tbl.getValueAt(row, 4));
                    if ("CONFIRMED".equalsIgnoreCase(status)) c.setBackground(new Color(235, 243, 255));
                    else if ("IN PROGRESS".equalsIgnoreCase(status)) c.setBackground(new Color(255, 253, 235));
                    else if ("COMPLETED".equalsIgnoreCase(status)) c.setBackground(new Color(236, 253, 242));
                    else if ("CANCELLED".equalsIgnoreCase(status)) c.setBackground(new Color(255, 242, 242));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel("Search:"));
        JTextField searchField = SharedStyles.createFilterField(10);
        filterRow.add(searchField);
        filterRow.add(new JLabel("Status:"));
        JComboBox<String> statusFilter = SharedStyles.createFilterCombo(
                new String[]{"ALL", "CONFIRMED", "IN PROGRESS", "COMPLETED", "CANCELLED"});
        statusFilter.setPreferredSize(new java.awt.Dimension(95, 28));
        filterRow.add(statusFilter);
        filterRow.add(new JLabel("Pay:"));
        JComboBox<String> paymentFilter = SharedStyles.createFilterCombo(new String[]{"ALL", "PAID", "UNPAID"});
        paymentFilter.setPreferredSize(new java.awt.Dimension(80, 28));
        filterRow.add(paymentFilter);
        final String[] fromDateValue = {""};
        final String[] toDateValue = {""};
        javax.swing.JButton rangeBtn = SharedStyles.createActionButton("Select Date", SharedStyles.BTN_BLUE);
        rangeBtn.setPreferredSize(new java.awt.Dimension(140, 28));
        filterRow.add(new JLabel("Date:"));
        filterRow.add(rangeBtn);
        JCheckBox completedOnly = new JCheckBox("Completed");
        completedOnly.setOpaque(false);
        filterRow.add(completedOnly);

        Runnable applyFilter = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            String status = String.valueOf(statusFilter.getSelectedItem());
            String payment = String.valueOf(paymentFilter.getSelectedItem());
            LocalDate fromDate = parseDate(fromDateValue[0]);
            LocalDate toDate = parseDate(toDateValue[0]);
            model.setRowCount(0);
            for (Appointment a : list) {
                if ("PENDING".equalsIgnoreCase(a.getStatus())) continue;
                if (completedOnly.isSelected() && !"COMPLETED".equalsIgnoreCase(a.getStatus())) continue;
                if (!"ALL".equals(status) && !status.equalsIgnoreCase(a.getStatus())) continue;
                if (fromDate != null || toDate != null) {
                    try {
                        LocalDate apptDate = LocalDate.parse(a.getDate(), service_layer.AppointmentService.DATE_FORMATTER);
                        if (fromDate != null && apptDate.isBefore(fromDate)) continue;
                        if (toDate != null && apptDate.isAfter(toDate)) continue;
                    } catch (java.time.format.DateTimeParseException ignore) {
                        continue;
                    }
                }
                boolean isPaid = paymentService().isPaid(a.getAppointmentId());
                String paymentValue = isPaid ? "PAID" : "UNPAID";
                if (!"ALL".equals(payment) && !payment.equalsIgnoreCase(paymentValue)) continue;
                model.feedback.Feedback fbObj = fbRepo.findByAppointmentId(a.getAppointmentId());
                String existingFb = (fbObj == null || fbObj.getDescription().trim().isEmpty() || "NONE".equalsIgnoreCase(fbObj.getDescription())) ? "-" : fbObj.getDescription();
                String rowText = (a.getAppointmentId()
                        + resolveVehicleInfo(a.getVehicleId())
                        + resolveServiceNames(a.getServiceId())
                        + a.getDate()
                        + a.getStatus()
                        + paymentValue
                        + existingFb).toLowerCase();
                if (!keyword.isEmpty() && !rowText.contains(keyword)) continue;
                model.addRow(new Object[]{
                    a.getAppointmentId(),
                    resolveVehicleInfo(a.getVehicleId()),
                    resolveServiceNames(a.getServiceId()),
                    a.getDate(),
                    a.getStatus(),
                    paymentValue,
                    existingFb
                });
            }
        };
        javax.swing.event.DocumentListener autoFilter = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
        };
        searchField.getDocument().addDocumentListener(autoFilter);
        statusFilter.addActionListener(e -> applyFilter.run());
        paymentFilter.addActionListener(e -> applyFilter.run());
        completedOnly.addActionListener(e -> applyFilter.run());

        rangeBtn.addActionListener(e -> {
            String[] picked = showDateRangePicker(fromDateValue[0], toDateValue[0]);
            if (picked == null) return;
            fromDateValue[0] = picked[0] == null ? "" : picked[0];
            toDateValue[0] = picked[1] == null ? "" : picked[1];
            applyFilter.run();
        });

        applyFilter.run();

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 205)));
        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);
        body.add(filterRow, BorderLayout.NORTH);
        body.add(tableScroll, BorderLayout.CENTER);
        JScrollPane outerScroll = new JScrollPane(body, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outerScroll.setBorder(null);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(outerScroll, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return LocalDate.parse(raw, service_layer.AppointmentService.DATE_FORMATTER);
        } catch (java.time.format.DateTimeParseException ex) {
            return null;
        }
    }
}
