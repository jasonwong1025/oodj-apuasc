package ui;

import abstracts.AbstractUser;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.users.Customer;
import model.users.User;
import service_layer.UserService;
import utils.IdGenerator;

public class CounterStaffDashboard extends JFrame implements Refreshable {

    private AbstractUser currentUser;
    private UserService userService;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DefaultListModel<String> navModel;
    private JList<String> navList;

    private static final String[] NAV_ITEMS = {
            "Dashboard",
            "Manage Customers",
            "Manage Appointments",
            "Process Payment",
            "My Profile"
    };

    public CounterStaffDashboard(AbstractUser user) {
        this.currentUser = user;
        this.userService = new UserService();

        setTitle("APU-ASC | Counter Staff - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(SharedStyles.MAIN_BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebarAndContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(SharedStyles.HEADER_BG);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel brand = new JLabel("APU Automotive Service Centre");
        brand.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(brand, BorderLayout.WEST);

        JLabel who = new JLabel(currentUser.getFullName() + "  |  Counter Staff");
        who.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JButton logout = SharedStyles.createActionButton("Logout", SharedStyles.BTN_LOGOUT);
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        east.setOpaque(false);
        east.add(who);
        east.add(logout);
        header.add(east, BorderLayout.EAST);

        return header;
    }

    private JPanel buildSidebarAndContent() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        navModel = new DefaultListModel<>();
        for (String s : NAV_ITEMS) navModel.addElement(s);

        navList = new JList<>(navModel);
        navList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navList.setBackground(SharedStyles.SIDEBAR_BG);
        navList.setForeground(SharedStyles.TEXT_ON_DARK);
        navList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        navList.setFixedCellHeight(46);
        navList.setBorder(new EmptyBorder(12, 0, 12, 0));
        navList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setBorder(new EmptyBorder(12, 20, 12, 16));
                if (isSelected) {
                    l.setBackground(SharedStyles.NAV_ACTIVE_TOP);
                    l.setForeground(Color.WHITE);
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                } else {
                    l.setBackground(SharedStyles.SIDEBAR_BG);
                    l.setForeground(SharedStyles.TEXT_ON_DARK);
                }
                return l;
            }
        });

        JScrollPane navScroll = new JScrollPane(navList);
        navScroll.setBorder(null);

        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(SharedStyles.SIDEBAR_BG);
        side.setPreferredSize(new Dimension(240, 0));
        side.add(navScroll, BorderLayout.CENTER);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        navList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            refresh();
        });

        navList.setSelectedIndex(0);

        wrap.add(side, BorderLayout.WEST);
        wrap.add(cardPanel, BorderLayout.CENTER);
        return wrap;
    }

    @Override
    public void refresh() {
        String selected = navList.getSelectedValue();
        if (selected == null) return;

        JPanel panel;
        switch (selected) {
            case "Dashboard": panel = buildDashboardPanel(); break;
            case "Manage Customers": panel = buildCustomerManagementPanel(); break;
            case "Manage Appointments": panel = buildAppointmentPanel(); break;
            case "Process Payment": panel = buildPaymentPanel(); break;            case "My Profile": panel = buildMyProfilePanel(); break;
            default: panel = new JPanel();
        }

        Component existing = null;
        for (Component c : cardPanel.getComponents()) {
            if (selected.equals(c.getName())) {
                existing = c;
                break;
            }
        }
        if (existing != null) cardPanel.remove(existing);

        panel.setName(selected);
        cardPanel.add(panel, selected);
        cardLayout.show(cardPanel, selected);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private JPanel buildDashboardPanel() {
    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(SharedStyles.MAIN_BG);
    root.setBorder(new EmptyBorder(16, 20, 20, 20));

    JPanel card = SharedStyles.createCardPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

    service_layer.UserService userService = new service_layer.UserService();
    service_layer.AppointmentService appointmentService = new service_layer.AppointmentService();

    int totalCustomers = userService.getAllCustomers().size();
    java.util.List<model.appointment.Appointment> appointments = appointmentService.getAllAppointments();

    int pending = 0;
    for (model.appointment.Appointment a : appointments) {
        if ("PENDING".equals(a.getStatus())) {
            pending++;
        }
    }

    JLabel title = new JLabel("Welcome, " + currentUser.getFullName());
    title.setFont(new Font("SansSerif", Font.BOLD, 20));

    JLabel c1 = new JLabel("Total Customers: " + totalCustomers);
    JLabel c2 = new JLabel("Total Appointments: " + appointments.size());
    JLabel c3 = new JLabel("Pending Appointments: " + pending);

    title.setBorder(new EmptyBorder(0, 0, 10, 0));
    c1.setBorder(new EmptyBorder(5, 0, 5, 0));
    c2.setBorder(new EmptyBorder(5, 0, 5, 0));
    c3.setBorder(new EmptyBorder(5, 0, 5, 0));

    card.add(title);
    card.add(c1);
    card.add(c2);
    card.add(c3);

    root.add(card, BorderLayout.NORTH);

    return root;
}

    private JPanel buildCustomerManagementPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        JComboBox<String> filterBox = new JComboBox<>(new String[]{"All", "Unpaid", "Partial"});
        top.add(new JLabel("Filter:"));
        top.add(filterBox);
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

        List<User> customers = userService.getAllCustomers();
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

            Object[] fields = {"Name:", name, "Email:", email, "Contact:", contact, "Password:", password};

            if (JOptionPane.showConfirmDialog(this, fields, "Add Customer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String id = IdGenerator.generateId("C", "data/users.txt");
                userService.addCustomer(new Customer(id, name.getText(), email.getText(), contact.getText(), password.getText()));
                refresh();
            }
        });
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a customer!");
               return;
           }

            String id = table.getValueAt(row, 0).toString();
            String name = table.getValueAt(row, 1).toString();
            String email = table.getValueAt(row, 2).toString();
            String contact = table.getValueAt(row, 3).toString();

            JTextField nameField = SharedStyles.createFilterField(20);
            JTextField emailField = SharedStyles.createFilterField(20);
            JTextField contactField = SharedStyles.createFilterField(20);

            nameField.setText(name);
            emailField.setText(email);
            contactField.setText(contact);

            Object[] fields = {
                    "Name:", nameField,
                    "Email:", emailField,
                    "Contact:", contactField
            };

            if (JOptionPane.showConfirmDialog(this, fields, "Edit Customer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                model.users.User existing = userService.findByUserId(id);

                existing.setFullName(nameField.getText());
                existing.setEmail(emailField.getText());
                existing.setContact(contactField.getText());

                userService.updateUser(existing);

                JOptionPane.showMessageDialog(this, "Customer updated!");
                refresh();
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
                userService.deleteUser(id);
                JOptionPane.showMessageDialog(this, "Customer deleted!");
                refresh();
            }
        });

        return root;
    }

    private JPanel buildAppointmentPanel() {
    JPanel root = new JPanel(new BorderLayout(0, 15));
    root.setBackground(SharedStyles.MAIN_BG);
    root.setBorder(new EmptyBorder(16, 20, 20, 20));

    service_layer.AppointmentService service = new service_layer.AppointmentService();
    List<model.appointment.Appointment> list = service.getAllAppointments();

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
    top.setOpaque(false);

    JButton addBtn = SharedStyles.createActionButton("Add Appointment", SharedStyles.BTN_GREEN);
    JButton updateBtn = SharedStyles.createActionButton("Update Status", SharedStyles.BTN_BLUE);
    JButton assignBtn = SharedStyles.createActionButton("Assign Technician", SharedStyles.BTN_RED);

    top.add(addBtn);
    top.add(updateBtn);
    top.add(assignBtn);

    root.add(top, BorderLayout.NORTH);

    String[] columns = {"ID", "Customer", "Vehicle", "Service", "Date", "Time", "Status", "Technician"};

    DefaultTableModel model = new DefaultTableModel(columns, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    for (model.appointment.Appointment a : list) {
        model.addRow(new Object[]{
                a.getAppointmentId(),
                a.getCustomerId(),
                a.getVehicleId(),
                a.getServiceId(),
                a.getDate(),
                a.getTime(),
                a.getStatus(),
                a.getTechnicianId()

        });
    }

    JTable table = new JTable(model);
    SharedStyles.applyTableStyle(table);
    root.add(new JScrollPane(table), BorderLayout.CENTER);

    // ADD
    addBtn.addActionListener(e -> {
        JTextField c = SharedStyles.createFilterField(20);
        JTextField v = SharedStyles.createFilterField(20);
        JTextField s = SharedStyles.createFilterField(20);
        JTextField d = SharedStyles.createFilterField(20);
        JTextField t = SharedStyles.createFilterField(20);

        Object[] fields = {"Customer ID:", c, "Vehicle ID:", v, "Service ID:", s, "Date:", d, "Time:", t};

        if (JOptionPane.showConfirmDialog(this, fields, "Add Appointment", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String result = service.bookAppointment(c.getText(), v.getText(), java.util.Arrays.asList(s.getText()), d.getText(), t.getText());
            JOptionPane.showMessageDialog(this, result);
            refresh();
        }
    });

    // UPDATE STATUS
    updateBtn.addActionListener(e -> {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select row");
            return;
        }

        String id = table.getValueAt(row, 0).toString();

        String status = (String) JOptionPane.showInputDialog(
                this,
                "Status",
                "Update",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"PENDING", "COMPLETED", "CANCELLED"},
                "PENDING"
        );

        if (status != null) {
            for (model.appointment.Appointment a : list) {
                if (a.getAppointmentId().equals(id)) {
                    a.setStatus(status);
                    new repository.AppointmentRepository().update(a);
                    break;
                }
            }
            JOptionPane.showMessageDialog(this, "Status updated");
            refresh();
        }
    });

    // ASSIGN TECHNICIAN
    assignBtn.addActionListener(e -> {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select appointment first");
            return;
        }

        String appointmentId = table.getValueAt(row, 0).toString();

        service_layer.UserService userService = new service_layer.UserService();
        List<model.users.User> allUsers = userService.listAllUsers();

        java.util.List<String> technicians = new java.util.ArrayList<>();

        for (model.users.User u : allUsers) {
            if ("Technician".equals(u.getRole())) {
                technicians.add(u.getUserId());
            }
        }

        if (technicians.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No technicians available");
            return;
        }

        String selectedTech = (String) JOptionPane.showInputDialog(
                this,
                "Select Technician:",
                "Assign Technician",
                JOptionPane.QUESTION_MESSAGE,
                null,
                technicians.toArray(),
                technicians.get(0)
        );

        if (selectedTech != null) {
            for (model.appointment.Appointment a : list) {
                if (a.getAppointmentId().equals(appointmentId)) {
                    a.setTechnicianId(selectedTech);
                    new repository.AppointmentRepository().update(a);
                    break;
                }
            }

            JOptionPane.showMessageDialog(this, "Technician assigned!");
            refresh();
        }
    });

    return root;
}

    private JPanel buildMyProfilePanel() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(SharedStyles.MAIN_BG);
        JPanel card = SharedStyles.createCardPanel();
        root.add(card);
        return root;
    }

    private JPanel buildPlaceholderPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SharedStyles.MAIN_BG);
        p.add(new JLabel(title + " Panel"));
        return p;
    }

