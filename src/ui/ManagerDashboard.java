package ui;

import abstracts.AbstractUser;
import model.appointment.Appointment;
import model.service.Category;
import model.service.Service;
import model.users.User;
import service_layer.AppointmentService;
import service_layer.CategoryService;
import service_layer.ServiceService;
import service_layer.UserService;
import utils.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ManagerDashboard extends JFrame implements Refreshable {

    private static final String[] NAV_ITEMS = {
            "Dashboard",
            "User Management",
            "Service Management",
            "All Feedback",
            "Reports",
            "My Profile"
    };

    private final AbstractUser currentUser;
    private final UserService userService;
    private final ServiceService serviceService;
    private final CategoryService categoryService;
    private final AppointmentService appointmentService;

    private CardLayout cardLayout;
    private JPanel cardPanel;

    private DefaultTableModel userTableModel;
    private JTable userTable;
    private JTextField userSearchField;
    private JComboBox<String> roleFilterCombo;
    private DefaultTableModel serviceTableModel;
    private JTable serviceTable;
    private JTextField serviceSearchField;
    private JComboBox<String> serviceCategoryFilter;
    private DefaultTableModel categoryTableModel;
    private JTable categoryTable;
    private JTextField categorySearchField;
    private DefaultListModel<String> navModel;
    private JList<String> navList;
    /** Rebuilt when switching to Dashboard so counts stay current. */
    private JPanel dashboardPanelRef;
    private boolean serviceExpanded = false;
    private boolean updatingNav = false;

    private static final String SVC_HEADER = "Service Management";
    private static final String SVC_CATALOG = "Manage Service Catalog";
    private static final String SVC_CATEGORIES = "Manage Categories";

    public ManagerDashboard(AbstractUser user) {
        this.currentUser = user;
        this.userService = new UserService();
        this.serviceService = new ServiceService();
        this.categoryService = new CategoryService();
        this.appointmentService = new AppointmentService();

        setTitle("APU-ASC | Manager - " + currentUser.getFullName());
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

        JComboBox<String> lang = SharedStyles.createFilterCombo(new String[]{"English", "Bahasa Melayu"});
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        center.setOpaque(false);
        center.add(lang);
        header.add(center, BorderLayout.CENTER);

        JLabel who = new JLabel(currentUser.getFullName() + "  |  " + roleDisplay(currentUser.getRole()));
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
        for (String s : NAV_ITEMS) {
            navModel.addElement(s);
        }
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
                String text = (String) value;
                boolean isSub = isServiceSubItem(text);
                l.setOpaque(true);

                if (isSub) {
                    l.setBorder(new EmptyBorder(12, 44, 12, 16));
                    l.setText("\u2022  " + text);
                    l.setFont(new Font("SansSerif", Font.PLAIN, 13));
                } else {
                    l.setBorder(new EmptyBorder(12, 20, 12, 16));
                    l.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    if (SVC_HEADER.equals(text)) {
                        l.setText(text + (serviceExpanded ? "  \u25BE" : "  \u25B8"));
                    }
                }

                if (isSelected) {
                    l.setBackground(SharedStyles.NAV_ACTIVE_TOP);
                    l.setForeground(Color.WHITE);
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                } else {
                    l.setBackground(isSub ? new Color(48, 48, 54) : SharedStyles.SIDEBAR_BG);
                    l.setForeground(SharedStyles.TEXT_ON_DARK);
                }
                return l;
            }
        });

        JScrollPane navScroll = new JScrollPane(navList);
        navScroll.setBorder(null);
        navScroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(SharedStyles.SIDEBAR_BG);
        side.setPreferredSize(new Dimension(240, 0));
        side.add(navScroll, BorderLayout.CENTER);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);
        cardPanel.add(buildUserManagementPanel(), "USER");
        cardPanel.add(buildServiceCatalogPanel(), "SVC_CATALOG");
        cardPanel.add(buildCategoriesPanel(), "SVC_CATEGORIES");
        cardPanel.add(buildPlaceholderPanel("All Feedback", "View customer and staff feedback (link to data layer next)."), "FEED");
        cardPanel.add(buildPlaceholderPanel("Reports", "Export analysis summaries (link to appointments/payments next)."), "REPORT");
        cardPanel.add(buildMyProfilePanel(), "PROFILE");

        navList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = navList.locationToIndex(e.getPoint());
                if (idx < 0) return;
                if (SVC_HEADER.equals(navModel.get(idx))) {
                    toggleServiceDropdown();
                }
            }
        });

        navList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || updatingNav) return;
            refresh();
        });
        navList.setSelectedIndex(0);

        wrap.add(side, BorderLayout.WEST);
        wrap.add(cardPanel, BorderLayout.CENTER);
        return wrap;
    }

    @Override
    public void refresh() {
        int i = navList.getSelectedIndex();
        if (i < 0) return;
        String selected = navModel.get(i);
        if (SVC_HEADER.equals(selected)) return;

        switch (selected) {
            case "Dashboard":
                if (dashboardPanelRef != null) {
                    cardPanel.remove(dashboardPanelRef);
                }
                dashboardPanelRef = buildDashboardPanel();
                cardPanel.add(dashboardPanelRef, "DASHBOARD");
                cardLayout.show(cardPanel, "DASHBOARD");
                cardPanel.revalidate();
                cardPanel.repaint();
                break;
            case "User Management": cardLayout.show(cardPanel, "USER"); refreshUserTable(); break;
            case SVC_CATALOG: cardLayout.show(cardPanel, "SVC_CATALOG"); refreshServiceTable(); break;
            case SVC_CATEGORIES: cardLayout.show(cardPanel, "SVC_CATEGORIES"); refreshCategoryTable(); break;
            case "All Feedback": cardLayout.show(cardPanel, "FEED"); break;
            case "Reports": cardLayout.show(cardPanel, "REPORT"); break;
            case "My Profile": cardLayout.show(cardPanel, "PROFILE"); break;
        }
        
        // Consistent title sync
        User self = userService.findByUserId(currentUser.getUserId());
        if (self != null) {
            setTitle("APU-ASC | Manager - " + self.getFullName());
        }
    }

    private void toggleServiceDropdown() {
        updatingNav = true;
        int svcIdx = -1;
        for (int i = 0; i < navModel.size(); i++) {
            if (SVC_HEADER.equals(navModel.get(i))) {
                svcIdx = i;
                break;
            }
        }
        if (svcIdx < 0) {
            updatingNav = false;
            return;
        }

        if (serviceExpanded) {
            navModel.removeElement(SVC_CATALOG);
            navModel.removeElement(SVC_CATEGORIES);
            serviceExpanded = false;
            navList.repaint();
            updatingNav = false;
        } else {
            navModel.insertElementAt(SVC_CATALOG, svcIdx + 1);
            navModel.insertElementAt(SVC_CATEGORIES, svcIdx + 2);
            serviceExpanded = true;
            navList.repaint();
            updatingNav = false;
            navList.setSelectedIndex(svcIdx + 1);
        }
    }

    private boolean isServiceSubItem(String text) {
        return SVC_CATALOG.equals(text) || SVC_CATEGORIES.equals(text);
    }

    private JPanel buildDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(SharedStyles.MAIN_BG);
        p.setBorder(new EmptyBorder(20, 24, 24, 24));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel heading = new JLabel("Dashboard");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        topRow.add(heading, BorderLayout.WEST);
        p.add(topRow, BorderLayout.NORTH);

        List<User> allUsers = userService.listAllUsers();
        long normalServiceTechs = allUsers.stream()
                .filter(u -> "Technician".equals(u.getRole()))
                .filter(u -> "Normal Service".equals(u.getTechnicianServiceType()))
                .count();
        long majorServiceTechs = allUsers.stream()
                .filter(u -> "Technician".equals(u.getRole()))
                .filter(u -> "Major Service".equals(u.getTechnicianServiceType()))
                .count();
        List<Appointment> appointments = appointmentService.getAllAppointments();

        JPanel analyticsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        analyticsRow.setOpaque(false);
        analyticsRow.add(buildYearlyEarningsStatCard(appointments));
        analyticsRow.add(buildTechnicianServiceTypeCard(normalServiceTechs, majorServiceTechs));

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        center.add(analyticsRow, BorderLayout.NORTH);
        center.add(buildAppointmentsTableCard(appointments), BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildTechnicianServiceTypeCard(long normalServiceTechs, long majorServiceTechs) {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("Technician Distribution");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 12, 0));
        content.setOpaque(false);
        content.add(createMetricTile("Normal Service", String.valueOf(normalServiceTechs), new Color(88, 97, 255)));
        content.add(createMetricTile("Major Service", String.valueOf(majorServiceTechs), new Color(255, 138, 101)));
        card.add(content, BorderLayout.CENTER);

        JLabel total = new JLabel("Total technicians: " + (normalServiceTechs + majorServiceTechs));
        total.setFont(new Font("SansSerif", Font.PLAIN, 12));
        total.setForeground(new Color(95, 98, 110));
        card.add(total, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createMetricTile(String label, String value, Color accent) {
        JPanel tile = new JPanel(new BorderLayout(0, 8));
        tile.setOpaque(true);
        tile.setBackground(new Color(245, 247, 255));
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 240)),
                new EmptyBorder(14, 14, 14, 14)));

        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(new Color(84, 88, 104));

        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 34));
        v.setForeground(accent);

        tile.add(l, BorderLayout.NORTH);
        tile.add(v, BorderLayout.CENTER);
        return tile;
    }

    private JPanel buildYearlyEarningsStatCard(List<Appointment> appointments) {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(12, 0));

        JLabel title = new JLabel("General Statistic");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(title, BorderLayout.NORTH);

        int[] monthlyTotalEarned = new int[12]; // Placeholder until payment amount flow is implemented
        int[] monthlyEarnedTrend = new int[12]; // Placeholder trend line for total earned

        JPanel chartAndStats = new JPanel(new BorderLayout(12, 0));
        chartAndStats.setOpaque(false);

        JPanel chartHolder = new JPanel(new BorderLayout(0, 6));
        chartHolder.setOpaque(false);
        chartHolder.add(new MiniChartPanel(monthlyTotalEarned, monthlyEarnedTrend), BorderLayout.CENTER);
        JLabel chartNote = new JLabel("Total earned graph (yearly, pending payment integration)");
        chartNote.setFont(new Font("SansSerif", Font.PLAIN, 11));
        chartNote.setForeground(new Color(120, 125, 142));
        chartHolder.add(chartNote, BorderLayout.SOUTH);
        chartAndStats.add(chartHolder, BorderLayout.CENTER);

        int yearlyTotalBookings = countByYear(appointments, YearMonth.now().getYear(), null);
        int yearlyCancelledBookings = countByYear(appointments, YearMonth.now().getYear(), "CANCELLED");
        String yearlyTotalEarned = "RM 0.00";

        JPanel statsColumn = new JPanel();
        statsColumn.setOpaque(false);
        statsColumn.setLayout(new BoxLayout(statsColumn, BoxLayout.Y_AXIS));
        statsColumn.setPreferredSize(new Dimension(180, 0));
        statsColumn.add(createMetricLine("Total Earned (Year)", yearlyTotalEarned));
        statsColumn.add(createMetricLine("Total Bookings (Year)", String.valueOf(yearlyTotalBookings)));
        statsColumn.add(createMetricLine("Cancelled Bookings (Year)", String.valueOf(yearlyCancelledBookings)));

        chartAndStats.add(statsColumn, BorderLayout.EAST);
        card.add(chartAndStats, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAppointmentsTableCard(List<Appointment> appointments) {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Opened bookings");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleRow.add(title, BorderLayout.WEST);

        JTextField quickSearch = SharedStyles.createFilterField(18);
        quickSearch.setText("Search...");
        JButton filterBtn = SharedStyles.createActionButton("Filters", SharedStyles.BTN_BLUE);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(quickSearch);
        right.add(filterBtn);
        titleRow.add(right, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        String[] cols = {"Appointment ID", "Customer ID", "Vehicle ID", "Service ID(s)", "Date", "Time", "Status", "Technician ID"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        List<Appointment> opened = new ArrayList<>();
        for (Appointment a : appointments) {
            if (!"PENDING".equalsIgnoreCase(a.getStatus())) continue;
            opened.add(a);
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
        title.setText("Opened bookings (" + opened.size() + ")");

        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        table.setGridColor(new Color(220, 220, 225));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    private JPanel createMetricLine(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 0, 8, 0));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(new Color(95, 98, 110));
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 22));
        v.setForeground(SharedStyles.NAV_ACTIVE_TOP);
        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private int countByYear(List<Appointment> appointments, int year, String statusFilter) {
        int total = 0;
        for (Appointment a : appointments) {
            LocalDate date = parseDate(a.getDate());
            if (date == null || date.getYear() != year) continue;
            if (statusFilter != null && !statusFilter.equalsIgnoreCase(a.getStatus())) continue;
            total++;
        }
        return total;
    }

    private LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) return null;
        try {
            return LocalDate.parse(rawDate, AppointmentService.DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static class MiniChartPanel extends JPanel {
        private final int[] barSeries;
        private final int[] lineSeries;

        private MiniChartPanel(int[] barSeries, int[] lineSeries) {
            this.barSeries = barSeries;
            this.lineSeries = lineSeries;
            setOpaque(false);
            setPreferredSize(new Dimension(300, 190));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int left = 20;
                int right = w - 10;
                int top = 14;
                int bottom = h - 26;

                g2.setColor(new Color(235, 236, 246));
                for (int i = 0; i < 4; i++) {
                    int y = top + i * (bottom - top) / 3;
                    g2.drawLine(left, y, right, y);
                }

                int max = 1;
                for (int v : barSeries) max = Math.max(max, v);
                for (int v : lineSeries) max = Math.max(max, v);

                int slotW = Math.max(1, (right - left) / 12);
                int barW = Math.max(4, slotW - 6);

                int prevX = -1;
                int prevY = -1;
                for (int i = 0; i < 12; i++) {
                    int barVal = i < barSeries.length ? barSeries[i] : 0;
                    int lineVal = i < lineSeries.length ? lineSeries[i] : 0;
                    int x = left + i * slotW + 3;

                    int barH = (int) ((barVal / (double) max) * (bottom - top));
                    int yBar = bottom - barH;
                    g2.setColor(new Color(206, 212, 247));
                    g2.fillRoundRect(x, yBar, barW, barH, 8, 8);

                    int yPoint = bottom - (int) ((lineVal / (double) max) * (bottom - top));
                    int pointX = x + barW / 2;
                    g2.setColor(new Color(88, 97, 255));
                    g2.fillOval(pointX - 3, yPoint - 3, 6, 6);
                    if (prevX >= 0) {
                        g2.drawLine(prevX, prevY, pointX, yPoint);
                    }
                    prevX = pointX;
                    prevY = yPoint;
                }

                g2.setColor(new Color(120, 125, 142));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                String[] months = {"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"};
                for (int i = 0; i < 12; i++) {
                    int x = left + i * slotW + slotW / 2;
                    g2.drawString(months[i], x - 3, h - 8);
                }
            } finally {
                g2.dispose();
            }
        }
    }

    private JPanel buildPlaceholderPanel(String title, String body) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SharedStyles.MAIN_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(24, 24, 8, 24);
        JLabel h = new JLabel(title);
        h.setFont(new Font("SansSerif", Font.BOLD, 22));
        p.add(h, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 24, 24, 24);
        JLabel t = new JLabel("<html><div style='width:520px'>" + body + "</div></html>");
        t.setFont(new Font("SansSerif", Font.PLAIN, 14));
        p.add(t, gbc);
        return p;
    }

    private JPanel buildServiceCatalogPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        row1.setOpaque(false);
        row1.add(new JLabel("Search:"));
        serviceSearchField = SharedStyles.createFilterField(24);
        row1.add(serviceSearchField);
        row1.add(new JLabel("Category:"));
        serviceCategoryFilter = SharedStyles.createFilterCombo(new String[]{"ALL"});
        row1.add(serviceCategoryFilter);
        JButton filterBtn = SharedStyles.createActionButton("Filter", SharedStyles.BTN_BLUE);
        filterBtn.addActionListener(e -> refreshServiceTable());
        row1.add(filterBtn);
        top.add(row1);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        row2.setOpaque(false);
        JButton addBtn = SharedStyles.createActionButton("Add Service", SharedStyles.BTN_GREEN);
        addBtn.addActionListener(e -> showAddServiceDialog());
        row2.add(addBtn);

        JButton editBtn = SharedStyles.createActionButton("Edit Selected", SharedStyles.BTN_BLUE);
        editBtn.addActionListener(e -> {
            if (serviceTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(this); return; }
            showEditServiceDialog();
        });
        row2.add(editBtn);

        JButton deleteBtn = SharedStyles.createActionButton("Delete Selected", SharedStyles.BTN_RED);
        deleteBtn.addActionListener(e -> {
            if (serviceTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(this); return; }
            deleteSelectedService();
        });
        row2.add(deleteBtn);

        JButton refreshBtn = SharedStyles.createActionButton("Refresh", SharedStyles.BTN_BLUE);
        refreshBtn.addActionListener(e -> refreshServiceTable());
        row2.add(refreshBtn);

        top.add(row2);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Service ID", "Service Name", "Category", "Price (RM)", "In Normal Service"};
        serviceTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        serviceTable = new JTable(serviceTableModel);
        serviceTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        serviceTable.setRowHeight(28);
        serviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serviceTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        serviceTable.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        serviceTable.setGridColor(new Color(220, 220, 225));
        serviceTable.setShowGrid(true);
        serviceTable.setFillsViewportHeight(true);
        serviceTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });
        JScrollPane sp = new JScrollPane(serviceTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        root.add(sp, BorderLayout.CENTER);

        refreshServiceTable();
        return root;
    }

    private JPanel buildCategoriesPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        row1.setOpaque(false);
        row1.add(new JLabel("Search:"));
        categorySearchField = SharedStyles.createFilterField(24);
        row1.add(categorySearchField);
        JButton filterBtn = SharedStyles.createActionButton("Filter", SharedStyles.BTN_BLUE);
        filterBtn.addActionListener(e -> refreshCategoryTable());
        row1.add(filterBtn);
        top.add(row1);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        row2.setOpaque(false);
        JButton addBtn = SharedStyles.createActionButton("Add Category", SharedStyles.BTN_GREEN);
        addBtn.addActionListener(e -> showAddCategoryDialog());
        row2.add(addBtn);

        JButton editBtn = SharedStyles.createActionButton("Edit Selected", SharedStyles.BTN_BLUE);
        editBtn.addActionListener(e -> {
            if (categoryTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(this); return; }
            showEditCategoryDialog();
        });
        row2.add(editBtn);

        JButton deleteBtn = SharedStyles.createActionButton("Delete Selected", SharedStyles.BTN_RED);
        deleteBtn.addActionListener(e -> {
            if (categoryTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(this); return; }
            deleteSelectedCategory();
        });
        row2.add(deleteBtn);

        JButton refreshBtn = SharedStyles.createActionButton("Refresh", SharedStyles.BTN_BLUE);
        refreshBtn.addActionListener(e -> refreshCategoryTable());
        row2.add(refreshBtn);

        top.add(row2);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Category ID", "Category Name"};
        categoryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        categoryTable = new JTable(categoryTableModel);
        categoryTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        categoryTable.setRowHeight(28);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        categoryTable.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        categoryTable.setGridColor(new Color(220, 220, 225));
        categoryTable.setShowGrid(true);
        categoryTable.setFillsViewportHeight(true);
        categoryTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });
        JScrollPane sp = new JScrollPane(categoryTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        root.add(sp, BorderLayout.CENTER);

        refreshCategoryTable();
        return root;
    }

    private JPanel buildUserManagementPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        row1.setOpaque(false);
        row1.add(new JLabel("Search:"));
        userSearchField = SharedStyles.createFilterField(24);
        row1.add(userSearchField);
        row1.add(new JLabel("Role:"));
        roleFilterCombo = SharedStyles.createFilterCombo(new String[]{
                "ALL", "Manager", "Counter Staff", "Technician", "Customer"
        });
        row1.add(roleFilterCombo);
        JButton filterBtn = SharedStyles.createActionButton("Filter", SharedStyles.BTN_BLUE);
        filterBtn.addActionListener(e -> refreshUserTable());
        row1.add(filterBtn);
        top.add(row1);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        row2.setOpaque(false);
        JButton addBtn = SharedStyles.createActionButton("Add User", SharedStyles.BTN_GREEN);
        addBtn.addActionListener(e -> showAddUserDialog());
        row2.add(addBtn);

        JButton editBtn = SharedStyles.createActionButton("Edit Selected", SharedStyles.BTN_BLUE);
        editBtn.addActionListener(e -> {
            if (userTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(this); return; }
            showEditUserDialog();
        });
        row2.add(editBtn);

        JButton deleteBtn = SharedStyles.createActionButton("Delete Selected", SharedStyles.BTN_RED);
        deleteBtn.addActionListener(e -> {
            if (userTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(this); return; }
            deleteSelectedUser();
        });
        row2.add(deleteBtn);

        JButton deactBtn = SharedStyles.createActionButton("Deactivate", SharedStyles.BTN_ORANGE);
        deactBtn.addActionListener(e -> {
            if (userTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(this); return; }
            setSelectedActive(false);
        });
        row2.add(deactBtn);

        JButton reactBtn = SharedStyles.createActionButton("Reactivate", SharedStyles.BTN_GREEN);
        reactBtn.addActionListener(e -> {
            if (userTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(this); return; }
            setSelectedActive(true);
        });
        row2.add(reactBtn);

        JButton refreshBtn = SharedStyles.createActionButton("Refresh", SharedStyles.BTN_BLUE);
        refreshBtn.addActionListener(e -> refreshUserTable());
        row2.add(refreshBtn);

        JButton exportBtn = SharedStyles.createActionButton("Export CSV", SharedStyles.BTN_BLUE);
        exportBtn.addActionListener(e -> exportCsv());
        row2.add(exportBtn);

        top.add(row2);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Email", "Contact", "Role", "Service Type", "Status"};
        userTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        userTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userTable.setRowHeight(28);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        userTable.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        userTable.setGridColor(new Color(220, 220, 225));
        userTable.setShowGrid(true);
        userTable.setFillsViewportHeight(true);
        userTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });
        JScrollPane sp = new JScrollPane(userTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        root.add(sp, BorderLayout.CENTER);

        refreshUserTable();
        return root;
    }

    private JPanel buildMyProfilePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SharedStyles.MAIN_BG);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel h = new JLabel("My Profile");
        h.setFont(new Font("SansSerif", Font.BOLD, 22));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        p.add(h, gbc);
        gbc.gridwidth = 1;

        User self = userService.findByUserId(currentUser.getUserId());
        if (self == null) {
            gbc.gridy = 1;
            p.add(new JLabel("Could not load profile."), gbc);
            return p;
        }

        JTextField fullName = SharedStyles.createFilterField(28);
        fullName.setText(self.getFullName());
        JTextField email = SharedStyles.createFilterField(28);
        email.setText(self.getEmail());
        JTextField contact = SharedStyles.createFilterField(28);
        contact.setText(self.getContact());
        JPasswordField pass = new JPasswordField(28);
        pass.setBorder(userSearchField.getBorder());

        int y = 1;
        addProfileRow(p, gbc, y++, "Full Name:", fullName);
        addProfileRow(p, gbc, y++, "Email:", email);
        addProfileRow(p, gbc, y++, "Contact:", contact);
        addProfileRow(p, gbc, y++, "New Password (optional):", pass);

        JButton save = SharedStyles.createActionButton("Save Profile", SharedStyles.BTN_GREEN);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            User u = userService.findByUserId(currentUser.getUserId());
            if (u == null) return;
            u.setFullName(fullName.getText().trim());
            u.setEmail(email.getText().trim());
            u.setContact(contact.getText().trim());
            String np = new String(pass.getPassword());
            if (ValidationUtil.isNotEmpty(np)) {
                u.setPassword(np);
            }
            String err = userService.updateUser(u, currentUser.getUserId());
            if (err != null) {
                JOptionPane.showMessageDialog(this, err, "Profile", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Profile updated.", "Profile", JOptionPane.INFORMATION_MESSAGE);
                pass.setText("");
            }
        });
        p.add(save, gbc);

        return p;
    }

    private void addProfileRow(JPanel p, GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        p.add(field, gbc);
    }

    private void refreshUserTable() {
        if (userTableModel == null) return;
        String roleKey = mapRoleFilter((String) roleFilterCombo.getSelectedItem());
        List<User> rows = userService.filterUsers(userSearchField.getText(), roleKey);
        userTableModel.setRowCount(0);
        for (User u : rows) {
            userTableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getContact(),
                    roleDisplay(u.getRole()),
                    "Technician".equals(u.getRole()) ? u.getTechnicianServiceType() : "-",
                    u.isActive() ? "ACTIVE" : "INACTIVE"
            });
        }
    }

    private User getSelectedUserFromTable() {
        int r = userTable.getSelectedRow();
        if (r < 0) return null;
        String id = (String) userTableModel.getValueAt(r, 0);
        return userService.findByUserId(id);
    }

    private void refreshServiceCategoryFilter() {
        if (serviceCategoryFilter == null) return;
        String selected = (String) serviceCategoryFilter.getSelectedItem();
        serviceCategoryFilter.removeAllItems();
        serviceCategoryFilter.addItem("ALL");
        for (Category c : categoryService.listAll()) {
            serviceCategoryFilter.addItem(c.getCategoryId() + " - " + c.getCategoryName());
        }
        if (selected != null) {
            serviceCategoryFilter.setSelectedItem(selected);
        }
    }

    private void refreshServiceTable() {
        if (serviceTableModel == null) return;
        refreshServiceCategoryFilter();
        String keyword = serviceSearchField == null ? "" : serviceSearchField.getText().trim();
        String categoryDisplay = serviceCategoryFilter == null ? "ALL" : String.valueOf(serviceCategoryFilter.getSelectedItem());
        String categoryId = "ALL".equals(categoryDisplay) ? "ALL" : extractCategoryId(categoryDisplay);
        List<Service> rows = serviceService.filter(keyword, categoryId);
        serviceTableModel.setRowCount(0);
        for (Service s : rows) {
            String categoryName = categoryService.getCategoryNameById(s.getCategoryId());
            serviceTableModel.addRow(new Object[]{
                    s.getServiceId(),
                    s.getServiceName(),
                    categoryName != null ? categoryName : s.getCategoryId(),
                    String.format("%.2f", s.getPrice()),
                    s.isIncludedInNormalService() ? "YES" : "NO"
            });
        }
    }

    private Service getSelectedServiceFromTable() {
        if (serviceTable == null || serviceTableModel == null) return null;
        int r = serviceTable.getSelectedRow();
        if (r < 0) return null;
        String id = String.valueOf(serviceTableModel.getValueAt(r, 0));
        return serviceService.findById(id);
    }

    private void showAddServiceDialog() {
        List<Category> categories = categoryService.listAll();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No categories found. Please add categories first under Manage Categories.",
                    "Add Service", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog d = new JDialog(this, "Add Service", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = SharedStyles.createFilterField(22);
        String[] categoryItems = categories.stream()
                .map(c -> c.getCategoryId() + " - " + c.getCategoryName())
                .toArray(String[]::new);
        JComboBox<String> categoryField = SharedStyles.createFilterCombo(categoryItems);
        JTextField priceField = SharedStyles.createFilterField(22);
        JCheckBox includeInNormalService = new JCheckBox("Include in Normal Service");
        includeInNormalService.setOpaque(false);

        int y = 0;
        addDialogRow(d, gbc, y++, "Service Name:", nameField);
        addDialogRow(d, gbc, y++, "Category:", categoryField);
        addDialogRow(d, gbc, y++, "Price (RM):", priceField);
        addDialogRow(d, gbc, y++, "Normal Service:", includeInNormalService);

        JButton save = SharedStyles.createActionButton("Save", SharedStyles.BTN_GREEN);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            String err = serviceService.addService(
                    nameField.getText().trim(),
                    extractCategoryId(String.valueOf(categoryField.getSelectedItem())),
                    priceField.getText().trim(),
                    includeInNormalService.isSelected());
            if (err != null) {
                JOptionPane.showMessageDialog(d, err, "Add Service", JOptionPane.ERROR_MESSAGE);
            } else {
                d.dispose();
                refreshServiceTable();
            }
        });
        d.add(save, gbc);

        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void showEditServiceDialog() {
        Service target = getSelectedServiceFromTable();
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Select a service to edit.", "Edit Service", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Category> categories = categoryService.listAll();

        JDialog d = new JDialog(this, "Edit Service", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField idField = SharedStyles.createFilterField(22);
        idField.setText(target.getServiceId());
        idField.setEditable(false);
        JTextField nameField = SharedStyles.createFilterField(22);
        nameField.setText(target.getServiceName());
        String[] categoryItems = categories.stream()
                .map(c -> c.getCategoryId() + " - " + c.getCategoryName())
                .toArray(String[]::new);
        JComboBox<String> categoryField = SharedStyles.createFilterCombo(categoryItems);
        String selectedCategoryDisplay = target.getCategoryId() + " - " +
                (categoryService.getCategoryNameById(target.getCategoryId()) != null
                        ? categoryService.getCategoryNameById(target.getCategoryId())
                        : target.getCategoryId());
        categoryField.setSelectedItem(selectedCategoryDisplay);
        JTextField priceField = SharedStyles.createFilterField(22);
        priceField.setText(String.format("%.2f", target.getPrice()));
        JCheckBox includeInNormalService = new JCheckBox("Include in Normal Service");
        includeInNormalService.setOpaque(false);
        includeInNormalService.setSelected(target.isIncludedInNormalService());

        int y = 0;
        addDialogRow(d, gbc, y++, "Service ID:", idField);
        addDialogRow(d, gbc, y++, "Service Name:", nameField);
        addDialogRow(d, gbc, y++, "Category:", categoryField);
        addDialogRow(d, gbc, y++, "Price (RM):", priceField);
        addDialogRow(d, gbc, y++, "Normal Service:", includeInNormalService);

        JButton save = SharedStyles.createActionButton("Update", SharedStyles.BTN_BLUE);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            String err = serviceService.updateService(
                    target.getServiceId(),
                    nameField.getText().trim(),
                    extractCategoryId(String.valueOf(categoryField.getSelectedItem())),
                    priceField.getText().trim(),
                    includeInNormalService.isSelected());
            if (err != null) {
                JOptionPane.showMessageDialog(d, err, "Edit Service", JOptionPane.ERROR_MESSAGE);
            } else {
                d.dispose();
                refreshServiceTable();
            }
        });
        d.add(save, gbc);

        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void deleteSelectedService() {
        Service target = getSelectedServiceFromTable();
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Select a service to delete.", "Delete Service", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this service?\n\n" + target.getServiceName() + " (" + target.getServiceId() + ")",
                "Delete Service",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String err = serviceService.deleteService(target.getServiceId());
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Delete Service", JOptionPane.ERROR_MESSAGE);
        } else {
            refreshServiceTable();
        }
    }

    private String extractCategoryId(String categoryDisplay) {
        if (categoryDisplay == null) return "";
        int sep = categoryDisplay.indexOf(" - ");
        if (sep < 0) return categoryDisplay.trim();
        return categoryDisplay.substring(0, sep).trim();
    }

    private void refreshCategoryTable() {
        if (categoryTableModel == null) return;
        String keyword = categorySearchField == null ? "" : categorySearchField.getText().trim();
        List<Category> rows = categoryService.filter(keyword);
        categoryTableModel.setRowCount(0);
        for (Category c : rows) {
            categoryTableModel.addRow(new Object[]{c.getCategoryId(), c.getCategoryName()});
        }
    }

    private Category getSelectedCategoryFromTable() {
        if (categoryTable == null || categoryTableModel == null) return null;
        int r = categoryTable.getSelectedRow();
        if (r < 0) return null;
        String id = String.valueOf(categoryTableModel.getValueAt(r, 0));
        return categoryService.findById(id);
    }

    private void showAddCategoryDialog() {
        JDialog d = new JDialog(this, "Add Category", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = SharedStyles.createFilterField(22);

        int y = 0;
        addDialogRow(d, gbc, y++, "Category Name:", nameField);

        JButton save = SharedStyles.createActionButton("Save", SharedStyles.BTN_GREEN);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            String err = categoryService.addCategory(nameField.getText().trim());
            if (err != null) {
                JOptionPane.showMessageDialog(d, err, "Add Category", JOptionPane.ERROR_MESSAGE);
            } else {
                d.dispose();
                refreshCategoryTable();
            }
        });
        d.add(save, gbc);

        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void showEditCategoryDialog() {
        Category target = getSelectedCategoryFromTable();
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Select a category to edit.", "Edit Category", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog d = new JDialog(this, "Edit Category", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField idField = SharedStyles.createFilterField(22);
        idField.setText(target.getCategoryId());
        idField.setEditable(false);
        JTextField nameField = SharedStyles.createFilterField(22);
        nameField.setText(target.getCategoryName());

        int y = 0;
        addDialogRow(d, gbc, y++, "Category ID:", idField);
        addDialogRow(d, gbc, y++, "Category Name:", nameField);

        JButton save = SharedStyles.createActionButton("Update", SharedStyles.BTN_BLUE);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            String err = categoryService.updateCategory(target.getCategoryId(), nameField.getText().trim());
            if (err != null) {
                JOptionPane.showMessageDialog(d, err, "Edit Category", JOptionPane.ERROR_MESSAGE);
            } else {
                d.dispose();
                refreshCategoryTable();
            }
        });
        d.add(save, gbc);

        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void deleteSelectedCategory() {
        Category target = getSelectedCategoryFromTable();
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Select a category to delete.", "Delete Category", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this category?\n\n" + target.getCategoryName() + " (" + target.getCategoryId() + ")",
                "Delete Category",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String err = categoryService.deleteCategory(target.getCategoryId());
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Delete Category", JOptionPane.ERROR_MESSAGE);
        } else {
            refreshCategoryTable();
        }
    }

    private void setSelectedActive(boolean active) {
        User u = getSelectedUserFromTable();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Select a user first.", "User Management", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String err = userService.setUserActive(u.getUserId(), active, currentUser.getUserId());
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "User Management", JOptionPane.ERROR_MESSAGE);
        } else {
            refreshUserTable();
        }
    }

    private void deleteSelectedUser() {
        User u = getSelectedUserFromTable();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Select a user to delete.", "Delete User", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentUser.getUserId().equals(u.getUserId())) {
            JOptionPane.showMessageDialog(this,
                    "You cannot delete your own account.\nUse another manager account if this user must be removed.",
                    "Delete User",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String who = u.getFullName() + " (" + u.getEmail() + ", " + u.getUserId() + ")";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete this user? This cannot be undone.\n\n" + who,
                "Delete User",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        String err = userService.deleteUser(u.getUserId(), currentUser.getUserId());
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Delete User", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "User deleted.", "Delete User", JOptionPane.INFORMATION_MESSAGE);
            refreshUserTable();
        }
    }

    private void showAddUserDialog() {
        JDialog d = new JDialog(this, "Add User", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JComboBox<String> role = SharedStyles.createFilterCombo(new String[]{
                "Manager", "Counter Staff", "Technician", "Customer"
        });
        JTextField fullName = SharedStyles.createFilterField(22);
        JTextField email = SharedStyles.createFilterField(22);
        JTextField contact = SharedStyles.createFilterField(22);
        JPasswordField password = new JPasswordField(22);
        JComboBox<String> technicianServiceType = SharedStyles.createFilterCombo(new String[]{
                "Select Service Type",
                "Normal Service",
                "Major Service"
        });
        technicianServiceType.setEnabled(false);
        role.addActionListener(e -> {
            String selectedRole = mapRoleFilter((String) role.getSelectedItem());
            boolean isTechnician = "Technician".equals(selectedRole);
            technicianServiceType.setEnabled(isTechnician);
            if (!isTechnician) {
                technicianServiceType.setSelectedIndex(0);
            }
        });

        int y = 0;
        addDialogRow(d, gbc, y++, "Role:", role);
        addDialogRow(d, gbc, y++, "Full Name:", fullName);
        addDialogRow(d, gbc, y++, "Email:", email);
        addDialogRow(d, gbc, y++, "Contact:", contact);
        addDialogRow(d, gbc, y++, "Password:", password);
        addDialogRow(d, gbc, y++, "Technician Service:", technicianServiceType);

        JButton save = SharedStyles.createActionButton("Save", SharedStyles.BTN_GREEN);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            String rk = mapRoleFilter((String) role.getSelectedItem());
            String selectedTechServiceType = String.valueOf(technicianServiceType.getSelectedItem());
            if (!"Technician".equals(rk)) {
                selectedTechServiceType = "-";
            }
            String err = userService.addUser(rk, fullName.getText(), email.getText(),
                    contact.getText(), new String(password.getPassword()), selectedTechServiceType);
            if (err != null) {
                JOptionPane.showMessageDialog(d, err, "Add User", JOptionPane.ERROR_MESSAGE);
            } else {
                d.dispose();
                refreshUserTable();
            }
        });
        d.add(save, gbc);

        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void showEditUserDialog() {
        User u = getSelectedUserFromTable();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Select a user to edit.", "Edit User", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JDialog d = new JDialog(this, "Edit User", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField idF = SharedStyles.createFilterField(22);
        idF.setText(u.getUserId());
        idF.setEditable(false);
        JTextField fullName = SharedStyles.createFilterField(22);
        fullName.setText(u.getFullName());
        JTextField email = SharedStyles.createFilterField(22);
        email.setText(u.getEmail());
        JTextField contact = SharedStyles.createFilterField(22);
        contact.setText(u.getContact());
        JComboBox<String> status = SharedStyles.createFilterCombo(new String[]{"ACTIVE", "INACTIVE"});
        status.setSelectedItem(u.isActive() ? "ACTIVE" : "INACTIVE");
        JComboBox<String> technicianServiceType = SharedStyles.createFilterCombo(new String[]{
                "Normal Service",
                "Major Service"
        });
        boolean isTechnician = "Technician".equals(u.getRole());
        technicianServiceType.setEnabled(isTechnician);
        String currentServiceType = u.getTechnicianServiceType();
        if (!"Normal Service".equals(currentServiceType) && !"Major Service".equals(currentServiceType)) {
            currentServiceType = "Normal Service";
        }
        technicianServiceType.setSelectedItem(currentServiceType);
        JPasswordField password = new JPasswordField(22);

        int y = 0;
        addDialogRow(d, gbc, y++, "ID:", idF);
        addDialogRow(d, gbc, y++, "Full Name:", fullName);
        addDialogRow(d, gbc, y++, "Email:", email);
        addDialogRow(d, gbc, y++, "Contact:", contact);
        addDialogRow(d, gbc, y++, "Status:", status);
        addDialogRow(d, gbc, y++, "Technician Service:", technicianServiceType);
        addDialogRow(d, gbc, y++, "New Password (optional):", password);

        JButton save = SharedStyles.createActionButton("Update", SharedStyles.BTN_BLUE);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            User copy = userService.findByUserId(u.getUserId());
            if (copy == null) return;
            copy.setFullName(fullName.getText().trim());
            copy.setEmail(email.getText().trim());
            copy.setContact(contact.getText().trim());
            copy.setActive("ACTIVE".equals(status.getSelectedItem()));
            if ("Technician".equals(copy.getRole())) {
                copy.setTechnicianServiceType(String.valueOf(technicianServiceType.getSelectedItem()));
            } else {
                copy.setTechnicianServiceType("-");
            }
            String np = new String(password.getPassword());
            if (ValidationUtil.isNotEmpty(np)) {
                copy.setPassword(np);
            } else {
                copy.setPassword(u.getPassword());
            }
            String err = userService.updateUser(copy, currentUser.getUserId());
            if (err != null) {
                JOptionPane.showMessageDialog(d, err, "Edit User", JOptionPane.ERROR_MESSAGE);
            } else {
                d.dispose();
                refreshUserTable();
            }
        });
        d.add(save, gbc);

        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void addDialogRow(JDialog d, GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        d.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        d.add(field, gbc);
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        String path = f.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) {
            path += ".csv";
            f = new File(path);
        }
        try (FileWriter fw = new FileWriter(f)) {
            for (String line : userService.exportUsersToCsvLines()) {
                fw.write(line);
                fw.write(System.lineSeparator());
            }
            JOptionPane.showMessageDialog(this, "Exported to " + f.getName(), "Export CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Export CSV", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String mapRoleFilter(String display) {
        if (display == null) return "ALL";
        switch (display) {
            case "Counter Staff":
                return "CounterStaff";
            default:
                return display;
        }
    }

    private static String roleDisplay(String role) {
        if ("CounterStaff".equals(role)) return "Counter Staff";
        return role;
    }

}
