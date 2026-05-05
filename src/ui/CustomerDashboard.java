package ui;

import abstracts.AbstractUser;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.appointment.Appointment;
import model.feedback.Review;
import model.service.Service;
import model.users.User;
import model.vehicle.Vehicle;
import service_layer.*;
import service_layer.AppointmentService.SlotType;
import utils.ValidationUtil;

public class CustomerDashboard extends JFrame implements Refreshable {

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
    private final UserService userService;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DefaultListModel<String> navModel;
    private JList<String> navList;
    private JLabel headerWho;

    public CustomerDashboard(AbstractUser user) {
        this.currentUser = user;
        this.vehicleService = new VehicleService();
        this.appointmentService = new AppointmentService();
        this.paymentService = new PaymentService();
        this.reviewService = new ReviewService();
        this.serviceLookup = new ServiceService();
        this.userService = new UserService();

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

        headerWho = new JLabel(currentUser.getFullName() + "  |  Customer");
        headerWho.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JButton logout = SharedStyles.createActionButton("Logout", SharedStyles.BTN_LOGOUT);
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        east.setOpaque(false);
        east.add(headerWho);
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

        // Pre-initialization will be handled by selection listener
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
            case "Manage Vehicles": panel = buildVehiclesPanel(); break;
            case "Book Appointment": panel = buildBookingPanel(); break;
            case "My Appointments": panel = buildAppointmentsPanel(); break;
            case "Service History": panel = buildHistoryPanel(); break;
            case "Reviews": panel = buildReviewsPanel(); break;
            case "My Profile": panel = buildMyProfilePanel(); break;
            default: panel = new JPanel();
        }

        // Standard logic to replace panel in CardLayout
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

