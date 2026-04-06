package ui;

import abstracts.AbstractUser;
import javax.swing.*;
import java.awt.*;

public class CounterStaffDashboard extends JFrame {

    private JPanel contentPanel;

    public CounterStaffDashboard(AbstractUser user) {
        setTitle("Counter Staff Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // TOP TITLE
        JLabel title = new JLabel("Counter Staff Dashboard - " + user.getFullName(), SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // LEFT MENU
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(6, 1, 10, 10));

        JButton profileBtn = new JButton("Edit Profile");
        JButton customerBtn = new JButton("Manage Customer");
        JButton appointmentBtn = new JButton("Manage Appointment");
        JButton paymentBtn = new JButton("Process Payment");
        JButton logoutBtn = new JButton("Logout");

        menuPanel.add(profileBtn);
        menuPanel.add(customerBtn);
        menuPanel.add(appointmentBtn);
        menuPanel.add(paymentBtn);
        menuPanel.add(logoutBtn);

        add(menuPanel, BorderLayout.WEST);

        // CENTER CONTENT PANEL
        contentPanel = new JPanel();
        add(contentPanel, BorderLayout.CENTER);

        // LOGOUT
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        // Sang Yew Changes for Buttons
        profileBtn.addActionListener(e -> showEditProfile(user));
        customerBtn.addActionListener(e -> showCustomerPanel());
        appointmentBtn.addActionListener(e -> showMessage("Manage Appointment clicked"));
        paymentBtn.addActionListener(e -> showMessage("Process Payment clicked"));
    }

    private void showMessage(String message) {
        contentPanel.removeAll();
        contentPanel.add(new JLabel(message));
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    //Sang Yew Changes - Edit Profile
    private void showEditProfile(AbstractUser user) {
    contentPanel.removeAll();
    contentPanel.setLayout(new GridLayout(5, 2, 10, 10));

    JTextField nameField = new JTextField(user.getFullName());
    JTextField contactField = new JTextField(user.getContact());
    JPasswordField passwordField = new JPasswordField(user.getPassword());

    JButton saveBtn = new JButton("Save");

    contentPanel.add(new JLabel("Full Name:"));
    contentPanel.add(nameField);

    contentPanel.add(new JLabel("Contact:"));
    contentPanel.add(contactField);

    contentPanel.add(new JLabel("Password:"));
    contentPanel.add(passwordField);

    contentPanel.add(new JLabel(""));
    contentPanel.add(saveBtn);

    saveBtn.addActionListener(e -> {
        user.setFullName(nameField.getText());
        user.setContact(contactField.getText());
        user.setPassword(new String(passwordField.getPassword()));

        service_layer.UserService service = new service_layer.UserService();
        service.updateUser((model.users.User) user);

        JOptionPane.showMessageDialog(this, "Profile updated!");
    });

    contentPanel.revalidate();
    contentPanel.repaint();
}

    //Sang Yew Changes - Manage Customer
    private void showCustomerPanel() {
    contentPanel.removeAll();
    contentPanel.setLayout(new BorderLayout());

    service_layer.UserService service = new service_layer.UserService();

    // TABLE
    String[] columns = {"ID", "Name", "Email", "Contact"};
    java.util.List<model.users.User> customers = service.getAllCustomers();

    String[][] data = new String[customers.size()][4];

    for (int i = 0; i < customers.size(); i++) {
        model.users.User c = customers.get(i);
        data[i][0] = c.getUserId();
        data[i][1] = c.getFullName();
        data[i][2] = c.getEmail();
        data[i][3] = c.getContact();
    }

    JTable table = new JTable(data, columns);
    JScrollPane scrollPane = new JScrollPane(table);

    // BUTTONS
    JPanel btnPanel = new JPanel();

    JButton addBtn = new JButton("Add Customer");
    JButton deleteBtn = new JButton("Delete Customer");

    btnPanel.add(addBtn);
    btnPanel.add(deleteBtn);

    contentPanel.add(scrollPane, BorderLayout.CENTER);
    contentPanel.add(btnPanel, BorderLayout.SOUTH);

    // ADD CUSTOMER
    addBtn.addActionListener(e -> {
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JTextField contact = new JTextField();
        JTextField password = new JTextField();

        Object[] fields = {
            "Name:", name,
            "Email:", email,
            "Contact:", contact,
            "Password:", password
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Add Customer", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String id = "U" + System.currentTimeMillis();

            model.users.Customer newCustomer = new model.users.Customer(
                    id,
                    name.getText(),
                    email.getText(),
                    contact.getText(),
                    password.getText()
            );

            service.addCustomer(newCustomer);

            JOptionPane.showMessageDialog(this, "Customer added!");
            showCustomerPanel(); // refresh
        }
    });

    // DELETE CUSTOMER
    deleteBtn.addActionListener(e -> {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a customer first!");
            return;
        }

        String userId = table.getValueAt(row, 0).toString();
        service.deleteUser(userId);

        JOptionPane.showMessageDialog(this, "Customer deleted!");
        showCustomerPanel(); // refresh
    });

    contentPanel.revalidate();
    contentPanel.repaint();
}
}