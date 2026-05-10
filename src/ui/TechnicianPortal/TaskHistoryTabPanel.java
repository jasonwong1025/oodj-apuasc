package ui.TechnicianPortal;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.appointment.Appointment;
import model.service.Service;
import model.users.User;
import model.vehicle.Vehicle;
import ui.shared.SharedStyles;

public class TaskHistoryTabPanel extends TechnicianTabPanel {

    public TaskHistoryTabPanel(TechnicianContext context) {
        super(context);
        setLayout(new BorderLayout());
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        List<Appointment> allAppointments = context.appointmentService().getAllAppointments();

        List<Appointment> myTasks = new ArrayList<>();
        for (Appointment a : allAppointments) {
            if (currentUser().getUserId().equals(a.getTechnicianId())) {
                String status = a.getStatus();
                if ("COMPLETED".equalsIgnoreCase(status)) {
                    myTasks.add(a);
                }
            }
        }

        String[] columns = {"ID", "Customer", "Vehicle", "Service", "Date", "Time", "Status"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Appointment a : myTasks) {
            String serviceDisplay = resolveServiceNames(a.getServiceId());
            User cust = context.userService().findByUserId(a.getCustomerId());
            String customerDisplay = (cust != null) ? String.format("%s (%s)", cust.getFullName(), cust.getUserId()) : a.getCustomerId();

            Vehicle v = context.vehicleService().findById(a.getVehicleId());
            String vehicleDisplay = (v != null) ? String.format("%s (%s %s)", v.getPlateNumber(), v.getBrand(), v.getModel()) : a.getVehicleId();

            model.addRow(new Object[]{
                    a.getAppointmentId(),
                    customerDisplay,
                    vehicleDisplay,
                    serviceDisplay,
                    a.getDate(),
                    a.getTime(),
                    a.getStatus()
            });
        }

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);

        // Filter Controls Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        JTextField searchField = SharedStyles.createFilterField(15);

        filterPanel.add(searchLbl);
        filterPanel.add(searchField);

        root.add(filterPanel, BorderLayout.NORTH);

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
        table.setRowSorter(sorter);

        java.awt.event.ActionListener filterAction = ev -> {
            String txt = searchField.getText().trim();
            if (txt.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txt));
            }
        };

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterAction.actionPerformed(null); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterAction.actionPerformed(null); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterAction.actionPerformed(null); }
        });

        root.add(new JScrollPane(table), BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }

    private String resolveServiceNames(String serviceIds) {
        if (serviceIds == null || serviceIds.trim().isEmpty()) return "N/A";
        String[] parts = serviceIds.split(",");
        List<String> names = new ArrayList<>();
        for (String p : parts) {
            Service svc = context.serviceService().findById(p.trim());
            names.add(svc != null ? svc.getServiceName() : p.trim());
        }
        return String.join(", ", names);
    }
}
