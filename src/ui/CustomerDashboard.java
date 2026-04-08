package ui;

import abstracts.AbstractUser;
import model.appointment.Appointment;
import model.review.Review;
import model.vehicle.Vehicle;
import model.service.Service;
import service_layer.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerDashboard extends JFrame {

    private static final String[] NAV_ITEMS = {
            "Dashboard",
            "Manage Vehicles",
            "Book Appointment",
            "My Appointments",
            "Service History",
            "Reviews",
            "My Profile"
    };

    private final AbstractUser currentUser;
    private final VehicleService vehicleService;
    private final AppointmentService appointmentService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final ServiceService serviceLookup;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DefaultListModel<String> navModel;
    private JList<String> navList;

    public CustomerDashboard(AbstractUser user) {
        this.currentUser = user;
        this.vehicleService = new VehicleService();
        this.appointmentService = new AppointmentService();
        this.paymentService = new PaymentService();
        this.reviewService = new ReviewService();
        this.serviceLookup = new ServiceService();

        setTitle("APU-ASC | Customer - " + currentUser.getFullName());
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

        JLabel who = new JLabel(currentUser.getFullName() + "  |  Customer");
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

        // Panels will be built on demand or refresh
        cardPanel.add(buildDashboardPanel(), "Dashboard");
        cardPanel.add(buildVehiclesPanel(), "Manage Vehicles");
        cardPanel.add(buildBookingPanel(), "Book Appointment");
        cardPanel.add(buildAppointmentsPanel(), "My Appointments");
        cardPanel.add(buildHistoryPanel(), "Service History");
        cardPanel.add(buildReviewsPanel(), "Reviews");
        cardPanel.add(buildMyProfilePanel(), "My Profile");

        navList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String selected = navList.getSelectedValue();
            if (selected == null) return;
            refreshSelectedPanel(selected);
            cardLayout.show(cardPanel, selected);
        });
        navList.setSelectedIndex(0);

        wrap.add(side, BorderLayout.WEST);
        wrap.add(cardPanel, BorderLayout.CENTER);
        return wrap;
    }

    private void refreshSelectedPanel(String name) {
        switch (name) {
            case "Dashboard": cardPanel.add(buildDashboardPanel(), "Dashboard"); break;
            case "Manage Vehicles": cardPanel.add(buildVehiclesPanel(), "Manage Vehicles"); break;
            case "Book Appointment": cardPanel.add(buildBookingPanel(), "Book Appointment"); break;
            case "My Appointments": cardPanel.add(buildAppointmentsPanel(), "My Appointments"); break;
            case "Service History": cardPanel.add(buildHistoryPanel(), "Service History"); break;
            case "Reviews": cardPanel.add(buildReviewsPanel(), "Reviews"); break;
            case "My Profile": cardPanel.add(buildMyProfilePanel(), "My Profile"); break;
        }
    }

    private JPanel buildDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SharedStyles.MAIN_BG);
        p.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel welcome = new JLabel("Welcome back, " + currentUser.getFullName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 28));
        p.add(welcome, BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        statsGrid.setOpaque(false);
        statsGrid.setBorder(new EmptyBorder(30, 0, 30, 0));

        List<Vehicle> vehicles = vehicleService.getCustomerVehicles(currentUser.getUserId());
        List<Appointment> appointments = appointmentService.getCustomerAppointments(currentUser.getUserId());
        int pending = 0;
        for (Appointment a : appointments) if (a.getStatus().equals("PENDING")) pending++;

        statsGrid.add(createStatCard("Registered Vehicles", String.valueOf(vehicles.size())));
        statsGrid.add(createStatCard("Active Appointments", String.valueOf(pending)));
        statsGrid.add(createStatCard("Total Services", String.valueOf(appointments.size())));

        p.add(statsGrid, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 16));
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 36));
        v.setForeground(SharedStyles.NAV_ACTIVE_TOP);
        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildVehiclesPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setOpaque(false);
        JButton addBtn = SharedStyles.createActionButton("Add New Vehicle", SharedStyles.BTN_GREEN);
        addBtn.addActionListener(e -> showAddVehicleDialog());
        top.add(addBtn);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Vehicle ID", "Plate Number", "Brand", "Model"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Vehicle> list = vehicleService.getCustomerVehicles(currentUser.getUserId());
        for (Vehicle v : list) model.addRow(new Object[]{v.getVehicleId(), v.getPlateNumber(), v.getBrand(), v.getModel()});

        JTable table = createStyledTable(model);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildBookingPanel() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(SharedStyles.MAIN_BG);
        
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Vehicle> vehicles = vehicleService.getCustomerVehicles(currentUser.getUserId());
        if (vehicles.isEmpty()) {
            card.add(new JLabel("Please register a vehicle first!"), gbc);
            center.add(card);
            return center;
        }

        JComboBox<String> vehicleCombo = SharedStyles.createFilterCombo(
            vehicles.stream().map(v -> v.getVehicleId() + " - " + v.getPlateNumber()).toArray(String[]::new)
        );
        List<Service> services = serviceLookup.listAll();
        JComboBox<String> serviceCombo = SharedStyles.createFilterCombo(
            services.stream().map(s -> s.getServiceId() + " - " + s.getServiceName()).toArray(String[]::new)
        );

        JTextField dateField = SharedStyles.createFilterField(20);
        dateField.setText("2026-05-01");
        JTextField timeField = SharedStyles.createFilterField(20);
        timeField.setText("10:00");

        int y = 0;
        addFormRow(card, gbc, y++, "Select Vehicle:", vehicleCombo);
        addFormRow(card, gbc, y++, "Select Service:", serviceCombo);
        addFormRow(card, gbc, y++, "Preferred Date:", dateField);
        addFormRow(card, gbc, y++, "Preferred Time:", timeField);

        JButton bookBtn = SharedStyles.createActionButton("Book Appointment", SharedStyles.BTN_BLUE);
        bookBtn.addActionListener(e -> {
            String vId = vehicleCombo.getSelectedItem().toString().split(" - ")[0];
            String sId = serviceCombo.getSelectedItem().toString().split(" - ")[0];
            String res = appointmentService.bookAppointment(currentUser.getUserId(), vId, sId, dateField.getText(), timeField.getText());
            JOptionPane.showMessageDialog(this, res);
            navList.setSelectedIndex(3); // Switch to My Appointments
        });
        gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        card.add(bookBtn, gbc);

        center.add(card);
        return center;
    }

    private JPanel buildAppointmentsPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        top.setOpaque(false);
        JButton cancelBtn = SharedStyles.createActionButton("Cancel Selected", SharedStyles.BTN_RED);
        top.add(cancelBtn);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Vehicle", "Service", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> list = appointmentService.getCustomerAppointments(currentUser.getUserId());
        for (Appointment a : list) {
            if (a.getStatus().equals("PENDING")) {
                model.addRow(new Object[]{a.getAppointmentId(), a.getVehicleId(), a.getServiceId(), a.getDate(), a.getTime(), a.getStatus()});
            }
        }

        JTable table = createStyledTable(model);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                appointmentService.cancelAppointment(table.getValueAt(row, 0).toString());
                refreshSelectedPanel("My Appointments");
            }
        });
        return root;
    }

    private JPanel buildHistoryPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        String[] cols = {"Apt ID", "Service", "Date", "Status", "Payment", "Tech Feedback"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> list = appointmentService.getCustomerAppointments(currentUser.getUserId());
        for (Appointment a : list) {
            if (!a.getStatus().equals("PENDING")) {
                boolean isPaid = paymentService.isPaid(a.getAppointmentId());
                model.addRow(new Object[]{a.getAppointmentId(), a.getServiceId(), a.getDate(), a.getStatus(), isPaid ? "PAID" : "UNPAID", a.getTechnicianFeedback()});
            }
        }

        JTable table = createStyledTable(model);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildReviewsPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        top.setOpaque(false);
        JButton reviewBtn = SharedStyles.createActionButton("Write Review", SharedStyles.BTN_BLUE);
        top.add(reviewBtn);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Service", "Date", "Action State"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> list = appointmentService.getCustomerAppointments(currentUser.getUserId());
        List<Review> reviews = reviewService.getCustomerReviews(currentUser.getUserId());

        for (Appointment a : list) {
            if (a.getStatus().equals("COMPLETED")) {
                boolean reviewed = reviews.stream().anyMatch(r -> r.getAppointmentId().equals(a.getAppointmentId()));
                boolean paid = paymentService.isPaid(a.getAppointmentId());
                String status = reviewed ? "Reviewed" : (paid ? "Available" : "Payment Required");
                model.addRow(new Object[]{a.getAppointmentId(), a.getServiceId(), a.getDate(), status});
            }
        }

        JTable table = createStyledTable(model);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        reviewBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1 && table.getValueAt(row, 3).equals("Available")) {
                showReviewDialog(table.getValueAt(row, 0).toString());
            } else if (row != -1) {
                JOptionPane.showMessageDialog(this, "Status: " + table.getValueAt(row, 3));
            }
        });
        return root;
    }

    private JPanel buildMyProfilePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SharedStyles.MAIN_BG);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;
        JTextField nameF = SharedStyles.createFilterField(25); nameF.setText(currentUser.getFullName());
        JTextField emailF = SharedStyles.createFilterField(25); emailF.setText(currentUser.getEmail());
        JTextField contactF = SharedStyles.createFilterField(25); contactF.setText(currentUser.getContact());
        JPasswordField passF = new JPasswordField(25); passF.setBorder(nameF.getBorder());

        addFormRow(p, gbc, y++, "Full Name:", nameF);
        addFormRow(p, gbc, y++, "Email:", emailF);
        addFormRow(p, gbc, y++, "Contact:", contactF);
        addFormRow(p, gbc, y++, "New Password:", passF);

        JButton saveBtn = SharedStyles.createActionButton("Update Profile", SharedStyles.BTN_GREEN);
        gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        p.add(saveBtn, gbc);

        return p;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        table.setGridColor(new Color(230, 230, 230));
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                return c;
            }
        });
        return table;
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int y, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        p.add(comp, gbc);
    }

    private void showAddVehicleDialog() {
        JTextField plate = SharedStyles.createFilterField(20);
        JTextField brand = SharedStyles.createFilterField(20);
        JTextField modelF = SharedStyles.createFilterField(20);
        Object[] msg = {"Plate:", plate, "Brand:", brand, "Model:", modelF};
        if (JOptionPane.showConfirmDialog(this, msg, "Add Vehicle", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            vehicleService.addVehicle(currentUser.getUserId(), plate.getText(), brand.getText(), modelF.getText());
            refreshSelectedPanel("Manage Vehicles");
        }
    }

    private void showReviewDialog(String aptId) {
        JComboBox<String> rating = SharedStyles.createFilterCombo(new String[]{"1", "2", "3", "4", "5"});
        JTextArea comment = new JTextArea(5, 20);
        Object[] msg = {"Rating:", rating, "Comment:", new JScrollPane(comment)};
        if (JOptionPane.showConfirmDialog(this, msg, "Submit Review", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            int ratingValue = Integer.parseInt((String) rating.getSelectedItem());
            reviewService.submitReview(currentUser.getUserId(), aptId, ratingValue, comment.getText());
            refreshSelectedPanel("Reviews");
        }
    }
}
