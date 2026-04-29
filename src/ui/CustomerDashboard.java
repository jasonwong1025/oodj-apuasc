package ui;

import abstracts.AbstractUser;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.appointment.Appointment;
import model.review.Review;
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
        DefaultTableModel model = new DefaultTableModel(cols, 0);
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
        SharedStyles.applyTableStyle(table);
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

        List<JCheckBox> normalChecks = new ArrayList<>();
        JPanel normalPanel = new JPanel();
        normalPanel.setLayout(new BoxLayout(normalPanel, BoxLayout.Y_AXIS));
        normalPanel.setOpaque(false);
        for (Service s : normalServices) {
            JCheckBox cb = new JCheckBox(s.getServiceName() + " (RM " + String.format("%.2f", s.getPrice()) + ")");
            cb.setOpaque(false);
            cb.putClientProperty("service", s);
            normalChecks.add(cb);
            normalPanel.add(cb);
        }
        JScrollPane normalScroll = new JScrollPane(normalPanel);
        normalScroll.setPreferredSize(new Dimension(320, 220));
        normalScroll.setBorder(BorderFactory.createTitledBorder("Normal Services (select at least 1)"));
        normalScroll.setOpaque(false);
        normalScroll.getViewport().setOpaque(false);

        JLabel normalStatusLabel = new JLabel("0 selected (min 1)");
        normalStatusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        normalStatusLabel.setForeground(Color.GRAY);

        List<JCheckBox> majorChecks = new ArrayList<>();
        JPanel majorPanel = new JPanel();
        majorPanel.setLayout(new BoxLayout(majorPanel, BoxLayout.Y_AXIS));
        majorPanel.setOpaque(false);
        for (Service s : majorServices) {
            JCheckBox cb = new JCheckBox(s.getServiceName() + " (RM " + String.format("%.2f", s.getPrice()) + ")");
            cb.setOpaque(false);
            cb.putClientProperty("service", s);
            majorChecks.add(cb);
            majorPanel.add(cb);
        }
        JScrollPane majorScroll = new JScrollPane(majorPanel);
        majorScroll.setPreferredSize(new Dimension(320, 220));
        majorScroll.setBorder(BorderFactory.createTitledBorder("Major Services (select at least 1)"));
        majorScroll.setOpaque(false);
        majorScroll.getViewport().setOpaque(false);

        JLabel majorStatusLabel = new JLabel("0 selected (min 1)");
        majorStatusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        majorStatusLabel.setForeground(Color.GRAY);

        JLabel totalStatusLabel = new JLabel("Total selected: 0/8");
        totalStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        totalStatusLabel.setForeground(SharedStyles.NAV_ACTIVE_TOP);

        CardLayout stepCards = new CardLayout();
        JPanel stepPanel = new JPanel(stepCards);
        stepPanel.setOpaque(false);

        JButton step1Next = SharedStyles.createActionButton("Next", SharedStyles.BTN_BLUE);
        JButton step1Finish = SharedStyles.createActionButton("Finish", SharedStyles.BTN_GREEN);
        JPanel step1Actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        step1Actions.setOpaque(false);
        step1Actions.add(step1Finish);
        step1Actions.add(step1Next);

        JPanel step1 = new JPanel(new BorderLayout(0, 10));
        step1.setOpaque(false);
        step1.add(new JLabel("Step 1 of 3: Normal Services"), BorderLayout.NORTH);
        JPanel step1Center = new JPanel(new BorderLayout(0, 6));
        step1Center.setOpaque(false);
        step1Center.add(normalScroll, BorderLayout.CENTER);
        step1Center.add(normalStatusLabel, BorderLayout.SOUTH);
        step1.add(step1Center, BorderLayout.CENTER);
        step1.add(step1Actions, BorderLayout.SOUTH);

        JButton step2Back = SharedStyles.createActionButton("Back", SharedStyles.BTN_BLUE);
        JButton step2Finish = SharedStyles.createActionButton("Finish", SharedStyles.BTN_GREEN);
        JPanel step2Actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        step2Actions.setOpaque(false);
        step2Actions.add(step2Back);
        step2Actions.add(step2Finish);

        JPanel step2 = new JPanel(new BorderLayout(0, 10));
        step2.setOpaque(false);
        step2.add(new JLabel("Step 2 of 3: Major Services (optional)"), BorderLayout.NORTH);
        JPanel step2Center = new JPanel(new BorderLayout(0, 6));
        step2Center.setOpaque(false);
        step2Center.add(majorScroll, BorderLayout.CENTER);
        step2Center.add(majorStatusLabel, BorderLayout.SOUTH);
        step2.add(step2Center, BorderLayout.CENTER);
        step2.add(step2Actions, BorderLayout.SOUTH);

        LocalDate today = LocalDate.now();
        final LocalDate[] selectedDate = {today};
        final String[] selectedTime = {null};
        final LocalDate[] displayedMonth = {today.withDayOfMonth(1)};

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
            SlotType requestedType = majorChecks.stream().anyMatch(JCheckBox::isSelected)
                    ? SlotType.MAJOR
                    : SlotType.NORMAL;
            for (String slotTime : AppointmentService.getAllowedSlotTimes()) {
                AppointmentService.SlotCapacity cap = appointmentService.getSlotCapacity(dateValue, slotTime);
                boolean available = appointmentService.isSlotAvailable(dateValue, slotTime, requestedType);
                int majorLimit = appointmentService.getCapacityLimitForSlotType(SlotType.MAJOR);
                int normalLimit = appointmentService.getCapacityLimitForSlotType(SlotType.NORMAL);
                int totalLimit = appointmentService.getTotalCapacityLimit();
                String label = String.format("%s  [Major: %d/%d | Normal: %d/%d | Total: %d/%d]%s",
                        slotTime,
                        cap.getMajorCount(), majorLimit,
                        cap.getNormalCount(), normalLimit,
                        cap.getTotalCount(), totalLimit,
                        available ? "" : " (FULL)");
                slotCombo.addItem(new SlotOption(slotTime, label, available));
            }
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

        JButton step3Back = SharedStyles.createActionButton("Back", SharedStyles.BTN_BLUE);
        JPanel step3Actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        step3Actions.setOpaque(false);
        step3Actions.add(step3Back);

        JPanel step3 = new JPanel(new BorderLayout(0, 10));
        step3.setOpaque(false);
        step3.add(new JLabel("Step 3 of 3: Schedule"), BorderLayout.NORTH);
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
        step3.add(schedulePanel, BorderLayout.CENTER);
        step3.add(step3Actions, BorderLayout.SOUTH);

        stepPanel.add(step1, "STEP1");
        stepPanel.add(step2, "STEP2");
        stepPanel.add(step3, "STEP3");
        stepCards.show(stepPanel, "STEP1");

        JTextArea summaryArea = new JTextArea(16, 28);
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryArea.setBackground(Color.WHITE);
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Summary"));

        JButton checkoutBtn = SharedStyles.createActionButton("Checkout", SharedStyles.BTN_GREEN);

        Runnable updateSummary = () -> {
            StringBuilder sb = new StringBuilder();
            List<Service> selectedNormals = normalChecks.stream()
                    .filter(JCheckBox::isSelected)
                    .map(cb -> (Service) cb.getClientProperty("service"))
                    .collect(Collectors.toList());
            List<Service> selectedMajors = majorChecks.stream()
                    .filter(JCheckBox::isSelected)
                    .map(cb -> (Service) cb.getClientProperty("service"))
                    .collect(Collectors.toList());

            int totalCount = selectedNormals.size() + selectedMajors.size();
            double totalPrice = 0.0;
            for (Service s : selectedNormals) totalPrice += s.getPrice();
            for (Service s : selectedMajors) totalPrice += s.getPrice();

            sb.append("Normal services (" + selectedNormals.size() + ")\n");
            for (Service s : selectedNormals) {
                sb.append("- ").append(s.getServiceName()).append(" (RM ").append(String.format("%.2f", s.getPrice())).append(")\n");
            }
            sb.append("\nMajor services (" + selectedMajors.size() + ")\n");
            for (Service s : selectedMajors) {
                sb.append("- ").append(s.getServiceName()).append(" (RM ").append(String.format("%.2f", s.getPrice())).append(")\n");
            }
            sb.append("\nTotal selected: ").append(totalCount).append("/8\n");
            sb.append("Total: RM ").append(String.format("%.2f", totalPrice)).append("\n");
                String scheduleText = selectedTime[0] == null
                    ? "Not selected"
                    : selectedDate[0].format(AppointmentService.DATE_FORMATTER) + " " + selectedTime[0];
                sb.append("Schedule: ").append(scheduleText).append("\n");
            summaryArea.setText(sb.toString());

            totalStatusLabel.setText("Total selected: " + totalCount + "/8");
        };
        updateSummaryRef[0] = updateSummary;

        for (JCheckBox cb : normalChecks) {
            cb.addItemListener(ev -> {
                int total = (int) normalChecks.stream().filter(JCheckBox::isSelected).count()
                        + (int) majorChecks.stream().filter(JCheckBox::isSelected).count();
                if (total > 8) {
                    cb.setSelected(false);
                }
                int normalCount = (int) normalChecks.stream().filter(JCheckBox::isSelected).count();
                normalStatusLabel.setText(normalCount + " selected (min 1)");
                refreshSlots.run();
                updateSummaryRef[0].run();
            });
        }

        for (JCheckBox cb : majorChecks) {
            cb.addItemListener(ev -> {
                int total = (int) normalChecks.stream().filter(JCheckBox::isSelected).count()
                        + (int) majorChecks.stream().filter(JCheckBox::isSelected).count();
                if (total > 8) {
                    cb.setSelected(false);
                }
                int majorCount = (int) majorChecks.stream().filter(JCheckBox::isSelected).count();
                majorStatusLabel.setText(majorCount + " selected (min 1)");
                refreshSlots.run();
                updateSummaryRef[0].run();
            });
        }

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

        step1Next.addActionListener(e -> {
            int normalCount = (int) normalChecks.stream().filter(JCheckBox::isSelected).count();
            if (normalCount < 1) {
                SharedStyles.showWarning(this, "Select at least 1 normal service to continue.");
                return;
            }
            stepCards.show(stepPanel, "STEP2");
        });

        step1Finish.addActionListener(e -> {
            int normalCount = (int) normalChecks.stream().filter(JCheckBox::isSelected).count();
            if (normalCount < 1) {
                SharedStyles.showWarning(this, "Select at least 1 normal service to finish.");
                return;
            }
            stepCards.show(stepPanel, "STEP3");
        });

        step2Back.addActionListener(e -> stepCards.show(stepPanel, "STEP1"));

        step2Finish.addActionListener(e -> {
            int normalCount = (int) normalChecks.stream().filter(JCheckBox::isSelected).count();
            int majorCount = (int) majorChecks.stream().filter(JCheckBox::isSelected).count();
            if (normalCount < 1) {
                SharedStyles.showWarning(this, "Select at least 1 normal service.");
                return;
            }
            if (majorCount < 1) {
                SharedStyles.showWarning(this, "Select at least 1 major service.");
                return;
            }
            stepCards.show(stepPanel, "STEP3");
        });

        step3Back.addActionListener(e -> {
            int majorCount = (int) majorChecks.stream().filter(JCheckBox::isSelected).count();
            if (majorCount > 0) stepCards.show(stepPanel, "STEP2");
            else stepCards.show(stepPanel, "STEP1");
        });

        updateCalendarRef[0].run();
        refreshSlots.run();
        updateSummaryRef[0].run();
        
        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setOpaque(false);
        leftPanel.add(stepPanel, BorderLayout.CENTER);
        leftPanel.add(totalStatusLabel, BorderLayout.SOUTH);

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

            int normalCount = (int) normalChecks.stream().filter(JCheckBox::isSelected).count();
            int majorCount = (int) majorChecks.stream().filter(JCheckBox::isSelected).count();
            int totalCount = normalCount + majorCount;

            if (normalCount < 1) {
                SharedStyles.showWarning(this, "Please select at least 1 normal service.");
                return;
            }
            if (totalCount > 8) {
                SharedStyles.showWarning(this, "You can select up to 8 services only.");
                return;
            }

            List<Service> selected = new ArrayList<>();
            for (JCheckBox cb : normalChecks) {
                if (cb.isSelected()) selected.add((Service) cb.getClientProperty("service"));
            }
            for (JCheckBox cb : majorChecks) {
                if (cb.isSelected()) selected.add((Service) cb.getClientProperty("service"));
            }

            double total = selected.stream().mapToDouble(Service::getPrice).sum();
            String dateValue = selectedDate[0].format(AppointmentService.DATE_FORMATTER);
            String timeValue = selectedTime[0];
            SlotType requestedType = majorCount > 0 ? SlotType.MAJOR : SlotType.NORMAL;
            String scheduleError = appointmentService.validateSchedule(dateValue, timeValue, requestedType);
            if (scheduleError != null) {
                SharedStyles.showWarning(this, scheduleError);
                return;
            }
            StringBuilder summary = new StringBuilder("<html><body style='width: 300px;'>");
            summary.append("<h2>Booking Summary</h2>");
            summary.append("<hr>");
            summary.append("<b>Normal Services</b>:<br>");
            for (JCheckBox cb : normalChecks) {
                if (cb.isSelected()) {
                    Service s = (Service) cb.getClientProperty("service");
                    summary.append("• ").append(s.getServiceName()).append(": RM ").append(String.format("%.2f", s.getPrice())).append("<br>");
                }
            }
            if (majorCount > 0) {
                summary.append("<br><b>Major Services</b>:<br>");
                for (JCheckBox cb : majorChecks) {
                    if (cb.isSelected()) {
                        Service s = (Service) cb.getClientProperty("service");
                        summary.append("• ").append(s.getServiceName()).append(": RM ").append(String.format("%.2f", s.getPrice())).append("<br>");
                    }
                }
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

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        top.setOpaque(false);
        JButton cancelBtn = SharedStyles.createActionButton("Cancel Appointment", SharedStyles.BTN_RED);
        top.add(cancelBtn);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Vehicle", "Service Name(s)", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> list = appointmentService.getCustomerAppointments(currentUser.getUserId());
        for (Appointment a : list) {
            if (a.getStatus().equals("PENDING")) {
                model.addRow(new Object[]{
                    a.getAppointmentId(),
                    resolveVehicleInfo(a.getVehicleId()),
                    resolveServiceNames(a.getServiceId()),
                    a.getDate(),
                    a.getTime(),
                    a.getStatus()
                });
            }
        }

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

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

        String[] cols = {"Apt ID", "Vehicle", "Service Name(s)", "Date", "Status", "Payment", "Tech Feedback"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> list = appointmentService.getCustomerAppointments(currentUser.getUserId());
        for (Appointment a : list) {
            if (!a.getStatus().equals("PENDING")) {
                boolean isPaid = paymentService.isPaid(a.getAppointmentId());
                model.addRow(new Object[]{
                    a.getAppointmentId(),
                    resolveVehicleInfo(a.getVehicleId()),
                    resolveServiceNames(a.getServiceId()),
                    a.getDate(),
                    a.getStatus(),
                    isPaid ? "PAID" : "UNPAID",
                    a.getTechnicianFeedback()
                });
            }
        }

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildReviewsPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        top.setOpaque(false);
        JButton reviewBtn = SharedStyles.createActionButton("Write Review", SharedStyles.BTN_BLUE);
        top.add(reviewBtn);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Vehicle", "Service Name(s)", "Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> list = appointmentService.getCustomerAppointments(currentUser.getUserId());
        List<Review> reviews = reviewService.getCustomerReviews(currentUser.getUserId());

        for (Appointment a : list) {
            if (a.getStatus().equals("COMPLETED")) {
                boolean reviewed = reviews.stream().anyMatch(r -> r.getAppointmentId().equals(a.getAppointmentId()));
                boolean paid = paymentService.isPaid(a.getAppointmentId());
                String status = reviewed ? "Reviewed" : (paid ? "Available" : "Payment Pending");
                model.addRow(new Object[]{
                    a.getAppointmentId(),
                    resolveVehicleInfo(a.getVehicleId()),
                    resolveServiceNames(a.getServiceId()),
                    a.getDate(),
                    status
                });
            }
        }

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        reviewBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                SharedStyles.showSelectionError(this);
                return;
            }
            String status = table.getValueAt(row, 3).toString();
            if (status.equals("Available")) showReviewDialog(table.getValueAt(row, 0).toString());
            else JOptionPane.showMessageDialog(this, "System: " + status);
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
        JComboBox<String> rating = SharedStyles.createFilterCombo(new String[]{"1", "2", "3", "4", "5"});
        JTextArea comment = new JTextArea(5, 20);
        Object[] msg = {"Rating:", rating, "Comment:", new JScrollPane(comment)};
        if (JOptionPane.showConfirmDialog(this, msg, "Submit Review", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            int score = Integer.parseInt(rating.getSelectedItem().toString());
            reviewService.submitReview(currentUser.getUserId(), aptId, score, comment.getText().trim());
            refresh();
        }
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
