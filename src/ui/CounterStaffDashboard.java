package ui;

import abstracts.AbstractUser;
import service_layer.UserService;
import model.users.User;
import model.users.Customer;
import utils.IdGenerator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

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
            case "Process Payment": panel = buildPlaceholderPanel("Process Payment"); break;
            case "My Profile": panel = buildMyProfilePanel(); break;
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
        top.setOpaque(false);

        JButton addBtn = SharedStyles.createActionButton("Add Customer", SharedStyles.BTN_GREEN);
        top.add(addBtn);

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
                String id = IdGenerator.generateId("CUS", "data/users.txt");
                userService.addCustomer(new Customer(id, name.getText(), email.getText(), contact.getText(), password.getText()));
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

        top.add(addBtn);
        top.add(updateBtn);
        root.add(top, BorderLayout.NORTH);

        String[] columns = {"ID", "Customer", "Vehicle", "Service", "Date", "Time", "Status"};

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
                    a.getStatus()
            });
        }

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

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
}