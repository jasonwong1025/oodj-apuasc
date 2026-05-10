package ui;

import abstracts.AbstractUser;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.users.User;
import service_layer.UserService;
import service_layer.AppointmentService;
import service_layer.ReviewService;
import service_layer.VehicleService;
import ui.TechnicianPortal.*;
import java.util.HashMap;
import java.util.Map;

public class TechnicianDashboard extends JFrame implements Refreshable {

    private final AbstractUser currentUser;
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final ReviewService reviewService;
    private final VehicleService vehicleService;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DefaultListModel<String> navModel;
    private JList<String> navList;
    private JLabel headerWho;

    private final TechnicianContext context;
    private final Map<String, TechnicianTabPanel> tabs = new HashMap<>();

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
        this.appointmentService = new AppointmentService();
        this.reviewService = new ReviewService();
        this.vehicleService = new VehicleService();

        this.context = new TechnicianContext(
            this,
            currentUser,
            userService,
            appointmentService,
            reviewService,
            vehicleService,
            this::refresh
        );

        initializeTabs();

        setTitle("APU-ASC | Technician - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(SharedStyles.MAIN_BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebarAndContent(), BorderLayout.CENTER);
    }

    private void initializeTabs() {
        tabs.put("Dashboard", new DashboardTabPanel(context));
        tabs.put("My Tasks", new MyTasksTabPanel(context));
        tabs.put("Task History", new TaskHistoryTabPanel(context));
        tabs.put("Provide Feedback", new ProvideFeedbackTabPanel(context));
        tabs.put("Customer Reviews", new ReviewsTabPanel(context));
        tabs.put("My Profile", new MyProfileTabPanel(context));
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
        headerWho = new JLabel(currentUser.getFullName() + "  |  " + displayRole);
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

        for (String item : NAV_ITEMS) {
            TechnicianTabPanel panel = tabs.get(item);
            if (panel != null) {
                cardPanel.add(panel, item);
            }
        }

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

        TechnicianTabPanel panel = tabs.get(selected);
        if (panel != null) {
            panel.refresh();
            cardLayout.show(cardPanel, selected);
        }

        // Update header just in case name changed
        User current = userService.findByUserId(currentUser.getUserId());
        if (current != null) {
            String serviceType = current.getTechnicianServiceType();
            String displayRole = "Technician";
            if (serviceType != null && !serviceType.trim().isEmpty() && !serviceType.equals("-")) {
                displayRole += " (" + serviceType + ")";
            }
            headerWho.setText(current.getFullName() + "  |  " + displayRole);
            setTitle("APU-ASC | Technician - " + current.getFullName());
        }
    }
}
