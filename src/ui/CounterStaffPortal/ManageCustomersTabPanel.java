package ui.CounterStaffPortal;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.users.Customer;
import model.users.User;
import model.vehicle.Vehicle;
import repository.VehicleRepository;
import ui.SharedStyles;
import utils.IdGenerator;
import utils.Result;
import java.util.List;

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
        JButton deleteBtn = SharedStyles.createActionButton("Delete Customer", SharedStyles.BTN_RED);
        top.add(deleteBtn);

        root.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Email", "Contact"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<User> customers = context.userService().getAllCustomers();
        for (User u : customers) {
            model.addRow(new Object[]{u.getUserId(), u.getFullName(), u.getEmail(), u.getContact()});
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

            if (JOptionPane.showConfirmDialog(this, fields, "Add Customer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
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
                new VehicleRepository().save(v);

                JOptionPane.showMessageDialog(this, "Customer + Vehicle added successfully!");
                context.refreshAction().run();
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a customer!");
                return;
            }

            String id = table.getValueAt(row, 0).toString();
            String nameVal = table.getValueAt(row, 1).toString();
            String emailVal = table.getValueAt(row, 2).toString();
            String contactVal = table.getValueAt(row, 3).toString();

            JTextField nameField = SharedStyles.createFilterField(20);
            JTextField emailField = SharedStyles.createFilterField(20);
            JTextField contactField = SharedStyles.createFilterField(20);

            nameField.setText(nameVal);
            emailField.setText(emailVal);
            contactField.setText(contactVal);

            Object[] fields = {
                "Name:", nameField,
                "Email:", emailField,
                "Contact:", contactField
            };

            if (JOptionPane.showConfirmDialog(this, fields, "Edit Customer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                User existing = context.userService().findByUserId(id);
                if (existing != null) {
                    existing.setFullName(nameField.getText());
                    existing.setEmail(emailField.getText());
                    existing.setContact(contactField.getText());

                    Result<Void> result = context.userService().updateUser(existing);
                    if (result.isSuccess()) {
                        JOptionPane.showMessageDialog(this, "Customer updated!");
                        context.refreshAction().run();
                    } else {
                        JOptionPane.showMessageDialog(this, result.getError(), "Update Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a customer!");
                return;
            }

            String id = table.getValueAt(row, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this customer?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                Result<Void> result = context.userService().deleteUser(id);
                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Customer deleted!");
                    context.refreshAction().run();
                } else {
                    JOptionPane.showMessageDialog(this, result.getError(), "Delete Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        add(root, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }
}
