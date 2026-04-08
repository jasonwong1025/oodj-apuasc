package ui;

import abstracts.AbstractUser;
import model.appointment.Appointment;
import model.review.Review;
import model.vehicle.Vehicle;
import model.service.Service;
import service_layer.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerDashboard extends JFrame {
    private final AbstractUser user;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;

    private final VehicleService vehicleService;
    private final AppointmentService appointmentService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final ServiceService serviceLookup; // To get service names/prices

    public CustomerDashboard(AbstractUser user) {
        this.user = user;
        this.vehicleService = new VehicleService();
        this.appointmentService = new AppointmentService();
        this.paymentService = new PaymentService();
        this.reviewService = new ReviewService();
        this.serviceLookup = new ServiceService();

        setTitle("APU Automotive Service Centre - Customer Portal");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(CustomerPortalStyles.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(250, 0));

        JLabel logoLabel = new JLabel("APU-ASC", SwingConstants.CENTER);
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        logoLabel.setBorder(new javax.swing.border.EmptyBorder(30, 0, 30, 0));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logoLabel);

        sidebar.add(CustomerPortalStyles.createSidebarButton("Dashboard", () -> switchPanel("Dashboard")));
        sidebar.add(CustomerPortalStyles.createSidebarButton("Manage Vehicles", () -> switchPanel("Vehicles")));
        sidebar.add(CustomerPortalStyles.createSidebarButton("Book Appointment", () -> switchPanel("Book")));
        sidebar.add(CustomerPortalStyles.createSidebarButton("My Appointments", () -> switchPanel("Appointments")));
        sidebar.add(CustomerPortalStyles.createSidebarButton("Service History", () -> switchPanel("History")));
        sidebar.add(CustomerPortalStyles.createSidebarButton("Reviews", () -> switchPanel("Reviews")));

        sidebar.add(Box.createVerticalGlue());

        sidebar.add(CustomerPortalStyles.createSidebarButton("Logout", () -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        }));

        add(sidebar, BorderLayout.WEST);

        // Content Area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CustomerPortalStyles.MAIN_BG);

        contentPanel.add(createDashboardPanel(), "Dashboard");
        contentPanel.add(createVehiclesPanel(), "Vehicles");
        contentPanel.add(createBookingPanel(), "Book");
        contentPanel.add(createAppointmentsPanel(), "Appointments");
        contentPanel.add(createHistoryPanel(), "History");
        contentPanel.add(createReviewsPanel(), "Reviews");

        add(contentPanel, BorderLayout.CENTER);
        
        switchPanel("Dashboard");
    }

    private void switchPanel(String name) {
        // Refresh data before switching if needed
        if (name.equals("Vehicles")) contentPanel.add(createVehiclesPanel(), "Vehicles");
        if (name.equals("Appointments")) contentPanel.add(createAppointmentsPanel(), "Appointments");
        if (name.equals("History")) contentPanel.add(createHistoryPanel(), "History");
        if (name.equals("Reviews")) contentPanel.add(createReviewsPanel(), "Reviews");
        if (name.equals("Book")) contentPanel.add(createBookingPanel(), "Book");
        if (name.equals("Dashboard")) contentPanel.add(createDashboardPanel(), "Dashboard");
        
        cardLayout.show(contentPanel, name);
    }

    private JPanel createDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CustomerPortalStyles.MAIN_BG);
        p.setBorder(new javax.swing.border.EmptyBorder(40, 40, 40, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel welcome = new JLabel("Welcome back, " + user.getFullName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 28));
        header.add(welcome, BorderLayout.WEST);
        p.add(header, BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        statsGrid.setOpaque(false);
        statsGrid.setBorder(new javax.swing.border.EmptyBorder(30, 0, 30, 0));

        List<Vehicle> vehicles = vehicleService.getCustomerVehicles(user.getUserId());
        List<Appointment> appointments = appointmentService.getCustomerAppointments(user.getUserId());
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
        CustomerPortalStyles.styleCard(card);
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 16));
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 36));
        v.setForeground(CustomerPortalStyles.ACCENT_COLOR);
        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private JPanel createVehiclesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CustomerPortalStyles.MAIN_BG);
        p.setBorder(new javax.swing.border.EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Manage Your Vehicles");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        p.add(title, BorderLayout.NORTH);

        String[] cols = {"ID", "Plate Number", "Brand", "Model"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        List<Vehicle> list = vehicleService.getCustomerVehicles(user.getUserId());
        for (Vehicle v : list) model.addRow(new Object[]{v.getVehicleId(), v.getPlateNumber(), v.getBrand(), v.getModel()});

        JTable table = new JTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton addBtn = CustomerPortalStyles.createPrimaryButton("Add New Vehicle");
        addBtn.addActionListener(e -> showAddVehicleDialog());
        actions.add(addBtn);
        p.add(actions, BorderLayout.SOUTH);

        return p;
    }

    private void showAddVehicleDialog() {
        JTextField plate = new JTextField();
        JTextField brand = new JTextField();
        JTextField model = new JTextField();
        Object[] message = {
            "Plate Number:", plate,
            "Brand:", brand,
            "Model:", model
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Vehicle", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            vehicleService.addVehicle(user.getUserId(), plate.getText(), brand.getText(), model.getText());
            switchPanel("Vehicles");
        }
    }

    private JPanel createBookingPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CustomerPortalStyles.MAIN_BG);
        CustomerPortalStyles.styleCard(p);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Vehicle> vehicles = vehicleService.getCustomerVehicles(user.getUserId());
        if (vehicles.isEmpty()) {
            p.add(new JLabel("Please register a vehicle first!"), gbc);
            return p;
        }

        JComboBox<String> vehicleCombo = new JComboBox<>();
        for (Vehicle v : vehicles) vehicleCombo.addItem(v.getVehicleId() + " - " + v.getPlateNumber());

        List<Service> services = serviceLookup.listAll();
        JComboBox<String> serviceCombo = new JComboBox<>();
        for (Service s : services) serviceCombo.addItem(s.getServiceId() + " - " + s.getServiceName());

        JTextField dateField = new JTextField("YYYY-MM-DD");
        JTextField timeField = new JTextField("HH:MM");

        gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel("Select Vehicle:"), gbc);
        gbc.gridx = 1; p.add(vehicleCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1; p.add(new JLabel("Select Service:"), gbc);
        gbc.gridx = 1; p.add(serviceCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 2; p.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1; p.add(dateField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; p.add(new JLabel("Time:"), gbc);
        gbc.gridx = 1; p.add(timeField, gbc);

        JButton bookBtn = CustomerPortalStyles.createPrimaryButton("Book Appointment");
        bookBtn.addActionListener(e -> {
            String vId = vehicleCombo.getSelectedItem().toString().split(" - ")[0];
            String sId = serviceCombo.getSelectedItem().toString().split(" - ")[0];
            String res = appointmentService.bookAppointment(user.getUserId(), vId, sId, dateField.getText(), timeField.getText());
            JOptionPane.showMessageDialog(this, res);
            switchPanel("Appointments");
        });
        gbc.gridx = 1; gbc.gridy = 4; p.add(bookBtn, gbc);

        return p;
    }

    private JPanel createAppointmentsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CustomerPortalStyles.MAIN_BG);
        p.setBorder(new javax.swing.border.EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Pending Appointments");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        p.add(title, BorderLayout.NORTH);

        String[] cols = {"ID", "Vehicle", "Service", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        List<Appointment> list = appointmentService.getCustomerAppointments(user.getUserId());
        for (Appointment a : list) {
            if (a.getStatus().equals("PENDING")) {
                model.addRow(new Object[]{a.getAppointmentId(), a.getVehicleId(), a.getServiceId(), a.getDate(), a.getTime(), a.getStatus()});
            }
        }

        JTable table = new JTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton cancelBtn = CustomerPortalStyles.createPrimaryButton("Cancel Selected Appointment");
        cancelBtn.setBackground(new Color(192, 57, 43));
        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = table.getValueAt(row, 0).toString();
                appointmentService.cancelAppointment(id);
                switchPanel("Appointments");
            }
        });
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.setOpaque(false);
        bp.add(cancelBtn);
        p.add(bp, BorderLayout.SOUTH);

        return p;
    }

    private JPanel createHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CustomerPortalStyles.MAIN_BG);
        p.setBorder(new javax.swing.border.EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Service & Payment History");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        p.add(title, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Service", "Date", "Status", "Payment Status", "Tech Feedback"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        List<Appointment> list = appointmentService.getCustomerAppointments(user.getUserId());
        for (Appointment a : list) {
            if (!a.getStatus().equals("PENDING")) {
                boolean isPaid = paymentService.isPaid(a.getAppointmentId());
                model.addRow(new Object[]{
                    a.getAppointmentId(), 
                    a.getServiceId(), 
                    a.getDate(), 
                    a.getStatus(), 
                    isPaid ? "PAID" : "UNPAID",
                    a.getTechnicianFeedback()
                });
            }
        }

        JTable table = new JTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel createReviewsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CustomerPortalStyles.MAIN_BG);
        p.setBorder(new javax.swing.border.EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Provide Feedback & Reviews");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        p.add(title, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Service", "Date", "Review Action"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        List<Appointment> list = appointmentService.getCustomerAppointments(user.getUserId());
        for (Appointment a : list) {
            if (a.getStatus().equals("COMPLETED")) {
                boolean isPaid = paymentService.isPaid(a.getAppointmentId());
                boolean reviewed = false;
                List<Review> reviews = reviewService.getCustomerReviews(user.getUserId());
                for (Review r : reviews) if (r.getAppointmentId().equals(a.getAppointmentId())) reviewed = true;

                String action = reviewed ? "Reviewed" : (isPaid ? "Review Now" : "Pay First");
                model.addRow(new Object[]{a.getAppointmentId(), a.getServiceId(), a.getDate(), action});
            }
        }

        JTable table = new JTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton reviewBtn = CustomerPortalStyles.createPrimaryButton("Open Review Dialog");
        reviewBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = table.getValueAt(row, 0).toString();
                String action = table.getValueAt(row, 3).toString();
                if (action.equals("Review Now")) {
                    showReviewDialog(id);
                } else {
                    JOptionPane.showMessageDialog(this, action);
                }
            }
        });
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.setOpaque(false);
        bp.add(reviewBtn);
        p.add(bp, BorderLayout.SOUTH);

        return p;
    }

    private void showReviewDialog(String appointmentId) {
        JComboBox<Integer> rating = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JTextArea comment = new JTextArea(5, 20);
        Object[] message = {
            "Rating (1-5):", rating,
            "Comment:", new JScrollPane(comment)
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Submit Review", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String res = reviewService.submitReview(user.getUserId(), appointmentId, (Integer)rating.getSelectedItem(), comment.getText());
            JOptionPane.showMessageDialog(this, res);
            switchPanel("Reviews");
        }
    }
}