//PAYMENT
    private JPanel buildPaymentPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        service_layer.AppointmentService apptService = new service_layer.AppointmentService();
        service_layer.PaymentService paymentService = new service_layer.PaymentService();

        java.util.List<model.appointment.Appointment> list = apptService.getAllAppointments();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setOpaque(false);

        JComboBox<String> filterBox = new JComboBox<>(new String[]{"All", "Unpaid", "Partial"});
        top.add(new JLabel("Filter:"));
        top.add(filterBox);

        JButton payBtn = SharedStyles.createActionButton("Process Payment", SharedStyles.BTN_GREEN);
        JButton deleteBtn = SharedStyles.createActionButton("Delete Payment", SharedStyles.BTN_RED);
        JButton receiptBtn = SharedStyles.createActionButton("Print Receipt", SharedStyles.BTN_BLUE);

        top.add(payBtn);
        top.add(deleteBtn);
        top.add(receiptBtn);

        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Appointment ID", "Customer", "Service", "Price", "Payment Status", "Remaining"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (model.appointment.Appointment a : list) {

            String rawServiceId = a.getServiceId();
            String[] serviceIds = rawServiceId.split(",");

            StringBuilder serviceNames = new StringBuilder();
            double totalPrice = 0;

            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("data/services.txt"))) {

                java.util.List<String[]> services = new java.util.ArrayList<>();
                String line;

                while ((line = br.readLine()) != null) {
                    services.add(line.split("\\|"));
                }

                for (String sid : serviceIds) {
                    String idNumber = sid.replaceAll("\\D", "");

                    for (String[] parts : services) {
                        String serviceNumber = parts[0].replaceAll("\\D", "");

                        if (serviceNumber.equals(idNumber)) {
                            serviceNames.append(parts[1]).append(", ");
                            totalPrice += Double.parseDouble(parts[3]);
                            break;
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            String finalServiceNames = serviceNames.length() > 0
                    ? serviceNames.substring(0, serviceNames.length() - 2)
                    : "Unknown";

            String paymentStatus = "UNPAID";
            double remaining = totalPrice;

            java.util.List<model.payment.Payment> payments =
                    paymentService.getCustomerPayments(a.getCustomerId());

            for (model.payment.Payment p : payments) {
                if (p.getAppointmentId().equals(a.getAppointmentId())) {
                    paymentStatus = p.getStatus();
                    remaining = p.getRemainingAmount();
                    break;
                }
            }

            String selectedFilter = filterBox.getSelectedItem().toString();

            if (selectedFilter.equals("Unpaid") && !paymentStatus.equalsIgnoreCase("UNPAID")) continue;
            if (selectedFilter.equals("Partial") && !paymentStatus.equalsIgnoreCase("PARTIAL")) continue;

            model.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getCustomerId(),
                    finalServiceNames,
                    totalPrice,
                    paymentStatus,
                    remaining
            });
        }

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

    filterBox.addActionListener(e -> {
        model.setRowCount(0); // clear table

        for (model.appointment.Appointment a : list) {

            String rawServiceId = a.getServiceId();
            String[] serviceIds = rawServiceId.split(",");

            StringBuilder serviceNames = new StringBuilder();
            double totalPrice = 0;

            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("data/services.txt"))) {

                java.util.List<String[]> services = new java.util.ArrayList<>();
                String line;

                while ((line = br.readLine()) != null) {
                    services.add(line.split("\\|"));
                }

                for (String sid : serviceIds) {
                    String idNumber = sid.replaceAll("\\D", "");

                    for (String[] parts : services) {
                        String serviceNumber = parts[0].replaceAll("\\D", "");

                        if (serviceNumber.equals(idNumber)) {
                            serviceNames.append(parts[1]).append(", ");
                            totalPrice += Double.parseDouble(parts[3]);
                            break;
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            String finalServiceNames = serviceNames.length() > 0
                    ? serviceNames.substring(0, serviceNames.length() - 2)
                    : "Unknown";

            String paymentStatus = "UNPAID";
            double remaining = totalPrice;

            java.util.List<model.payment.Payment> payments =
                    paymentService.getCustomerPayments(a.getCustomerId());

            for (model.payment.Payment p : payments) {
                if (p.getAppointmentId().equals(a.getAppointmentId())) {
                    paymentStatus = p.getStatus();
                    remaining = p.getRemainingAmount();
                    break;
                }
            }

            String selectedFilter = filterBox.getSelectedItem().toString();

            if (selectedFilter.equals("Unpaid") && !paymentStatus.equalsIgnoreCase("UNPAID")) continue;
            if (selectedFilter.equals("Partial") && !paymentStatus.equalsIgnoreCase("PARTIAL")) continue;

            model.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getCustomerId(),
                    finalServiceNames,
                    totalPrice,
                    paymentStatus,
                    remaining
            });
        }
    });

        payBtn.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select appointment!");
                return;
            }

            String appointmentId = table.getValueAt(row, 0).toString();
            String customerId = table.getValueAt(row, 1).toString();
            String paymentStatus = table.getValueAt(row, 4).toString();

            if ("PAID".equalsIgnoreCase(paymentStatus)) {
                JOptionPane.showMessageDialog(this, "Already fully paid!");
                return;
            }

            double totalPrice = Double.parseDouble(table.getValueAt(row, 3).toString());

            JTextField amountField = SharedStyles.createFilterField(20);
            amountField.setText(String.valueOf(totalPrice));

            Object[] fields = {"Amount:", amountField};

            if (JOptionPane.showConfirmDialog(this, fields, "Process Payment", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    double enteredAmount = Double.parseDouble(amountField.getText());
                    double remaining = totalPrice - enteredAmount;

                    String newStatus = (remaining <= 0) ? "PAID" : "PARTIAL";
                    if (remaining <= 0) remaining = 0;

                    model.payment.Payment payment = new model.payment.Payment(
                            utils.IdGenerator.generateId("PAY", "data/payments.txt"),
                            appointmentId,
                            customerId,
                            enteredAmount,
                            remaining,
                            java.time.LocalDate.now().toString(),
                            newStatus
                    );

                    paymentService.processPayment(payment);

                    JOptionPane.showMessageDialog(this, "Payment recorded!");
                    refresh();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid amount!");
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a row!");
                return;
            }

            String appointmentId = table.getValueAt(row, 0).toString();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete payment for this appointment?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) return;

            java.util.List<model.payment.Payment> all =
                    paymentService.getCustomerPayments(table.getValueAt(row,1).toString());

            java.util.List<model.payment.Payment> updated = new java.util.ArrayList<>();

            for (model.payment.Payment p : all) {
                if (!p.getAppointmentId().equals(appointmentId)) {
                    updated.add(p);
                }
            }

            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter("data/payments.txt"))) {
                for (model.payment.Payment p : updated) {
                    bw.write(p.toString());
                    bw.newLine();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            JOptionPane.showMessageDialog(this, "Payment deleted!");
            refresh();
        });

        receiptBtn.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a row!");
                return;
            }

            String receipt = "====== RECEIPT ======\n"
                    + "Appointment: " + table.getValueAt(row, 0) + "\n"
                    + "Customer: " + table.getValueAt(row, 1) + "\n"
                    + "Service: " + table.getValueAt(row, 2) + "\n"
                    + "Total: RM " + table.getValueAt(row, 3) + "\n"
                    + "Status: " + table.getValueAt(row, 4) + "\n"
                    + "Remaining: RM " + table.getValueAt(row, 5) + "\n"
                    + "Date: " + java.time.LocalDate.now() + "\n"
                    + "=====================";

            JOptionPane.showMessageDialog(this, receipt);
        });

        return root;
    }
}