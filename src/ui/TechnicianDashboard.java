package ui;

import abstracts.AbstractUser;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.users.User;
import service_layer.UserService;

import javax.swing.table.DefaultTableModel;
import model.feedback.Review;
import model.appointment.Appointment;
import service_layer.ReviewService;
import service_layer.AppointmentService;
import service_layer.VehicleService;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TechnicianDashboard extends JFrame implements Refreshable {

    private AbstractUser currentUser;
    private UserService userService;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DefaultListModel<String> navModel;
    private JList<String> navList;

    private static final String[] NAV_ITEMS = {
            "Dashboard",
            "My Tasks",
            "Task History",
            "Provide Feedback",
            "Customer Reviews",
            "My Profile"
    };

    public TechnicianDashboard(AbstractUser user) {
        this.currentUser = user;
        this.userService = new UserService();

        setTitle("APU-ASC | Technician - " + currentUser.getFullName());
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

        String serviceType = currentUser.getTechnicianServiceType();
        String displayRole = "Technician";
        if (serviceType != null && !serviceType.trim().isEmpty() && !serviceType.equals("-")) {
            displayRole += " (" + serviceType + ")";
        }
        JLabel who = new JLabel(currentUser.getFullName() + "  |  " + displayRole);
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
        navList.setFixedCellHeight(46);
        navList.setBorder(new EmptyBorder(12, 0, 12, 0));
        navList.setBackground(SharedStyles.SIDEBAR_BG);
        navList.setForeground(SharedStyles.TEXT_ON_DARK);
        navList.setFont(new Font("SansSerif", Font.PLAIN, 14));
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
            case "My Tasks": panel = buildMyTasksPanel(); break;
            case "Task History": panel = buildTaskHistoryPanel(); break;
            case "Provide Feedback": panel = buildProvideFeedbackPanel(); break;
            case "Customer Reviews": panel = buildCustomerReviewsPanel(); break;
            case "My Profile": panel = buildMyProfilePanel(); break;
            default: panel = buildPlaceholderPanel(selected);
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
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top section contains Header and Stats cards
        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);

        // Welcome Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Welcome back, " + currentUser.getFullName() + "!");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(38, 38, 42));
        JLabel subtitle = new JLabel("Here's your task overview for today.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(110, 110, 115));
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        topContainer.add(headerPanel, BorderLayout.NORTH);

        // Compute Statistics
        service_layer.AppointmentService service = new service_layer.AppointmentService();
        java.util.List<model.appointment.Appointment> allAppointments = service.getAllAppointments();
        int total = 0, inProgress = 0, completed = 0, pendingConfirmed = 0;
        
        java.util.List<model.appointment.Appointment> myUpcoming = new java.util.ArrayList<>();

        for (model.appointment.Appointment a : allAppointments) {
            if (currentUser.getUserId().equals(a.getTechnicianId())) {
                total++;
                if ("IN PROGRESS".equalsIgnoreCase(a.getStatus())) {
                    inProgress++;
                    myUpcoming.add(a);
                }
                else if ("COMPLETED".equalsIgnoreCase(a.getStatus())) {
                    completed++;
                }
                else {
                    pendingConfirmed++;
                    myUpcoming.add(a);
                }
            }
        }

        // Stats Cards Panel
        JPanel statsPanel = new JPanel(new java.awt.GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        // Explicit height for stats cards to prevent them taking all the space
        statsPanel.setPreferredSize(new java.awt.Dimension(0, 100));

        statsPanel.add(createStatCard("Total Tasks", String.valueOf(total), new Color(0, 120, 215)));
        statsPanel.add(createStatCard("Pending/Confirmed", String.valueOf(pendingConfirmed), new Color(230, 126, 34)));
        statsPanel.add(createStatCard("In Progress", String.valueOf(inProgress), new Color(46, 160, 67)));
        statsPanel.add(createStatCard("Completed", String.valueOf(completed), new Color(38, 38, 42)));
        topContainer.add(statsPanel, BorderLayout.SOUTH);

        root.add(topContainer, BorderLayout.NORTH);

        // Center section: Upcoming Tasks table
        JPanel upcomingPanel = new JPanel(new BorderLayout(0, 10));
        upcomingPanel.setOpaque(false);

        JLabel upcomingLbl = new JLabel("Your Upcoming Tasks");
        upcomingLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        upcomingLbl.setForeground(new Color(38, 38, 42));
        upcomingPanel.add(upcomingLbl, BorderLayout.NORTH);

        String[] columns = {"ID", "Customer", "Vehicle", "Service", "Date", "Time", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        service_layer.ServiceService serviceSvc = new service_layer.ServiceService();
        service_layer.VehicleService vehicleSvc = new service_layer.VehicleService();
        for (model.appointment.Appointment a : myUpcoming) {
            String serviceDisplay = a.getServiceId();
            if (serviceDisplay != null && !serviceDisplay.trim().isEmpty()) {
                String[] parts = serviceDisplay.split(",");
                java.util.List<String> names = new java.util.ArrayList<>();
                for (String p : parts) {
                    model.service.Service svc = serviceSvc.findById(p.trim());
                    names.add(svc != null ? svc.getServiceName() : p.trim());
                }
                serviceDisplay = String.join(", ", names);
            }

            model.users.User cust = userService.findByUserId(a.getCustomerId());
            String customerDisplay = (cust != null) ? String.format("%s (%s)", cust.getFullName(), cust.getUserId()) : a.getCustomerId();

            model.vehicle.Vehicle v = vehicleSvc.findById(a.getVehicleId());
            String vehicleDisplay = (v != null) ? String.format("%s (%s %s)", v.getPlateNumber(), v.getBrand(), v.getModel()) : a.getVehicleId();

            tblModel.addRow(new Object[]{
                a.getAppointmentId(),
                customerDisplay,
                vehicleDisplay,
                serviceDisplay,
                a.getDate(),
                a.getTime(),
                a.getStatus()
            });
        }

        JTable table = new JTable(tblModel);
        SharedStyles.applyTableStyle(table);
        upcomingPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        root.add(upcomingPanel, BorderLayout.CENTER);

        return root;
    }

    private JPanel createStatCard(String label, String value, Color accentColor) {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        valLbl.setForeground(accentColor);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(new Color(110, 110, 115));

        card.add(valLbl, BorderLayout.CENTER);
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildMyTasksPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        service_layer.AppointmentService service = new service_layer.AppointmentService();
        java.util.List<model.appointment.Appointment> allAppointments = service.getAllAppointments();

        java.util.List<model.appointment.Appointment> myTasks = new java.util.ArrayList<>();
        for (model.appointment.Appointment a : allAppointments) {
            if (currentUser.getUserId().equals(a.getTechnicianId())) {
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

        service_layer.ServiceService serviceSvc = new service_layer.ServiceService();
        service_layer.VehicleService vehicleSvc = new service_layer.VehicleService();
        for (model.appointment.Appointment a : myTasks) {
            String serviceDisplay = a.getServiceId();
            if (serviceDisplay != null && !serviceDisplay.trim().isEmpty()) {
                String[] parts = serviceDisplay.split(",");
                java.util.List<String> names = new java.util.ArrayList<>();
                for (String p : parts) {
                    model.service.Service svc = serviceSvc.findById(p.trim());
                    names.add(svc != null ? svc.getServiceName() : p.trim());
                }
                serviceDisplay = String.join(", ", names);
            }

            model.users.User cust = userService.findByUserId(a.getCustomerId());
            String customerDisplay = (cust != null) ? String.format("%s (%s)", cust.getFullName(), cust.getUserId()) : a.getCustomerId();

            model.vehicle.Vehicle v = vehicleSvc.findById(a.getVehicleId());
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

            java.util.List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();
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

            java.util.List<String> optionsList = new java.util.ArrayList<>();
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
                for (model.appointment.Appointment a : myTasks) {
                    if (a.getAppointmentId().equals(id)) {
                        a.setStatus(newStatus);
                        new repository.AppointmentRepository().update(a);
                        JOptionPane.showMessageDialog(this, "Status successfully updated to " + newStatus);
                        refresh();
                        break;
                    }
                }
            }
        });

        root.add(new JScrollPane(table), BorderLayout.CENTER);

        return root;
    }

    private JPanel buildTaskHistoryPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        service_layer.AppointmentService service = new service_layer.AppointmentService();
        java.util.List<model.appointment.Appointment> allAppointments = service.getAllAppointments();

        java.util.List<model.appointment.Appointment> myTasks = new java.util.ArrayList<>();
        for (model.appointment.Appointment a : allAppointments) {
            if (currentUser.getUserId().equals(a.getTechnicianId())) {
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

        service_layer.ServiceService serviceSvc = new service_layer.ServiceService();
        service_layer.VehicleService vehicleSvc = new service_layer.VehicleService();
        for (model.appointment.Appointment a : myTasks) {
            String serviceDisplay = a.getServiceId();
            if (serviceDisplay != null && !serviceDisplay.trim().isEmpty()) {
                String[] parts = serviceDisplay.split(",");
                java.util.List<String> names = new java.util.ArrayList<>();
                for (String p : parts) {
                    model.service.Service svc = serviceSvc.findById(p.trim());
                    names.add(svc != null ? svc.getServiceName() : p.trim());
                }
                serviceDisplay = String.join(", ", names);
            }

            model.users.User cust = userService.findByUserId(a.getCustomerId());
            String customerDisplay = (cust != null) ? String.format("%s (%s)", cust.getFullName(), cust.getUserId()) : a.getCustomerId();

            model.vehicle.Vehicle v = vehicleSvc.findById(a.getVehicleId());
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

        // Apply initial filter
        filterAction.actionPerformed(null);

        root.add(new JScrollPane(table), BorderLayout.CENTER);

        return root;
    }

    private JPanel buildProvideFeedbackPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        service_layer.AppointmentService service = new service_layer.AppointmentService();
        java.util.List<model.appointment.Appointment> allAppointments = service.getAllAppointments();

        java.util.List<model.appointment.Appointment> myTasks = new java.util.ArrayList<>();
        for (model.appointment.Appointment a : allAppointments) {
            if (currentUser.getUserId().equals(a.getTechnicianId()) &&
                "COMPLETED".equalsIgnoreCase(a.getStatus())) {
                myTasks.add(a);
            }
        }

        String[] columns = {"ID", "Vehicle", "Service", "Appointment", "Status", "Feedback", "Feedback Date & Time"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        service_layer.ServiceService serviceSvc = new service_layer.ServiceService();
        service_layer.VehicleService vehicleSvc = new service_layer.VehicleService();
        repository.FeedbackRepository fbRepo = new repository.FeedbackRepository();

        for (model.appointment.Appointment a : myTasks) {
            // Service display
            String serviceDisplay = a.getServiceId();
            if (serviceDisplay != null && !serviceDisplay.trim().isEmpty()) {
                String[] parts = serviceDisplay.split(",");
                java.util.List<String> names = new java.util.ArrayList<>();
                for (String p : parts) {
                    model.service.Service svc = serviceSvc.findById(p.trim());
                    names.add(svc != null ? svc.getServiceName() : p.trim());
                }
                serviceDisplay = String.join(", ", names);
            }

            // Vehicle display: plate (brand model)
            model.vehicle.Vehicle v = vehicleSvc.findById(a.getVehicleId());
            String vehicleDisplay = (v != null) 
                ? String.format("%s (%s %s)", v.getPlateNumber(), v.getBrand(), v.getModel()) 
                : "Unknown Vehicle";

            // Feedback status and content
            model.feedback.Feedback fbObj = fbRepo.findByAppointmentId(a.getAppointmentId());
            boolean hasFeedback = (fbObj != null && fbObj.getDescription() != null && 
                                  !fbObj.getDescription().trim().isEmpty() && 
                                  !"NONE".equalsIgnoreCase(fbObj.getDescription()));
            
            String existingFb = hasFeedback ? fbObj.getDescription() : "-";
            String fbTime = hasFeedback ? fbObj.getDateTime() : "-";
            String status = hasFeedback ? "Submitted" : "Pending Feedback";

            model.addRow(new Object[]{
                    a.getAppointmentId(),
                    vehicleDisplay,
                    serviceDisplay,
                    a.getDate() + " " + a.getTime(),
                    status,
                    existingFb,
                    fbTime
            });
        }

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        
        // Setup Sorting and Filtering
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        // Button and Filter bar
        JButton writeFeedbackBtn = SharedStyles.createActionButton("Write Feedback", SharedStyles.BTN_BLUE);
        writeFeedbackBtn.setEnabled(false);

        JLabel filterLabel = new JLabel("Status Filter:");
        filterLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Pending Feedback", "Submitted"});
        statusFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusFilter.setPreferredSize(new Dimension(150, 32));

        statusFilter.addActionListener(e -> {
            String selected = (String) statusFilter.getSelectedItem();
            if ("All".equals(selected)) {
                sorter.setRowFilter(null);
            } else {
                // Filter by the 'Status' column (index 4)
                sorter.setRowFilter(RowFilter.regexFilter("^" + selected + "$", 4));
            }
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topBar.setOpaque(false);
        topBar.add(writeFeedbackBtn);
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(filterLabel);
        topBar.add(statusFilter);

        root.add(topBar, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        // Enable button only when a row is selected
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    String status = table.getValueAt(row, 4).toString();
                    writeFeedbackBtn.setEnabled("Pending Feedback".equalsIgnoreCase(status));
                } else {
                    writeFeedbackBtn.setEnabled(false);
                }
            }
        });

        // Open JDialog modal on button click
        writeFeedbackBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            
            // Double-check status before opening
            String status = table.getValueAt(row, 4).toString();
            if ("Submitted".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Feedback has already been submitted for this task.");
                return;
            }

            String id = table.getValueAt(row, 0).toString();
            String svcName = table.getValueAt(row, 2).toString();
            String existingFb = table.getValueAt(row, 5).toString();
            openFeedbackDialog(id, svcName, "-".equals(existingFb) ? "" : existingFb);
        });

        return root;
    }

    /**
     * Opens a JDialog modal for submitting/editing feedback for the given appointment.
     * Encapsulates all form logic within the dialog for clean OOP separation.
     */
    private void openFeedbackDialog(String apptId, String svcName, String existingFb) {
        JDialog dialog = new JDialog(this, "Write Feedback", true);
        dialog.setSize(500, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel titleLbl = new JLabel("Feedback Form", SwingConstants.LEFT);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setForeground(new Color(38, 38, 42));
        titleLbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        content.add(titleLbl, gbc);

        gbc.gridy++;
        JLabel idLabel = new JLabel("Appointment ID:");
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        idLabel.setForeground(new Color(100, 100, 100));
        content.add(idLabel, gbc);

        gbc.gridy++;
        JTextField idField = SharedStyles.createFilterField(20);
        idField.setText(apptId);
        idField.setEditable(false);
        idField.setBackground(new Color(245, 245, 247));
        content.add(idField, gbc);

        gbc.gridy++;
        JLabel svcLabel = new JLabel("Service Name:");
        svcLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        svcLabel.setForeground(new Color(100, 100, 100));
        content.add(svcLabel, gbc);

        gbc.gridy++;
        JTextField svcField = SharedStyles.createFilterField(20);
        svcField.setText(svcName);
        svcField.setEditable(false);
        svcField.setBackground(new Color(245, 245, 247));
        content.add(svcField, gbc);

        gbc.gridy++;
        JLabel fbLabel = new JLabel("Your Feedback:");
        fbLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        fbLabel.setForeground(new Color(100, 100, 100));
        content.add(fbLabel, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JTextArea fbArea = new JTextArea(6, 20);
        fbArea.setText(existingFb);
        fbArea.setLineWrap(true);
        fbArea.setWrapStyleWord(true);
        fbArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane fbScroll = new JScrollPane(fbArea);
        fbScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        content.add(fbScroll, gbc);

        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy++;
        gbc.insets = new java.awt.Insets(14, 4, 4, 4);
        JButton submitBtn = SharedStyles.createActionButton("Save Feedback", SharedStyles.BTN_BLUE);
        content.add(submitBtn, gbc);

        gbc.gridy++;
        gbc.insets = new java.awt.Insets(0, 4, 6, 4);
        JButton cancelBtn = SharedStyles.createActionButton("Cancel", new Color(150, 150, 150));
        content.add(cancelBtn, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());

        submitBtn.addActionListener(e -> {
            String fb = fbArea.getText().trim();
            if (fb.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Feedback content cannot be empty.");
                return;
            }
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            repository.FeedbackRepository fRepo = new repository.FeedbackRepository();
            model.feedback.Feedback fbObj = fRepo.findByAppointmentId(apptId);
            if (fbObj == null) {
                fbObj = new model.feedback.Feedback(fRepo.generateNextId(), apptId, fb, currentDateTime);
            } else {
                fbObj.setDescription(fb);
                fbObj.setDateTime(currentDateTime);
            }
            fRepo.addOrUpdate(fbObj);
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Feedback saved successfully!");
            refresh();
        });

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }


    private JPanel buildCustomerReviewsPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        String[] columns = {
                "Review ID",
                "Appointment ID",
                "Customer",
                "Rating",
                "Review",
                "Review Date & time"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        JTextField searchField = SharedStyles.createFilterField(20);

        JLabel ratingLbl = new JLabel("Rating:");
        ratingLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        JComboBox<String> ratingFilter = new JComboBox<>(new String[]{"All", "1", "2", "3", "4", "5"});
        ratingFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));

        topPanel.add(searchLbl);
        topPanel.add(searchField);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(ratingLbl);
        topPanel.add(ratingFilter);
        root.add(topPanel, BorderLayout.NORTH);

        java.awt.event.ActionListener filterAction = ev -> {
            String text = searchField.getText().trim();
            String rating = ratingFilter.getSelectedItem().toString();

            java.util.List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();
            
            if (!text.isEmpty()) {
                filters.add(RowFilter.regexFilter("(?i)" + text));
            }
            
            if (!"All".equals(rating)) {
                // Rating is in column index 3, format is "X / 5"
                filters.add(RowFilter.regexFilter("^" + rating + " / 5$", 3));
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
        ratingFilter.addActionListener(filterAction);

        ReviewService reviewService = new ReviewService();
        AppointmentService appointmentService = new AppointmentService();

        List<Review> reviews = reviewService.getAllReviews();
        List<Appointment> appointments = appointmentService.getAllAppointments();

        for (Review r : reviews) {
            for (Appointment a : appointments) {
                if (a.getAppointmentId().equals(r.getAppointmentId()) && 
                    currentUser.getUserId().equals(a.getTechnicianId())) {
                    
                    User customer = userService.findByUserId(a.getCustomerId());
                    String customerDisplay = (customer != null) 
                        ? customer.getFullName() + " (" + a.getCustomerId() + ")" 
                        : a.getCustomerId();

                    model.addRow(new Object[]{
                            r.getReviewId(),
                            r.getAppointmentId(),
                            customerDisplay,
                            r.getRating() + " / 5",
                            r.getDescription(),
                            r.getDate()
                    });
                    break;
                }
            }
        }

        root.add(new JScrollPane(table), BorderLayout.CENTER);
        return root;
    }


    private JPanel buildMyProfilePanel() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(SharedStyles.MAIN_BG);
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        User self = userService.findByUserId(currentUser.getUserId());
        int y = 0;
        JTextField nameF = SharedStyles.createFilterField(25); nameF.setText(self.getFullName());
        JTextField contactF = SharedStyles.createFilterField(25); contactF.setText(self.getContact());
        JPasswordField passF = new JPasswordField(25); passF.setBorder(nameF.getBorder());

        SharedStyles.addFormRow(card, gbc, y++, "Full Name:", nameF);
        SharedStyles.addFormRow(card, gbc, y++, "Contact:", contactF);
        SharedStyles.addFormRow(card, gbc, y++, "Password:", passF);

        JButton saveBtn = SharedStyles.createActionButton("Save Profile", SharedStyles.BTN_GREEN);
        gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        saveBtn.addActionListener(e -> {
            self.setFullName(nameF.getText());
            self.setContact(contactF.getText());
            String newPass = new String(passF.getPassword());
            if (newPass.length() > 0) self.setPassword(newPass);
            userService.updateUser(self, currentUser.getUserId());
            JOptionPane.showMessageDialog(this, "Profile updated!");
            refresh();
        });
        card.add(saveBtn, gbc);

        root.add(card);
        return root;
    }

    private JPanel buildPlaceholderPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SharedStyles.MAIN_BG);
        p.add(new JLabel(title + " Panel (Styling Integrated)"));
        return p;
    }
}
