package ui.CustomerPortal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.JButton;
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
import ui.SharedStyles;

public class AppointmentsTabPanel extends CustomerTabPanel {
    public AppointmentsTabPanel(CustomerContext context) {
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

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("My Appointments");
        title.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        JButton cancelBtn = SharedStyles.createActionButton("Cancel Appointment", SharedStyles.BTN_RED);
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRight.setOpaque(false);
        topRight.add(cancelBtn);
        top.add(title, BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Vehicle", "Service Name(s)", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        java.util.List<Appointment> list = appointmentService().getCustomerAppointments(currentUser().getUserId());

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
                    String status = String.valueOf(tbl.getValueAt(row, 5));
                    if ("PENDING".equalsIgnoreCase(status)) c.setBackground(new Color(255, 253, 235));
                    else if ("CONFIRMED".equalsIgnoreCase(status)) c.setBackground(new Color(235, 243, 255));
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
        JTextField searchField = SharedStyles.createFilterField(16);
        filterRow.add(searchField);
        filterRow.add(new JLabel("Category:"));
        JComboBox<String> categoryFilter = SharedStyles.createFilterCombo(new String[]{"ALL", "NORMAL", "MAJOR"});
        filterRow.add(categoryFilter);

        Runnable applyFilter = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            String category = String.valueOf(categoryFilter.getSelectedItem());
            model.setRowCount(0);
            for (Appointment a : list) {
                if (!"PENDING".equalsIgnoreCase(a.getStatus())) continue;
                String appointmentCategory = getAppointmentCategory(a);
                if (!"ALL".equals(category) && !category.equalsIgnoreCase(appointmentCategory)) continue;
                String rowText = (a.getAppointmentId()
                        + resolveVehicleInfo(a.getVehicleId())
                        + resolveServiceNames(a.getServiceId())
                        + a.getDate()
                        + a.getTime()
                        + a.getStatus()).toLowerCase();
                if (!keyword.isEmpty() && !rowText.contains(keyword)) continue;
                model.addRow(new Object[]{
                    a.getAppointmentId(),
                    resolveVehicleInfo(a.getVehicleId()),
                    resolveServiceNames(a.getServiceId()),
                    a.getDate(),
                    a.getTime(),
                    a.getStatus()
                });
            }
        };
        javax.swing.event.DocumentListener autoFilter = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
        };
        searchField.getDocument().addDocumentListener(autoFilter);
        categoryFilter.addActionListener(e -> applyFilter.run());

        applyFilter.run();

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 205)));
        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);
        body.add(filterRow, BorderLayout.NORTH);
        body.add(tableScroll, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                SharedStyles.showSelectionError(context.getOwner());
                return;
            }
            if (javax.swing.JOptionPane.showConfirmDialog(context.getOwner(), "Cancel this appointment?", "Confirm", javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION) {
                appointmentService().cancelAppointment(table.getValueAt(row, 0).toString());
                context.getRefreshAction().run();
            }
        });

        revalidate();
        repaint();
    }
}
