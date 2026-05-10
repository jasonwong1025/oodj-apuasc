package ui.CounterStaffPortal;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.appointment.Appointment;
import model.service.Service;
import model.users.User;
import repository.AppointmentRepository;
import ui.SharedStyles;
import utils.Result;

public class ManageAppointmentsTabPanel extends CounterStaffTabPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private final JComboBox<String> filterBox;
    private final JComboBox<String> sortBox;
    private List<Appointment> list;

    public ManageAppointmentsTabPanel(CounterStaffContext context) {
        super(context);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        this.list = context.appointmentService().getAllAppointments();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setOpaque(false);

        filterBox = new JComboBox<>(new String[]{
            "PENDING", "All", "CONFIRMED", "IN PROGRESS", "COMPLETED", "CANCELLED"
        });
        sortBox = new JComboBox<>(new String[]{
            "Newest Date", "Oldest Date"
        });

        sortBox.setPreferredSize(new Dimension(140, 25));
        filterBox.setPreferredSize(new Dimension(120, 25));

        top.add(new JLabel("Filter:"));
        top.add(filterBox);
        top.add(new JLabel("Sort:"));
        top.add(sortBox);

        JButton addBtn = SharedStyles.createActionButton("Add Appointment", SharedStyles.BTN_GREEN);
        JButton updateBtn = SharedStyles.createActionButton("Update Status", SharedStyles.BTN_ORANGE);
        JButton assignBtn = SharedStyles.createActionButton("Assign Technician", SharedStyles.BTN_BLUE);

        top.add(addBtn);
        top.add(updateBtn);
        top.add(assignBtn);
        root.add(top, BorderLayout.NORTH);

        String[] columns = {"ID", "Customer", "Vehicle", "Service", "Date", "Time", "Status", "Type", "Technician", "Staff"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        filterBox.addActionListener(e -> refresh());
        sortBox.addActionListener(e -> refresh());

        addBtn.addActionListener(e -> openAddAppointmentDialog());
        updateBtn.addActionListener(e -> updateStatus());
        assignBtn.addActionListener(e -> assignTechnician());

        add(root, BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        list = context.appointmentService().getAllAppointments();
        model.setRowCount(0);
        
        list.sort((a1, a2) -> {
            String d1 = a1.getDate() + " " + a1.getTime();
            String d2 = a2.getDate() + " " + a2.getTime();
            if ("Newest Date".equals(sortBox.getSelectedItem())) {
                return d2.compareTo(d1);
            } else {
                return d1.compareTo(d2);
            }
        });

        for (Appointment a : list) {
            String selectedFilter = filterBox.getSelectedItem().toString();
            if (!"All".equals(selectedFilter) && !a.getStatus().equalsIgnoreCase(selectedFilter)) {
                continue;
            }

            String serviceDisplay = resolveServiceNames(a.getServiceId());

            model.addRow(new Object[]{
                a.getAppointmentId(),
                a.getCustomerId(),
                a.getVehicleId(),
                serviceDisplay,
                a.getDate(),
                a.getTime(),
                a.getStatus(),
                a.getAppointmentType(),
                a.getTechnicianId(),
                a.getCounterStaffId()
            });
        }
    }

    private void openAddAppointmentDialog() {
        JTextField customerIdField = SharedStyles.createFilterField(20);
        JComboBox<String> vehicleBox = new JComboBox<>();
        vehicleBox.setPreferredSize(new Dimension(200, 30));

        customerIdField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                vehicleBox.removeAllItems();
                String customerId = customerIdField.getText().trim().toUpperCase();
                if (customerId.length() < 4) return;
                List<model.vehicle.Vehicle> vehicles = context.vehicleService().getCustomerVehicles(customerId);
                for (model.vehicle.Vehicle v : vehicles) {
                    vehicleBox.addItem(v.getVehicleId() + " - " + v.getPlateNumber());
                }
            }
        });

        DefaultListModel<String> serviceModel = new DefaultListModel<>();
        List<Service> services = context.serviceService().listAll();
        for (Service s : services) {
            serviceModel.addElement(s.getServiceId() + " - " + s.getServiceName() + " (RM " + s.getPrice() + ")");
        }

        JList<String> serviceList = new JList<>(serviceModel);
        serviceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane serviceScroll = new JScrollPane(serviceList);
        serviceScroll.setPreferredSize(new Dimension(250, 100));

        JComboBox<String> dayBox = new JComboBox<>();
        JComboBox<String> monthBox = new JComboBox<>();
        JComboBox<String> yearBox = new JComboBox<>();
        for (int i = 1; i <= 31; i++) dayBox.addItem(String.format("%02d", i));
        for (int i = 1; i <= 12; i++) monthBox.addItem(String.format("%02d", i));
        for (int i = 2026; i <= 2031; i++) yearBox.addItem(String.valueOf(i));

        JComboBox<String> timeBox = new JComboBox<>();
        timeBox.setPreferredSize(new Dimension(200, 30));

        Runnable updateSlots = () -> {
            String date = yearBox.getSelectedItem() + "-" + monthBox.getSelectedItem() + "-" + dayBox.getSelectedItem();
            timeBox.removeAllItems();
            for (String slot : service_layer.AppointmentService.getAllowedSlotTimes()) {
                if (context.appointmentService().isSlotAvailable(date, slot)) {
                    timeBox.addItem(slot + " ✅");
                } else {
                    timeBox.addItem(slot + " ❌ FULL");
                }
            }
        };

        dayBox.addActionListener(ev -> updateSlots.run());
        monthBox.addActionListener(ev -> updateSlots.run());
        yearBox.addActionListener(ev -> updateSlots.run());
        updateSlots.run();

        Object[] fields = {
            "Customer ID:", customerIdField,
            "Vehicle:", vehicleBox,
            "Select Services:", serviceScroll,
            "Date:", new JPanel(new FlowLayout(FlowLayout.LEFT)) {{ add(dayBox); add(monthBox); add(yearBox); }},
            "Time Slot:", timeBox
        };

        if (JOptionPane.showConfirmDialog(this, fields, "Add Appointment", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            List<String> selectedServices = new ArrayList<>();
            for (String selected : serviceList.getSelectedValuesList()) {
                selectedServices.add(selected.split(" - ")[0]);
            }

            if (selectedServices.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select at least one service!");
                return;
            }

            String selectedVehicle = vehicleBox.getSelectedItem() != null ? vehicleBox.getSelectedItem().toString().split(" - ")[0] : "";
            if (selectedVehicle.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select a vehicle!");
                return;
            }

            String selectedTime = timeBox.getSelectedItem().toString();
            if (selectedTime.contains("❌")) {
                JOptionPane.showMessageDialog(this, "This slot is FULL!");
                return;
            }
            selectedTime = selectedTime.replace(" ✅", "");

            String date = yearBox.getSelectedItem() + "-" + monthBox.getSelectedItem() + "-" + dayBox.getSelectedItem();
            Result<Appointment> bookResult = context.appointmentService().bookAppointment(
                customerIdField.getText().trim().toUpperCase(),
                selectedVehicle,
                selectedServices,
                date,
                selectedTime,
                currentUser().getUserId()
            );

            if (bookResult.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Appointment booked successfully!");
                context.refreshAction().run();
            } else {
                JOptionPane.showMessageDialog(this, bookResult.getError(), "Booking Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateStatus() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row!");
            return;
        }

        String id = table.getValueAt(row, 0).toString();
        String staffId = table.getValueAt(row, 9).toString();

        if ("NONE".equalsIgnoreCase(staffId) || "CUSTOMER".equalsIgnoreCase(staffId)) {
            Appointment ap = findById(id);
            if (ap != null) {
                ap.setCounterStaffId(currentUser().getUserId());
                new AppointmentRepository().update(ap);
                staffId = currentUser().getUserId();
            }
        }

        if (!staffId.equals(currentUser().getUserId())) {
            JOptionPane.showMessageDialog(this, "You can only edit appointments handled by yourself.");
            return;
        }

        Appointment targetAppt = findById(id);
        if (targetAppt == null) return;

        if (!"PENDING".equalsIgnoreCase(targetAppt.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only Technicians can update the status once it is CONFIRMED.");
            return;
        }

        String[] options = {"PENDING", "CONFIRMED", "CANCELLED"};
        String status = (String) JOptionPane.showInputDialog(this, "Update Status for " + id, "Select Status",
                JOptionPane.QUESTION_MESSAGE, null, options, targetAppt.getStatus());

        if (status != null && !status.equals(targetAppt.getStatus())) {
            targetAppt.setStatus(status);
            new AppointmentRepository().update(targetAppt);
            JOptionPane.showMessageDialog(this, "Status updated to " + status);
            context.refreshAction().run();
        }
    }

    private void assignTechnician() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row!");
            return;
        }

        String id = table.getValueAt(row, 0).toString();
        Appointment target = findById(id);
        if (target == null) return;

        if ("CANCELLED".equalsIgnoreCase(target.getStatus())) {
            JOptionPane.showMessageDialog(this, "Cannot assign technician to cancelled appointment.");
            return;
        }

        List<User> techs = context.userService().listAllUsers();
        List<String> techIds = new ArrayList<>();
        for (User u : techs) {
            if ("Technician".equalsIgnoreCase(u.getRole()) && u.isActive()) {
                techIds.add(u.getUserId() + " - " + u.getFullName());
            }
        }

        String selectedTech = (String) JOptionPane.showInputDialog(this, "Assign Technician for " + id,
                "Select Technician", JOptionPane.QUESTION_MESSAGE, null, techIds.toArray(), null);

        if (selectedTech != null) {
            String techId = selectedTech.split(" - ")[0];
            target.setTechnicianId(techId);
            new AppointmentRepository().update(target);
            JOptionPane.showMessageDialog(this, "Technician assigned!");
            context.refreshAction().run();
        }
    }

    private Appointment findById(String id) {
        for (Appointment a : list) {
            if (a.getAppointmentId().equals(id)) return a;
        }
        return null;
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