        // Update header just in case name changed
        User current = userService.findByUserId(currentUser.getUserId());
        if (current != null) {
            headerWho.setText(current.getFullName() + "  |  Customer");
            setTitle("APU-ASC | Customer - " + current.getFullName());
        }
    }

    private JPanel buildDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 30));
        p.setBackground(SharedStyles.MAIN_BG);
        p.setBorder(new EmptyBorder(30, 40, 40, 40));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel welcome = new JLabel("Hello, " + currentUser.getFullName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 28));
        topRow.add(welcome, BorderLayout.WEST);
        p.add(topRow, BorderLayout.NORTH);

        // Stats Row
        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        statsGrid.setOpaque(false);
        List<Vehicle> vehicles = vehicleService.getCustomerVehicles(currentUser.getUserId());
        List<Appointment> appointments = appointmentService.getCustomerAppointments(currentUser.getUserId());
        long pending = appointments.stream().filter(a -> a.getStatus().equals("PENDING")).count();
        statsGrid.add(createStatCard("Registered Vehicles", String.valueOf(vehicles.size())));
        statsGrid.add(createStatCard("Active Appointments", String.valueOf(pending)));
        statsGrid.add(createStatCard("Total Services", String.valueOf(appointments.size())));

        // Content Row (Recent Activities)
        JPanel contentRow = new JPanel(new GridLayout(1, 1, 20, 0));
        contentRow.setOpaque(false);
        
        JPanel recentCard = SharedStyles.createCardPanel();
        recentCard.setLayout(new BorderLayout(0, 15));
        JLabel recentTitle = new JLabel("Upcoming Appointments");
        recentTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        recentCard.add(recentTitle, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Vehicle", "Service(s)", "Date & Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> upcoming = appointments.stream()
                .filter(a -> a.getStatus().equals("PENDING"))
            .collect(Collectors.toList());
        for (Appointment a : upcoming) {
            model.addRow(new Object[]{
                a.getAppointmentId(),
                resolveVehicleInfo(a.getVehicleId()),
                resolveServiceNames(a.getServiceId()),
                a.getDate() + " " + a.getTime(),
                a.getStatus()
            });
        }
        
        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        table.setGridColor(new Color(220, 220, 225));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = String.valueOf(tbl.getValueAt(row, 4));
                    if ("CONFIRMED".equalsIgnoreCase(status)) c.setBackground(new Color(235, 243, 255));
                    else if ("IN PROGRESS".equalsIgnoreCase(status)) c.setBackground(new Color(255, 253, 235));
                    else if ("PENDING".equalsIgnoreCase(status)) c.setBackground(new Color(255, 253, 235));
                    else if ("COMPLETED".equalsIgnoreCase(status)) c.setBackground(new Color(236, 253, 242));
                    else if ("CANCELLED".equalsIgnoreCase(status)) c.setBackground(new Color(255, 242, 242));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });
        recentCard.add(new JScrollPane(table), BorderLayout.CENTER);
        contentRow.add(recentCard);

        JPanel mainCenter = new JPanel(new BorderLayout(0, 20));
        mainCenter.setOpaque(false);
        mainCenter.add(statsGrid, BorderLayout.NORTH);
        mainCenter.add(contentRow, BorderLayout.CENTER);

        p.add(mainCenter, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout());
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 15));
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 36));
        v.setForeground(SharedStyles.NAV_ACTIVE_TOP);
        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildVehiclesPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        String[] cols = {"Vehicle ID", "Plate Number", "Brand", "Model"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Vehicle> list = vehicleService.getCustomerVehicles(currentUser.getUserId());
        for (Vehicle v : list) model.addRow(new Object[]{v.getVehicleId(), v.getPlateNumber(), v.getBrand(), v.getModel()});

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JButton addBtn = SharedStyles.createActionButton("Register New Vehicle", SharedStyles.BTN_GREEN);
        addBtn.addActionListener(e -> showAddVehicleDialog());
        actions.add(addBtn);

        JButton updateBtn = SharedStyles.createActionButton("Update Selected", SharedStyles.BTN_BLUE);
        updateBtn.addActionListener(e -> {
            Vehicle selected = getSelectedVehicle(table);
            if (selected == null) {
                SharedStyles.showSelectionError(this);
                return;
            }
            showEditVehicleDialog(selected);
        });
        actions.add(updateBtn);

        JButton deleteBtn = SharedStyles.createActionButton("Delete Selected", SharedStyles.BTN_RED);
        deleteBtn.addActionListener(e -> {
            Vehicle selected = getSelectedVehicle(table);
            if (selected == null) {
                SharedStyles.showSelectionError(this);
                return;
            }
            deleteVehicle(selected);
        });
        actions.add(deleteBtn);

        top.add(actions, BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Search Plate: "));
        JTextField searchField = SharedStyles.createFilterField(15);
        searchBar.add(searchField);
        top.add(searchBar, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);

        searchField.addCaretListener(e -> {
            String text = searchField.getText();
            if (text.trim().length() == 0) sorter.setRowFilter(null);
            else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
        });

        root.add(new JScrollPane(table), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildBookingPanel() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(SharedStyles.MAIN_BG);
        
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Vehicle> vehicles = vehicleService.getCustomerVehicles(currentUser.getUserId());
        if (vehicles.isEmpty()) {
            card.add(new JLabel("Please register a vehicle first!"), gbc);
            GridBagConstraints emptyGbc = new GridBagConstraints();
            emptyGbc.gridx = 0;
            emptyGbc.gridy = 0;
            emptyGbc.weightx = 1;
            emptyGbc.weighty = 1;
            emptyGbc.fill = GridBagConstraints.BOTH;
            center.add(card, emptyGbc);
            return center;
        }

        JComboBox<String> vehicleCombo = SharedStyles.createFilterCombo(
            vehicles.stream().map(v -> v.getVehicleId() + " - " + v.getPlateNumber()).toArray(String[]::new)
        );

        List<Service> allServices = serviceLookup.listAll();
        List<Service> normalServices = allServices.stream().filter(Service::isIncludedInNormalService).collect(Collectors.toList());
        List<Service> majorServices = allServices.stream().filter(s -> !s.isIncludedInNormalService()).collect(Collectors.toList());

        final int NORMAL_LIMIT = 3;
        final int MAJOR_LIMIT = 8;

        List<JCheckBox> normalChecks = new ArrayList<>();
        List<JCheckBox> majorChecks = new ArrayList<>();
        List<JCheckBox> allChecks = new ArrayList<>();

        for (Service s : normalServices) {
            JCheckBox cb = new JCheckBox(s.getServiceName() + " (RM " + String.format("%.2f", s.getPrice()) + ")");
            cb.setOpaque(false);
            cb.putClientProperty("service", s);
            normalChecks.add(cb);
            allChecks.add(cb);
        }
        for (Service s : majorServices) {
            JCheckBox cb = new JCheckBox(s.getServiceName() + " (RM " + String.format("%.2f", s.getPrice()) + ")");
            cb.setOpaque(false);
            cb.putClientProperty("service", s);
            majorChecks.add(cb);
            allChecks.add(cb);
        }

        JRadioButton normalCategoryBtn = new JRadioButton("Normal");
        JRadioButton majorCategoryBtn = new JRadioButton("Major");
        normalCategoryBtn.setSelected(true);
        normalCategoryBtn.setOpaque(false);
        majorCategoryBtn.setOpaque(false);
        ButtonGroup categoryGroup = new ButtonGroup();
        categoryGroup.add(normalCategoryBtn);
        categoryGroup.add(majorCategoryBtn);

        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        categoryPanel.setOpaque(false);
        categoryPanel.add(new JLabel("Category:"));
        categoryPanel.add(normalCategoryBtn);
        categoryPanel.add(majorCategoryBtn);

        JPanel serviceListPanel = new JPanel();
        serviceListPanel.setLayout(new BoxLayout(serviceListPanel, BoxLayout.Y_AXIS));
        serviceListPanel.setOpaque(false);

        JScrollPane serviceScroll = new JScrollPane(serviceListPanel);
        serviceScroll.setPreferredSize(new Dimension(320, 220));
        serviceScroll.setBorder(BorderFactory.createTitledBorder("Normal Services (max 3)"));
        serviceScroll.setOpaque(false);
        serviceScroll.getViewport().setOpaque(false);

        JLabel selectionStatusLabel = new JLabel("0 selected (max 3)");
        selectionStatusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        selectionStatusLabel.setForeground(Color.GRAY);

        JLabel totalStatusLabel = new JLabel("Total selected: 0/3");
        totalStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        totalStatusLabel.setForeground(SharedStyles.NAV_ACTIVE_TOP);

        LocalDate today = LocalDate.now();
        LocalDate defaultDate = today;
        if (java.time.LocalTime.now().isAfter(java.time.LocalTime.of(16, 30))) {
            defaultDate = today.plusDays(1);
        }
        final LocalDate[] selectedDate = {defaultDate};
        final String[] selectedTime = {null};
        final LocalDate[] displayedMonth = {defaultDate.withDayOfMonth(1)};

        JLabel monthLabel = new JLabel();
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        JButton prevMonth = SharedStyles.createActionButton("<", SharedStyles.BTN_BLUE);
        JButton nextMonth = SharedStyles.createActionButton(">", SharedStyles.BTN_BLUE);
        JPanel calendarHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        calendarHeader.setOpaque(false);
        calendarHeader.add(prevMonth);
        calendarHeader.add(monthLabel);
        calendarHeader.add(nextMonth);

        JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarGrid.setOpaque(false);

        JComboBox<SlotOption> slotCombo = new JComboBox<>();
        slotCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SlotOption opt) {
                    l.setText(opt.label);
                    if (!opt.available && !isSelected) {
                        l.setForeground(Color.GRAY);
                    }
                }
                return l;
            }
        });

        Runnable refreshSlots = () -> {
            slotCombo.removeAllItems();
            String dateValue = selectedDate[0].format(AppointmentService.DATE_FORMATTER);
            SlotType requestedType = majorCategoryBtn.isSelected() ? SlotType.MAJOR : SlotType.NORMAL;
            for (String slotTime : AppointmentService.getAllowedSlotTimes()) {
                AppointmentService.SlotCapacity cap = appointmentService.getSlotCapacity(dateValue, slotTime);
                boolean available = appointmentService.isSlotAvailable(dateValue, slotTime, requestedType);
                
                // Exclude past timeslots
                LocalDateTime slotDateTime = LocalDateTime.of(selectedDate[0], LocalTime.parse(slotTime, AppointmentService.TIME_FORMATTER));
                if (slotDateTime.isBefore(LocalDateTime.now())) {
                    available = false;
                }

                int majorLimit = appointmentService.getCapacityLimitForSlotType(SlotType.MAJOR);
                int normalLimit = appointmentService.getCapacityLimitForSlotType(SlotType.NORMAL);
                int totalLimit = appointmentService.getTotalCapacityLimit();
                String label = String.format("%s  [Major: %d/%d | Normal: %d/%d | Total: %d/%d]%s",
                        slotTime,
                        cap.getMajorCount(), majorLimit,
                        cap.getNormalCount(), normalLimit,
                        cap.getTotalCount(), totalLimit,
                        available ? "" : " (FULL)");
                if (available) {
                    slotCombo.addItem(new SlotOption(slotTime, label, available));
                }
            }
            if (slotCombo.getItemCount() == 0) {
                slotCombo.addItem(new SlotOption(null, "No slots available", false));
            }
        };

        java.util.function.Supplier<List<JCheckBox>> activeChecks = () ->
                majorCategoryBtn.isSelected() ? allChecks : normalChecks;
        java.util.function.Supplier<Integer> maxSelection = () ->
                majorCategoryBtn.isSelected() ? MAJOR_LIMIT : NORMAL_LIMIT;

        Runnable refreshServiceList = () -> {
            if (normalCategoryBtn.isSelected()) {
                for (JCheckBox cb : majorChecks) {
                    if (cb.isSelected()) cb.setSelected(false);
                }
                serviceScroll.setBorder(BorderFactory.createTitledBorder("Normal Services"));
            } else {
                serviceScroll.setBorder(BorderFactory.createTitledBorder("All Services"));
            }
            serviceListPanel.removeAll();
            for (JCheckBox cb : activeChecks.get()) {
                serviceListPanel.add(cb);
            }
            int total = (int) activeChecks.get().stream().filter(JCheckBox::isSelected).count();
            selectionStatusLabel.setText(total + " selected (max " + maxSelection.get() + ")");
            totalStatusLabel.setText("Total selected: " + total + "/" + maxSelection.get());
            serviceListPanel.revalidate();
            serviceListPanel.repaint();
        };

        final Runnable[] updateSummaryRef = new Runnable[1];
        final Runnable[] updateCalendarRef = new Runnable[1];

        updateCalendarRef[0] = () -> {
            calendarGrid.removeAll();
            YearMonth ym = YearMonth.of(displayedMonth[0].getYear(), displayedMonth[0].getMonth());
            monthLabel.setText(ym.getMonth() + " " + ym.getYear());

            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (String d : days) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                calendarGrid.add(lbl);
            }

            LocalDate firstDay = displayedMonth[0];
            int startOffset = firstDay.getDayOfWeek().getValue();
            for (int i = 1; i < startOffset; i++) {
                calendarGrid.add(new JLabel(""));
            }

            int maxDay = ym.lengthOfMonth();
            for (int day = 1; day <= maxDay; day++) {
                LocalDate date = LocalDate.of(ym.getYear(), ym.getMonth(), day);
                JButton btn = new JButton(String.valueOf(day));
                btn.setMargin(new Insets(2, 2, 2, 2));
                btn.setFocusPainted(false);
                if (date.equals(selectedDate[0])) {
                    btn.setBackground(SharedStyles.NAV_ACTIVE_TOP);
                    btn.setForeground(Color.WHITE);
                }
                btn.addActionListener(e -> {
                    selectedDate[0] = date;
                    updateCalendarRef[0].run();
                    refreshSlots.run();
                    if (updateSummaryRef[0] != null) {
                        updateSummaryRef[0].run();
                    }
                });
                calendarGrid.add(btn);
            }

            calendarGrid.revalidate();
            calendarGrid.repaint();
        };

        prevMonth.addActionListener(e -> {
            displayedMonth[0] = displayedMonth[0].minusMonths(1);
            updateCalendarRef[0].run();
        });
        nextMonth.addActionListener(e -> {
            displayedMonth[0] = displayedMonth[0].plusMonths(1);
            updateCalendarRef[0].run();
        });

        JPanel schedulePanel = new JPanel(new BorderLayout(0, 10));
        schedulePanel.setOpaque(false);

        JPanel calendarPanel = new JPanel(new BorderLayout(0, 2));
        calendarPanel.setOpaque(false);
        calendarPanel.add(calendarHeader, BorderLayout.NORTH);
        calendarPanel.add(calendarGrid, BorderLayout.CENTER);

        JPanel slotRow = new JPanel(new BorderLayout(10, 0));
        slotRow.setOpaque(false);
        slotRow.setBorder(new EmptyBorder(6, 0, 0, 0));
        slotRow.add(new JLabel("Time Slot:"), BorderLayout.WEST);
        slotRow.add(slotCombo, BorderLayout.CENTER);

        schedulePanel.add(calendarPanel, BorderLayout.CENTER);
        schedulePanel.add(slotRow, BorderLayout.SOUTH);

        JPanel serviceSelectionPanel = new JPanel(new BorderLayout(0, 6));
        serviceSelectionPanel.setOpaque(false);
        serviceSelectionPanel.add(categoryPanel, BorderLayout.NORTH);
        serviceSelectionPanel.add(serviceScroll, BorderLayout.CENTER);
        serviceSelectionPanel.add(selectionStatusLabel, BorderLayout.SOUTH);

        JTextArea summaryArea = new JTextArea(16, 28);
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryArea.setBackground(Color.WHITE);
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Summary"));

        JButton checkoutBtn = SharedStyles.createActionButton("Checkout", SharedStyles.BTN_GREEN);

        Runnable updateSummary = () -> {
            StringBuilder sb = new StringBuilder();
            boolean isMajorCategory = majorCategoryBtn.isSelected();
            List<Service> selectedServices = activeChecks.get().stream()
                    .filter(JCheckBox::isSelected)
                    .map(cb -> (Service) cb.getClientProperty("service"))
                    .collect(Collectors.toList());

            int totalCount = selectedServices.size();
            double totalPrice = 0.0;
            for (Service s : selectedServices) totalPrice += s.getPrice();

            sb.append("Category: ").append(isMajorCategory ? "Major" : "Normal").append("\n");
            sb.append("Selected services (").append(totalCount).append(")\n");
            for (Service s : selectedServices) {
                sb.append("- ").append(s.getServiceName()).append(" (RM ").append(String.format("%.2f", s.getPrice())).append(")\n");
            }
            sb.append("\nTotal selected: ").append(totalCount).append("/")
                    .append(maxSelection.get()).append("\n");
            sb.append("Total: RM ").append(String.format("%.2f", totalPrice)).append("\n");
                String scheduleText = selectedTime[0] == null
                    ? "Not selected"
                    : selectedDate[0].format(AppointmentService.DATE_FORMATTER) + " " + selectedTime[0];
                sb.append("Schedule: ").append(scheduleText).append("\n");
            summaryArea.setText(sb.toString());

            totalStatusLabel.setText("Total selected: " + totalCount + "/" + maxSelection.get());
        };
        updateSummaryRef[0] = updateSummary;

        for (JCheckBox cb : allChecks) {
            cb.addItemListener(ev -> {
                int total = (int) activeChecks.get().stream().filter(JCheckBox::isSelected).count();
                if (total > maxSelection.get()) {
                    cb.setSelected(false);
                    Toolkit.getDefaultToolkit().beep();
                    total = (int) activeChecks.get().stream().filter(JCheckBox::isSelected).count();
                }
                selectionStatusLabel.setText(total + " selected (max " + maxSelection.get() + ")");
                totalStatusLabel.setText("Total selected: " + total + "/" + maxSelection.get());
                refreshSlots.run();
                updateSummaryRef[0].run();
            });
        }

        normalCategoryBtn.addActionListener(e -> {
            refreshServiceList.run();
            refreshSlots.run();
            updateSummaryRef[0].run();
        });
        majorCategoryBtn.addActionListener(e -> {
            refreshServiceList.run();
            refreshSlots.run();
            updateSummaryRef[0].run();
        });

        slotCombo.addActionListener(e -> {
            SlotOption selected = (SlotOption) slotCombo.getSelectedItem();
            if (selected == null) return;
            if (!selected.available) {
                Toolkit.getDefaultToolkit().beep();
                selectedTime[0] = null;
                updateSummaryRef[0].run();
                return;
            }
            selectedTime[0] = selected.time;
            updateSummaryRef[0].run();
        });

        updateCalendarRef[0].run();
        refreshServiceList.run();
        refreshSlots.run();
        updateSummaryRef[0].run();
        
        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setOpaque(false);
        JPanel leftContent = new JPanel();
        leftContent.setOpaque(false);
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));
        leftContent.add(serviceSelectionPanel);
        leftContent.add(Box.createVerticalStrut(8));
        JSeparator serviceCalendarSeparator = new JSeparator(SwingConstants.HORIZONTAL);
        serviceCalendarSeparator.setForeground(new Color(220, 220, 220));
        serviceCalendarSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        leftContent.add(serviceCalendarSeparator);
        leftContent.add(Box.createVerticalStrut(8));
        leftContent.add(schedulePanel);
        leftPanel.add(leftContent, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 12));
        rightPanel.setOpaque(false);
        rightPanel.add(summaryScroll, BorderLayout.CENTER);
        rightPanel.add(checkoutBtn, BorderLayout.SOUTH);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(leftPanel);
        contentPanel.add(rightPanel);

        int y = 0;
        SharedStyles.addFormRow(card, gbc, y++, "Select Vehicle:", vehicleCombo);

        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        card.add(contentPanel, gbc);

        gbc.gridwidth = 1; // reset

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.weightx = 1;
        cardGbc.weighty = 1;
        cardGbc.fill = GridBagConstraints.BOTH;
        center.add(card, cardGbc);

        checkoutBtn.addActionListener(e -> {
            if (selectedTime[0] == null) {
                SharedStyles.showWarning(this, "Please select a date and time.");
                return;
            }

            boolean isMajorCategory = majorCategoryBtn.isSelected();
            List<Service> selected = activeChecks.get().stream()
                    .filter(JCheckBox::isSelected)
                    .map(cb -> (Service) cb.getClientProperty("service"))
                    .collect(Collectors.toList());
            int totalCount = selected.size();

            if (totalCount < 1) {
                SharedStyles.showWarning(this, "Please select at least 1 service.");
                return;
            }
            if (totalCount > maxSelection.get()) {
                SharedStyles.showWarning(this, "You can select up to " + maxSelection.get() + " services only.");
                return;
            }

            double total = selected.stream().mapToDouble(Service::getPrice).sum();
            String dateValue = selectedDate[0].format(AppointmentService.DATE_FORMATTER);
            String timeValue = selectedTime[0];
            SlotType requestedType = isMajorCategory ? SlotType.MAJOR : SlotType.NORMAL;
            String scheduleError = appointmentService.validateSchedule(dateValue, timeValue, requestedType);
            if (scheduleError != null) {
                SharedStyles.showWarning(this, scheduleError);
                return;
            }
            StringBuilder summary = new StringBuilder("<html><body style='width: 300px;'>");
            summary.append("<h2>Booking Summary</h2>");
            summary.append("<hr>");
            summary.append("<b>Category</b>: ")
                    .append(isMajorCategory ? "Major" : "Normal")
                    .append("<br><br>");
            summary.append("<b>Selected Services</b>:<br>");
            for (Service s : selected) {
                summary.append("• ").append(s.getServiceName()).append(": RM ")
                        .append(String.format("%.2f", s.getPrice())).append("<br>");
            }
            summary.append("<hr>");
            summary.append("<h3 style='color: #2e7d32;'>Total Amount: RM ").append(String.format("%.2f", total)).append("</h3>");
            summary.append("<br>Proceed with this booking?</body></html>");

            if (SharedStyles.showConfirm(this, summary.toString())) {
                String vId = vehicleCombo.getSelectedItem().toString().split(" - ")[0];
                List<String> sIds = selected.stream().map(Service::getServiceId).collect(Collectors.toList());
                String res = appointmentService.bookAppointment(currentUser.getUserId(), vId, sIds, dateValue, timeValue);
                SharedStyles.showMessage(this, res);
                if (res.startsWith("Success")) {
                    navList.setSelectedIndex(3);
                }
            }
        });

        return center;
    }

    private JPanel buildAppointmentsPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("My Appointments");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        JButton cancelBtn = SharedStyles.createActionButton("Cancel Appointment", SharedStyles.BTN_RED);
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRight.setOpaque(false);
        topRight.add(cancelBtn);
        top.add(title, BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Vehicle", "Service Name(s)", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> list = appointmentService.getCustomerAppointments(currentUser.getUserId());

        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        table.setGridColor(new Color(220, 220, 225));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = String.valueOf(tbl.getValueAt(row, 5));
                    if ("PENDING".equalsIgnoreCase(status)) c.setBackground(new Color(255, 253, 235));
                    else if ("CONFIRMED".equalsIgnoreCase(status)) c.setBackground(new Color(235, 243, 255));
                    else if ("IN PROGRESS".equalsIgnoreCase(status)) c.setBackground(new Color(255, 253, 235));
                    else if ("COMPLETED".equalsIgnoreCase(status)) c.setBackground(new Color(236, 253, 242));
                    else if ("CANCELLED".equalsIgnoreCase(status)) c.setBackground(new Color(255, 242, 242));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel("Search:"));
        JTextField searchField = SharedStyles.createFilterField(16);
        filterRow.add(searchField);
        filterRow.add(new JLabel("Category:"));
        JComboBox<String> categoryFilter = SharedStyles.createFilterCombo(new String[]{"ALL", "NORMAL", "MAJOR"});
        filterRow.add(categoryFilter);

        Runnable applyFilter = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            String category = String.valueOf(categoryFilter.getSelectedItem());
            model.setRowCount(0);
            for (Appointment a : list) {
                if (!"PENDING".equalsIgnoreCase(a.getStatus())) continue;
                String appointmentCategory = getAppointmentCategory(a);
                if (!"ALL".equals(category) && !category.equalsIgnoreCase(appointmentCategory)) continue;
                String rowText = (a.getAppointmentId()
                        + resolveVehicleInfo(a.getVehicleId())
                        + resolveServiceNames(a.getServiceId())
                        + a.getDate()
                        + a.getTime()
                        + a.getStatus()).toLowerCase();
                if (!keyword.isEmpty() && !rowText.contains(keyword)) continue;
                model.addRow(new Object[]{
                    a.getAppointmentId(),
                    resolveVehicleInfo(a.getVehicleId()),
                    resolveServiceNames(a.getServiceId()),
                    a.getDate(),
                    a.getTime(),
                    a.getStatus()
                });
            }
        };
        javax.swing.event.DocumentListener autoFilter = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
        };
        searchField.getDocument().addDocumentListener(autoFilter);
        categoryFilter.addActionListener(e -> applyFilter.run());

        applyFilter.run();

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);
        body.add(filterRow, BorderLayout.NORTH);
        body.add(tableScroll, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);

        root.add(card, BorderLayout.CENTER);

        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                SharedStyles.showSelectionError(this);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Cancel this appointment?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                appointmentService.cancelAppointment(table.getValueAt(row, 0).toString());
                refresh();
            }
        });
        return root;
    }

    private JPanel buildHistoryPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("Service History");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 4, 0));
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Vehicle", "Service Name(s)", "Date", "Status", "Payment", "Tech Feedback"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> list = appointmentService.getCustomerAppointments(currentUser.getUserId());
        repository.FeedbackRepository fbRepo = new repository.FeedbackRepository();

        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        table.setGridColor(new Color(220, 220, 225));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = String.valueOf(tbl.getValueAt(row, 4));
                    if ("CONFIRMED".equalsIgnoreCase(status)) c.setBackground(new Color(235, 243, 255));
                    else if ("IN PROGRESS".equalsIgnoreCase(status)) c.setBackground(new Color(255, 253, 235));
                    else if ("COMPLETED".equalsIgnoreCase(status)) c.setBackground(new Color(236, 253, 242));
                    else if ("CANCELLED".equalsIgnoreCase(status)) c.setBackground(new Color(255, 242, 242));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel("Search:"));
        JTextField searchField = SharedStyles.createFilterField(14);
        filterRow.add(searchField);
        filterRow.add(new JLabel("Status:"));
        JComboBox<String> statusFilter = SharedStyles.createFilterCombo(
                new String[]{"ALL", "CONFIRMED", "IN PROGRESS", "COMPLETED", "CANCELLED"});
        filterRow.add(statusFilter);
        filterRow.add(new JLabel("Payment:"));
        JComboBox<String> paymentFilter = SharedStyles.createFilterCombo(new String[]{"ALL", "PAID", "UNPAID"});
        filterRow.add(paymentFilter);
        final String[] fromDateValue = {""};
        final String[] toDateValue = {""};
        JButton rangeBtn = SharedStyles.createActionButton("Select Date Range", SharedStyles.BTN_BLUE);
        rangeBtn.setPreferredSize(new Dimension(110, 28));
        filterRow.add(new JLabel("Date:"));
        filterRow.add(rangeBtn);
        JCheckBox completedOnly = new JCheckBox("Completed only");
        completedOnly.setOpaque(false);
        filterRow.add(completedOnly);

        Runnable applyFilter = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            String status = String.valueOf(statusFilter.getSelectedItem());
            String payment = String.valueOf(paymentFilter.getSelectedItem());
            java.time.LocalDate fromDate = null;
            java.time.LocalDate toDate = null;
            try {
                if (!fromDateValue[0].isEmpty()) {
                    fromDate = java.time.LocalDate.parse(fromDateValue[0], AppointmentService.DATE_FORMATTER);
                }
            } catch (java.time.format.DateTimeParseException ignore) {
                fromDate = null;
            }
            try {
                if (!toDateValue[0].isEmpty()) {
                    toDate = java.time.LocalDate.parse(toDateValue[0], AppointmentService.DATE_FORMATTER);
                }
            } catch (java.time.format.DateTimeParseException ignore) {
                toDate = null;
            }
            model.setRowCount(0);
            for (Appointment a : list) {
                if ("PENDING".equalsIgnoreCase(a.getStatus())) continue;
                if (completedOnly.isSelected() && !"COMPLETED".equalsIgnoreCase(a.getStatus())) continue;
                if (!"ALL".equals(status) && !status.equalsIgnoreCase(a.getStatus())) continue;
                if (fromDate != null || toDate != null) {
                    try {
                        java.time.LocalDate apptDate = java.time.LocalDate.parse(a.getDate(), AppointmentService.DATE_FORMATTER);
                        if (fromDate != null && apptDate.isBefore(fromDate)) continue;
                        if (toDate != null && apptDate.isAfter(toDate)) continue;
                    } catch (java.time.format.DateTimeParseException ignore) {
                        continue;
                    }
                }
                boolean isPaid = paymentService.isPaid(a.getAppointmentId());
                String paymentValue = isPaid ? "PAID" : "UNPAID";
                if (!"ALL".equals(payment) && !payment.equalsIgnoreCase(paymentValue)) continue;
                model.feedback.Feedback fbObj = fbRepo.findByAppointmentId(a.getAppointmentId());
                String existingFb = (fbObj == null || fbObj.getDescription().trim().isEmpty() || "NONE".equalsIgnoreCase(fbObj.getDescription())) ? "-" : fbObj.getDescription();
                String rowText = (a.getAppointmentId()
                        + resolveVehicleInfo(a.getVehicleId())
                        + resolveServiceNames(a.getServiceId())
                        + a.getDate()
                        + a.getStatus()
                        + paymentValue
                        + existingFb).toLowerCase();
                if (!keyword.isEmpty() && !rowText.contains(keyword)) continue;
                model.addRow(new Object[]{
                    a.getAppointmentId(),
                    resolveVehicleInfo(a.getVehicleId()),
                    resolveServiceNames(a.getServiceId()),
                    a.getDate(),
                    a.getStatus(),
                    paymentValue,
                    existingFb
                });
            }
        };
        javax.swing.event.DocumentListener autoFilter = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
        };
        searchField.getDocument().addDocumentListener(autoFilter);
        statusFilter.addActionListener(e -> applyFilter.run());
        paymentFilter.addActionListener(e -> applyFilter.run());
        completedOnly.addActionListener(e -> applyFilter.run());

        rangeBtn.addActionListener(e -> {
            String[] picked = showDateRangePicker(this, fromDateValue[0], toDateValue[0]);
            if (picked == null) return;
            fromDateValue[0] = picked[0] == null ? "" : picked[0];
            toDateValue[0] = picked[1] == null ? "" : picked[1];
            applyFilter.run();
        });

        applyFilter.run();

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);
        body.add(filterRow, BorderLayout.NORTH);
        body.add(tableScroll, BorderLayout.CENTER);
        JScrollPane outerScroll = new JScrollPane(body, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outerScroll.setBorder(null);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(outerScroll, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildReviewsPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel hint = new JLabel("Select a Pending appointment to write a review.");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        hint.setForeground(Color.GRAY);
        JButton reviewBtn = SharedStyles.createActionButton("Write Review", SharedStyles.BTN_BLUE);
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRight.setOpaque(false);
        topRight.add(reviewBtn);
        top.add(hint, BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Vehicle", "Service Name(s)", "Date", "Status", "Rating", "Comment", "Review Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> list = appointmentService.getCustomerAppointments(currentUser.getUserId());
        List<Review> reviews = reviewService.getCustomerReviews(currentUser.getUserId());
        java.util.Map<String, Review> reviewByAppointment = new java.util.HashMap<>();
        for (Review r : reviews) {
            reviewByAppointment.put(r.getAppointmentId(), r);
        }

        for (Appointment a : list) {
            if (a.getStatus().equals("COMPLETED")) {
                Review review = reviewByAppointment.get(a.getAppointmentId());
                boolean reviewed = review != null;
                boolean paid = paymentService.isPaid(a.getAppointmentId());
                String status = reviewed ? "Reviewed" : (paid ? "Pending" : "Payment Pending");
                String rating = reviewed ? String.valueOf(review.getRating()) : "-";
                String comment = reviewed
                        ? ((review.getDescription() == null || review.getDescription().trim().isEmpty()) ? "-" : review.getDescription())
                        : "-";
                String reviewDate = reviewed ? review.getDate() : "-";
                model.addRow(new Object[]{
                    a.getAppointmentId(),
                    resolveVehicleInfo(a.getVehicleId()),
                    resolveServiceNames(a.getServiceId()),
                    a.getDate(),
                    status,
                    rating,
                    comment,
                    reviewDate
                });
            }
        }

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Appointments & Reviews"));
        root.add(tableScroll, BorderLayout.CENTER);

        reviewBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                SharedStyles.showSelectionError(this);
                return;
            }
            String status = table.getValueAt(row, 4).toString();
            if (status.equals("Pending")) {
                showReviewDialog(table.getValueAt(row, 0).toString());
            } else {
                JOptionPane.showMessageDialog(this, "System: " + status);
            }
        });
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
        JTextField emailF = SharedStyles.createFilterField(25); emailF.setText(self.getEmail());
        JTextField contactF = SharedStyles.createFilterField(25); contactF.setText(self.getContact());
        JPasswordField passF = new JPasswordField(25); passF.setBorder(nameF.getBorder());

        SharedStyles.addFormRow(card, gbc, y++, "Full Name:", nameF);
        SharedStyles.addFormRow(card, gbc, y++, "Email:", emailF);
        SharedStyles.addFormRow(card, gbc, y++, "Contact:", contactF);
        SharedStyles.addFormRow(card, gbc, y++, "Password:", passF);

        JButton saveBtn = SharedStyles.createActionButton("Update Profile", SharedStyles.BTN_GREEN);
        gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        saveBtn.addActionListener(e -> {
            if (!ValidationUtil.isNotEmpty(nameF.getText()) || !ValidationUtil.isValidEmail(emailF.getText())) {
                JOptionPane.showMessageDialog(this, "Please enter valid details.");
                return;
            }
            self.setFullName(nameF.getText().trim());
            self.setEmail(emailF.getText().trim());
            self.setContact(contactF.getText().trim());
            String newPass = new String(passF.getPassword());
            if (newPass.length() > 0) {
                if (!ValidationUtil.isValidPassword(newPass)) {
                    JOptionPane.showMessageDialog(this, ValidationUtil.passwordRequirementsMessage());
                    return;
                }
                self.setPassword(newPass);
            }
            userService.updateUser(self, currentUser.getUserId());
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            refresh();
        });
        card.add(saveBtn, gbc);

        root.add(card);
        return root;
    }

    private void showAddVehicleDialog() {
        JTextField plate = SharedStyles.createFilterField(20);
        JTextField brand = SharedStyles.createFilterField(20);
        JTextField modelF = SharedStyles.createFilterField(20);
        Object[] msg = {"Plate:", plate, "Brand:", brand, "Model:", modelF};
        if (JOptionPane.showConfirmDialog(this, msg, "Register Vehicle", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            utils.Result<Vehicle> res = vehicleService.addVehicle(currentUser.getUserId(), plate.getText(), brand.getText(), modelF.getText());
            if (!res.isSuccess()) {
                SharedStyles.showValidationError(this, res.getError());
            } else {
                JOptionPane.showMessageDialog(this, "Vehicle registered successfully.");
                refresh();
            }
        }
    }

    private Vehicle getSelectedVehicle(JTable table) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        String vehicleId = table.getModel().getValueAt(modelRow, 0).toString();
        return vehicleService.findById(vehicleId);
    }

    private void showEditVehicleDialog(Vehicle vehicle) {
        JTextField plate = SharedStyles.createFilterField(20);
        JTextField brand = SharedStyles.createFilterField(20);
        JTextField modelF = SharedStyles.createFilterField(20);

        plate.setText(vehicle.getPlateNumber());
        brand.setText(vehicle.getBrand());
        modelF.setText(vehicle.getModel());

        Object[] msg = {"Plate:", plate, "Brand:", brand, "Model:", modelF};
        if (JOptionPane.showConfirmDialog(this, msg, "Update Vehicle", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            utils.Result<Vehicle> res = vehicleService.updateVehicle(
                    currentUser.getUserId(),
                    vehicle.getVehicleId(),
                    plate.getText(),
                    brand.getText(),
                    modelF.getText());
            if (!res.isSuccess()) {
                SharedStyles.showValidationError(this, res.getError());
            } else {
                SharedStyles.showMessage(this, "Vehicle updated successfully.");
                refresh();
            }
        }
    }

    private void deleteVehicle(Vehicle vehicle) {
        if (!SharedStyles.showConfirm(this, "Delete selected vehicle?")) return;
        String err = vehicleService.deleteVehicleForCustomer(currentUser.getUserId(), vehicle.getVehicleId());
        if (err != null) {
            SharedStyles.showValidationError(this, err);
            return;
        }
        SharedStyles.showMessage(this, "Vehicle deleted successfully.");
        refresh();
    }

    private void showReviewDialog(String aptId) {
        Appointment appointment = findAppointmentById(
                appointmentService.getCustomerAppointments(currentUser.getUserId()),
                aptId);

        String vehicle = appointment == null ? "N/A" : resolveVehicleInfo(appointment.getVehicleId());
        String services = appointment == null ? "N/A" : resolveServiceNames(appointment.getServiceId());
        String date = appointment == null ? "N/A" : appointment.getDate();

        JDialog dialog = new JDialog(this, "Submit Review", true);
        dialog.setLayout(new BorderLayout(0, 12));

        JLabel header = new JLabel("Share your experience");
        header.setFont(new Font("SansSerif", Font.BOLD, 16));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 0, 2));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("Appointment: " + aptId));
        infoPanel.add(new JLabel("Vehicle: " + vehicle));
        infoPanel.add(new JLabel("Services: " + services));
        infoPanel.add(new JLabel("Date: " + date));

        JPanel infoCard = new JPanel(new BorderLayout());
        infoCard.setOpaque(false);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        infoCard.add(infoPanel, BorderLayout.CENTER);

        int[] selectedRating = {0};
        JLabel ratingValueLabel = new JLabel("No rating");
        ratingValueLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        ratingValueLabel.setForeground(Color.GRAY);

        JPanel starPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        starPanel.setOpaque(false);
        List<JToggleButton> starButtons = new ArrayList<>();
        ButtonGroup starGroup = new ButtonGroup();
        for (int i = 1; i <= 5; i++) {
            JToggleButton btn = new JToggleButton("☆");
            btn.setFont(new Font("SansSerif", Font.PLAIN, 22));
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final int rating = i;
            btn.addActionListener(e -> {
                selectedRating[0] = rating;
                updateStarButtons(starButtons, rating);
                ratingValueLabel.setText(rating + "/5");
                ratingValueLabel.setForeground(Color.DARK_GRAY);
            });
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    updateStarButtons(starButtons, rating);
                    ratingValueLabel.setText(rating + "/5");
                    ratingValueLabel.setForeground(Color.DARK_GRAY);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    int current = selectedRating[0];
                    updateStarButtons(starButtons, current);
                    if (current == 0) {
                        ratingValueLabel.setText("No rating");
                        ratingValueLabel.setForeground(Color.GRAY);
                    } else {
                        ratingValueLabel.setText(current + "/5");
                        ratingValueLabel.setForeground(Color.DARK_GRAY);
                    }
                }
            });
            starGroup.add(btn);
            starButtons.add(btn);
            starPanel.add(btn);
        }

        JTextArea comment = new JTextArea(5, 28);
        comment.setLineWrap(true);
        comment.setWrapStyleWord(true);
        JScrollPane commentScroll = new JScrollPane(comment);
        JLabel wordCountLabel = new JLabel("0/50 words");
        wordCountLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        wordCountLabel.setForeground(Color.GRAY);

        Runnable updateWordCount = () -> {
            int words = countWords(comment.getText());
            wordCountLabel.setText(words + "/50 words");
            wordCountLabel.setForeground(words > 50 ? Color.RED : Color.GRAY);
        };
        comment.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateWordCount.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateWordCount.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateWordCount.run(); }
        });

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(header);
        form.add(Box.createVerticalStrut(8));
        form.add(infoCard);
        form.add(Box.createVerticalStrut(12));
        form.add(new JLabel("Rating *"));
        JPanel ratingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        ratingRow.setOpaque(false);
        ratingRow.add(starPanel);
        ratingRow.add(ratingValueLabel);
        form.add(ratingRow);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Comment (optional, max 50 words)"));
        form.add(commentScroll);
        form.add(Box.createVerticalStrut(4));
        form.add(wordCountLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton submit = SharedStyles.createActionButton("Submit", SharedStyles.BTN_GREEN);
        JButton cancel = SharedStyles.createActionButton("Cancel", SharedStyles.BTN_RED);
        actions.add(cancel);
        actions.add(submit);

        submit.addActionListener(e -> {
            int words = countWords(comment.getText());
            if (selectedRating[0] == 0) {
                SharedStyles.showWarning(dialog, "Please select a rating.");
                return;
            }
            if (words > 50) {
                SharedStyles.showWarning(dialog, "Comment must be 50 words or fewer.");
                return;
            }
            String result = reviewService.submitReview(currentUser.getUserId(), aptId, selectedRating[0], comment.getText().trim());
            if (result != null && result.startsWith("Success")) {
                SharedStyles.showMessage(dialog, result);
                dialog.dispose();
                refresh();
            } else {
                SharedStyles.showWarning(dialog, result == null ? "Unable to submit review." : result);
            }
        });
        cancel.addActionListener(e -> dialog.dispose());

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 12, 12, 12));
        body.add(form, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);

        dialog.add(body, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private Appointment findAppointmentById(List<Appointment> list, String appointmentId) {
        if (list == null || appointmentId == null) return null;
        for (Appointment a : list) {
            if (appointmentId.equals(a.getAppointmentId())) return a;
        }
        return null;
    }

    private String[] showDateRangePicker(Frame parent, String currentFrom, String currentTo) {
        DateRangePickerDialog dialog = new DateRangePickerDialog(parent, currentFrom, currentTo);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) return null;
        return new String[]{dialog.getStartDate(), dialog.getEndDate()};
    }

    private static final class DateRangePickerDialog extends JDialog {
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate displayedMonth;
        private JLabel monthLabel;
        private JPanel calendarGrid;
        private boolean confirmed = false;
        private JButton confirmBtn;

        private DateRangePickerDialog(Frame parent, String startValue, String endValue) {
            super(parent, "Select Date Range", true);
            setLayout(new BorderLayout());
            setResizable(false);

            LocalDate today = LocalDate.now();
            displayedMonth = today.withDayOfMonth(1);
            startDate = parseDateSafe(startValue);
            endDate = parseDateSafe(endValue);
            if (startDate == null && endDate != null) {
                startDate = endDate;
                endDate = null;
            }
            if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                LocalDate tmp = startDate;
                startDate = endDate;
                endDate = tmp;
            }

            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(Color.WHITE);
            p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JPanel calendarHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            calendarHeader.setOpaque(false);
            JButton prevMonth = SharedStyles.createActionButton("<", SharedStyles.BTN_BLUE);
            JButton nextMonth = SharedStyles.createActionButton(">", SharedStyles.BTN_BLUE);
            monthLabel = new JLabel();
            monthLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            calendarHeader.add(prevMonth);
            calendarHeader.add(monthLabel);
            calendarHeader.add(nextMonth);

            calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));
            calendarGrid.setOpaque(false);

            prevMonth.addActionListener(e -> {
                displayedMonth = displayedMonth.minusMonths(1);
                updateCalendar();
            });
            nextMonth.addActionListener(e -> {
                displayedMonth = displayedMonth.plusMonths(1);
                updateCalendar();
            });

            JLabel hint = new JLabel("Select a start date, then an end date.");
            hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
            hint.setForeground(Color.GRAY);

            int y = 0;
            gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 6; p.add(calendarHeader, gbc);
            y++;
            gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 6; p.add(calendarGrid, gbc);
            y++;
            gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 6; p.add(hint, gbc);
            gbc.gridwidth = 1;

            add(p, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.setBackground(Color.WHITE);
            confirmBtn = SharedStyles.createActionButton("Confirm", SharedStyles.BTN_GREEN);
            JButton cancel = SharedStyles.createActionButton("Cancel", SharedStyles.BTN_RED);

            confirmBtn.addActionListener(e -> {
                if (startDate == null || endDate == null) {
                    SharedStyles.showWarning(this, "Please select a date range.");
                    return;
                }
                confirmed = true;
                dispose();
            });
            cancel.addActionListener(e -> dispose());

            btnPanel.add(cancel);
            btnPanel.add(confirmBtn);
            add(btnPanel, BorderLayout.SOUTH);

            updateCalendar();
            pack();
            setLocationRelativeTo(parent);
        }

        private void updateCalendar() {
            calendarGrid.removeAll();

            YearMonth ym = YearMonth.of(displayedMonth.getYear(), displayedMonth.getMonth());
            monthLabel.setText(ym.getMonth() + " " + ym.getYear());

            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (String d : days) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                calendarGrid.add(lbl);
            }

            LocalDate firstDay = displayedMonth;
            int startOffset = firstDay.getDayOfWeek().getValue();
            for (int i = 1; i < startOffset; i++) {
                calendarGrid.add(new JLabel(""));
            }

            int maxDay = ym.lengthOfMonth();
            for (int day = 1; day <= maxDay; day++) {
                LocalDate date = LocalDate.of(ym.getYear(), ym.getMonth(), day);
                JButton btn = new JButton(String.valueOf(day));
                btn.setMargin(new Insets(2, 2, 2, 2));
                btn.setFocusPainted(false);
                styleRangeButton(btn, date);
                btn.addActionListener(e -> {
                    if (startDate == null || endDate != null) {
                        startDate = date;
                        endDate = null;
                    } else {
                        if (date.isBefore(startDate)) {
                            endDate = startDate;
                            startDate = date;
                        } else {
                            endDate = date;
                        }
                    }
                    updateCalendar();
                });
                calendarGrid.add(btn);
            }

            confirmBtn.setEnabled(startDate != null && endDate != null);
            calendarGrid.revalidate();
            calendarGrid.repaint();
        }

        private void styleRangeButton(JButton btn, LocalDate date) {
            if (startDate != null && date.equals(startDate)) {
                btn.setBackground(SharedStyles.NAV_ACTIVE_TOP);
                btn.setForeground(Color.WHITE);
                return;
            }
            if (endDate != null && date.equals(endDate)) {
                btn.setBackground(SharedStyles.NAV_ACTIVE_TOP);
                btn.setForeground(Color.WHITE);
                return;
            }
            if (startDate != null && endDate != null && !date.isBefore(startDate) && !date.isAfter(endDate)) {
                btn.setBackground(new Color(220, 234, 255));
                btn.setForeground(Color.BLACK);
                return;
            }
            btn.setBackground(null);
            btn.setForeground(Color.BLACK);
        }

        private LocalDate parseDateSafe(String raw) {
            if (raw == null || raw.isBlank()) return null;
            try {
                return LocalDate.parse(raw, AppointmentService.DATE_FORMATTER);
            } catch (java.time.format.DateTimeParseException ex) {
                return null;
            }
        }

        private boolean isConfirmed() {
            return confirmed;
        }

        private String getStartDate() {
            return startDate == null ? "" : startDate.format(AppointmentService.DATE_FORMATTER);
        }

        private String getEndDate() {
            return endDate == null ? "" : endDate.format(AppointmentService.DATE_FORMATTER);
        }
    }

    private void updateStarButtons(List<JToggleButton> buttons, int rating) {
        for (int i = 0; i < buttons.size(); i++) {
            JToggleButton btn = buttons.get(i);
            boolean filled = i < rating;
            btn.setText(filled ? "★" : "☆");
            btn.setForeground(filled ? new Color(245, 166, 35) : Color.GRAY);
        }
    }

    private int countWords(String text) {
        if (text == null) return 0;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return 0;
        return trimmed.split("\\s+").length;
    }

    private String resolveServiceNames(String serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty() || serviceIds.equals("NONE")) return "N/A";
        String[] ids = serviceIds.split(",");
        List<String> names = new java.util.ArrayList<>();
        for (String id : ids) {
            String rawId = id.trim();
            Service s = serviceLookup.findById(rawId);
            if (s == null) {
                String normalizedId = normalizeLegacyServiceId(rawId);
                if (!normalizedId.equals(rawId)) {
                    s = serviceLookup.findById(normalizedId);
                }
            }

            if (s != null) names.add(s.getServiceName());
            else names.add("Unknown Service (" + rawId + ")");
        }
        return String.join(", ", names);
    }

    private String getAppointmentCategory(Appointment appointment) {
        if (appointment == null) return "NORMAL";
        String serviceIds = appointment.getServiceId();
        if (serviceIds == null || serviceIds.isEmpty() || serviceIds.equals("NONE")) return "NORMAL";
        String[] ids = serviceIds.split(",");
        for (String id : ids) {
            String rawId = id.trim();
            Service s = serviceLookup.findById(rawId);
            if (s == null) {
                String normalizedId = normalizeLegacyServiceId(rawId);
                if (!normalizedId.equals(rawId)) {
                    s = serviceLookup.findById(normalizedId);
                }
            }
            if (s == null) return "MAJOR";
            if (!s.isIncludedInNormalService()) return "MAJOR";
        }
        return "NORMAL";
    }

    private String normalizeLegacyServiceId(String serviceId) {
        if (serviceId == null) return "";
        String id = serviceId.trim().toUpperCase();
        if (id.startsWith("SEV") && id.length() > 3) {
            return "SV" + id.substring(3);
        }
        return id;
    }

    private String resolveVehicleInfo(String vehicleId) {
        if (vehicleId == null || vehicleId.isEmpty()) return "N/A";
        Vehicle v = vehicleService.findById(vehicleId);
        if (v != null) {
            return v.getPlateNumber() + " (" + v.getBrand() + " " + v.getModel() + ")";
        }
        return "Unknown Vehicle (" + vehicleId + ")";
    }

    private static final class SlotOption {
        private final String time;
        private final String label;
        private final boolean available;

        private SlotOption(String time, String label, boolean available) {
            this.time = time;
            this.label = label;
            this.available = available;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
