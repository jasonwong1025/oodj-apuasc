package ui.CounterStaffPortal;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.users.Customer;
import model.users.User;
import model.vehicle.Vehicle;
import ui.shared.SharedStyles;
import utils.IdGenerator;
import utils.Result;
import utils.ValidationUtil;

public class ManageCustomersTabPanel extends CounterStaffTabPanel {

    public ManageCustomersTabPanel(CounterStaffContext context) {
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

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setOpaque(false);

        JButton addBtn = SharedStyles.createActionButton("Add Customer", SharedStyles.BTN_GREEN);
        top.add(addBtn);
        JButton editBtn = SharedStyles.createActionButton("Edit Customer", SharedStyles.BTN_BLUE);
        top.add(editBtn);
        JButton deactivateBtn = SharedStyles.createActionButton("Deactivate Customer", SharedStyles.BTN_ORANGE);
        top.add(deactivateBtn);
        JButton deleteBtn = SharedStyles.createActionButton("Delete Customer", SharedStyles.BTN_RED);
        top.add(deleteBtn);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {
                "ID",
                "Full Name",
                "Email",
                "Contact",
                "Vehicle Plate",
                "Vehicle Count",
                "Status"
        };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<User> customers = context.userService().getAllCustomers();

        for (User u : customers) {

            List<Vehicle> vehicles =
                    context.vehicleService().getVehiclesByOwner(u.getUserId());

            String vehiclePlates = "-";

            if (!vehicles.isEmpty()) {

                java.util.List<String> plates =
                        new java.util.ArrayList<>();

                for (Vehicle v : vehicles) {
                    plates.add(v.getPlateNumber());
                }

                vehiclePlates = String.join(", ", plates);
            }

            int vehicleCount = vehicles.size();

            String status =
                    u.isActive() ? "ACTIVE" : "INACTIVE";

            model.addRow(new Object[]{
                    u.getUserId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getContact(),
                    vehiclePlates,
                    vehicleCount,
                    status
            });
        }

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        
        addBtn.addActionListener(e -> {
            JTextField name = SharedStyles.createFilterField(20);
            JTextField email = SharedStyles.createFilterField(20);
            JTextField contact = SharedStyles.createFilterField(20);
            JTextField password = SharedStyles.createFilterField(20);

            JTextField plate = SharedStyles.createFilterField(20);
            JTextField brand = SharedStyles.createFilterField(20);
            JTextField modelF = SharedStyles.createFilterField(20);

            Object[] fields = {
                "Name:", name,
                "Email:", email,
                "Contact:", contact,
                "Password:", password,
                "----- Vehicle Info -----",
                "Plate Number:", plate,
                "Brand:", brand,
                "Model:", modelF
            };

            if (SharedStyles.showConfirm(this, fields, "Add Customer")) {
                String fullName = name.getText().trim();
                String emailVal = email.getText().trim();
                String contactVal = contact.getText().trim();
                String passwordVal = password.getText();

                if (!fullName.matches("[a-zA-Z ]+")) {

                    SharedStyles.showError(this,
                            "Name must contain letters only.");

                    return;
                }

                if (!ValidationUtil.isValidEmail(emailVal)) {

                    SharedStyles.showError(this,
                            ValidationUtil.invalidEmailMessage());

                    return;
                }

                if (!ValidationUtil.isValidContact(contactVal)) {

                    SharedStyles.showError(this,
                            ValidationUtil.invalidContactMessage());

                    return;
                }

                if (!ValidationUtil.isValidPassword(passwordVal)) {

                    SharedStyles.showError(this,
                            ValidationUtil.passwordRequirementsMessage());

                    return;
                }
                String customerId = IdGenerator.generateId("C", "data/users.txt");

                context.userService().addCustomer(new Customer(
                    customerId,
                    name.getText(),
                    email.getText(),
                    contact.getText(),
                    password.getText()
                ));

                String vehicleId = IdGenerator.generateId("VEH", "data/vehicles.txt");
                Vehicle v = new Vehicle(
                    vehicleId,
                    customerId,
                    plate.getText(),
                    brand.getText(),
                    modelF.getText()
                );
                context.vehicleService().addVehicle(v);

                SharedStyles.showMessage(this, "Customer + Vehicle added successfully!");
                context.refreshAction().run();
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                SharedStyles.showSelectionError(this);
                return;
            }

            String id = table.getValueAt(row, 0).toString();
            String nameVal = table.getValueAt(row, 1).toString();
            String emailVal = table.getValueAt(row, 2).toString();
            String contactVal = table.getValueAt(row, 3).toString();

            JTextField nameField = SharedStyles.createFilterField(20);
            JTextField emailField = SharedStyles.createFilterField(20);
            JTextField contactField = SharedStyles.createFilterField(20);
            JTextField passwordField = SharedStyles.createFilterField(20);

            nameField.setText(nameVal);
            emailField.setText(emailVal);
            contactField.setText(contactVal);

            List<Vehicle> vehicles =
                    context.vehicleService().getVehiclesByOwner(id);

            JComboBox<String> vehicleBox = new JComboBox<>();

            for (Vehicle v : vehicles) {
                vehicleBox.addItem(v.getPlateNumber());
            }

            Vehicle selectedVehicle =
                    vehicles.isEmpty() ? null : vehicles.get(0);



            JTextField plateField = SharedStyles.createFilterField(20);
            JTextField brandField = SharedStyles.createFilterField(20);
            JTextField modelField = SharedStyles.createFilterField(20);

            if (selectedVehicle != null) {

                plateField.setText(selectedVehicle.getPlateNumber());
                brandField.setText(selectedVehicle.getBrand());
                modelField.setText(selectedVehicle.getModel());
            }

            Object[] fields = {
                "Name:", nameField,
                "Email:", emailField,
                "Contact:", contactField,
                "Password:", passwordField,
                "----- Vehicle Info -----",
                "Select Vehicle:", vehicleBox,
                "Plate Number:", plateField,
                "Brand:", brandField,
                "Model:", modelField
            };
            vehicleBox.addActionListener(ev -> {

                int index = vehicleBox.getSelectedIndex();

                if (index >= 0 && index < vehicles.size()) {

                    Vehicle v = vehicles.get(index);

                    plateField.setText(v.getPlateNumber());
                    brandField.setText(v.getBrand());
                    modelField.setText(v.getModel());
                }
            });

            if (SharedStyles.showConfirm(this, fields, "Edit Customer")) {
                String fullName = nameField.getText().trim();
                String emailText = emailField.getText().trim();
                String contactText = contactField.getText().trim();
                String passwordText = passwordField.getText();

                if (!fullName.matches("[a-zA-Z ]+")) {

                    SharedStyles.showError(this,
                            "Name must contain letters only.");

                    return;
                }

                if (!ValidationUtil.isValidEmail(emailText)) {

                    SharedStyles.showError(this,
                            ValidationUtil.invalidEmailMessage());

                    return;
                }

                if (!ValidationUtil.isValidContact(contactText)) {

                    SharedStyles.showError(this,
                            ValidationUtil.invalidContactMessage());

                    return;
                }

                if (!passwordText.trim().isEmpty()
                        && !ValidationUtil.isValidPassword(passwordText)) {

                    SharedStyles.showError(this,
                            ValidationUtil.passwordRequirementsMessage());

                    return;
                }
                User existing = context.userService().findByUserId(id);
                if (existing != null) {
                    existing.setFullName(fullName);
                    existing.setEmail(emailText);
                    existing.setContact(contactText);

                    if (!passwordText.trim().isEmpty()) {
                        existing.setPassword(passwordText);
                    }

                    Result<Void> result = context.userService().updateUser(existing);
                    if (result.isSuccess()) {
                    int selectedIndex = vehicleBox.getSelectedIndex();

                if (selectedIndex >= 0 && selectedIndex < vehicles.size()) {

                    Vehicle vehicle = vehicles.get(selectedIndex);

                    vehicle.setPlateNumber(
                            plateField.getText().trim());

                    vehicle.setBrand(
                            brandField.getText().trim());

                    vehicle.setModel(
                            modelField.getText().trim());

                    context.vehicleService().updateVehicle(vehicle);
                }
                        SharedStyles.showMessage(this, "Customer updated!");
                        context.refreshAction().run();
                    } else {
                        SharedStyles.showError(this, result.getError());
                    }
                }
            }
        });

            deactivateBtn.addActionListener(e -> {            int row = table.getSelectedRow();
            if (row == -1) {
                SharedStyles.showSelectionError(this);
                return;
            }

            String id = table.getValueAt(row, 0).toString();
            if (SharedStyles.showConfirm(this,
                    "Are you sure you want to deactivate this customer?")) {
                User customer = context.userService().findByUserId(id);

                if (customer != null) {

                    customer.setActive(false);

                    Result<Void> result =
                            context.userService().updateUser(customer);

                    if (result.isSuccess()) {

                        SharedStyles.showMessage(this,
                                "Customer deactivated successfully!");

                        context.refreshAction().run();

                    } else {

                        SharedStyles.showError(this,
                                result.getError());
                    }
                }
            }
        });

        deleteBtn.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {
                SharedStyles.showSelectionError(this);
                return;
            }

            String id = table.getValueAt(row, 0).toString();

            if (SharedStyles.showConfirm(this,
                    "Are you sure you want to permanently delete this customer?")) {

                Result<Void> result =
                        context.userService().deleteUser(id);

                if (result.isSuccess()) {

                    SharedStyles.showMessage(this,
                            "Customer deleted successfully!");

                    context.refreshAction().run();

                } else {

                    SharedStyles.showError(this,
                            result.getError());
                }
            }
        });

        add(root, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }
}
