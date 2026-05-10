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
import repository.AppointmentRepository;
import service_layer.ServiceService;
import ui.SharedStyles;

public class MyTasksTabPanel extends TechnicianTabPanel {

    public MyTasksTabPanel(TechnicianContext context) {
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
                if ("CONFIRMED".equalsIgnoreCase(status) || "IN PROGRESS".equalsIgnoreCase(status)) {
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
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        // Filter Controls Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        JTextField searchField = SharedStyles.createFilterField(15);

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "CONFIRMED", "IN PROGRESS"});
        statusFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JButton updateStatusBtn = SharedStyles.createActionButton("Update Status", SharedStyles.BTN_ORANGE);
        updateStatusBtn.setFont(new Font("SansSerif", Font.BOLD, 13));

        filterPanel.add(searchLbl);
        filterPanel.add(searchField);
        filterPanel.add(statusLbl);
        filterPanel.add(statusFilter);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(updateStatusBtn);

        root.add(filterPanel, BorderLayout.NORTH);

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
        table.setRowSorter(sorter);

        java.awt.event.ActionListener filterAction = ev -> {
            String txt = searchField.getText().trim();
            String statusTxt = statusFilter.getSelectedItem().toString();

            List<RowFilter<Object, Object>> filters = new ArrayList<>();
            if (!txt.isEmpty()) {
                filters.add(RowFilter.regexFilter("(?i)" + txt));
            }
            if (!"All".equalsIgnoreCase(statusTxt)) {
                filters.add(RowFilter.regexFilter("(?i)^" + statusTxt + "$", 6));
            }

            if (filters.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.andFilter(filters));
            }
        };

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterAction.actionPerformed(null); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterAction.actionPerformed(null); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterAction.actionPerformed(null); }
        });
        statusFilter.addActionListener(filterAction);

        updateStatusBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an appointment to update.");
                return;
            }

            int modelRow = table.convertRowIndexToModel(selectedRow);
            String id = model.getValueAt(modelRow, 0).toString();
            String currentStatus = model.getValueAt(modelRow, 6).toString();

            List<String> optionsList = new ArrayList<>();
            if ("CONFIRMED".equalsIgnoreCase(currentStatus)) {
                optionsList.add("CONFIRMED");
                optionsList.add("IN PROGRESS");
                optionsList.add("COMPLETED");
            } else if ("IN PROGRESS".equalsIgnoreCase(currentStatus)) {
                optionsList.add("IN PROGRESS");
                optionsList.add("COMPLETED");
            } else {
                optionsList.add(currentStatus);
            }
            String[] options = optionsList.toArray(new String[0]);
            String newStatus = (String) JOptionPane.showInputDialog(
                    this,
                    "Select new status for " + id + ":",
                    "Update Status",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    currentStatus
            );

            if (newStatus != null && !newStatus.equals(currentStatus)) {
                for (Appointment a : myTasks) {
                    if (a.getAppointmentId().equals(id)) {
                        a.setStatus(newStatus);
                        new AppointmentRepository().update(a);
                        JOptionPane.showMessageDialog(this, "Status successfully updated to " + newStatus);
                        context.refreshAction().run();
                        break;
                    }
                }
            }
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
        ServiceService serviceSvc = new ServiceService();
        for (String p : parts) {
            Service svc = serviceSvc.findById(p.trim());
            names.add(svc != null ? svc.getServiceName() : p.trim());
        }
        return String.join(", ", names);
    }
}
