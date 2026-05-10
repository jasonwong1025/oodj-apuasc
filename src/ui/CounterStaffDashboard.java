package ui;

import ui.auth.LoginFrame;
import ui.shared.SharedStyles;
import ui.core.BaseFrame;
import ui.core.Refreshable;

import abstracts.AbstractUser;
import service_layer.*;
import ui.CounterStaffPortal.*;
import ui.shared.ProfileTabPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CounterStaffDashboard extends BaseFrame implements Refreshable {

    private final AbstractUser currentUser;
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final VehicleService vehicleService;
    private final ServiceService serviceService;
    private final RegistrationService registrationService;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DefaultListModel<String> navModel;
    private JList<String> navList;

    private final CounterStaffContext context;
    private final Map<String, JPanel> tabs = new HashMap<>();

    private static final String[] NAV_ITEMS = {
            "Dashboard", "Manage Customers", "Manage Appointments", "Process Payment", "Customer Reviews", "My Profile"
    };

    public CounterStaffDashboard(AbstractUser user) {
        super("APU-ASC | Counter Staff - " + user.getFullName());
        this.currentUser = user;
        this.userService = new UserService();
        this.appointmentService = new AppointmentService();
        this.paymentService = new PaymentService();
        this.reviewService = new ReviewService();
        this.vehicleService = new VehicleService();
        this.serviceService = new ServiceService();
        this.registrationService = new RegistrationService();
        FeedbackService feedbackService = new FeedbackService();

        this.context = new CounterStaffContext(
            this, currentUser, userService, appointmentService, paymentService,
            reviewService, vehicleService, serviceService, registrationService,
            feedbackService, this::refresh
        );

        initializeTabs();
        init();
        refresh();
    }

    @Override
    protected void initContent() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebarAndContent(), BorderLayout.CENTER);
    }

    private void initializeTabs() {
        tabs.put("Dashboard", new DashboardTabPanel(context));
        tabs.put("Manage Customers", new ManageCustomersTabPanel(context));
        tabs.put("Manage Appointments", new ManageAppointmentsTabPanel(context));
        tabs.put("Process Payment", new ProcessPaymentTabPanel(context));
        tabs.put("Customer Reviews", new CustomerReviewsTabPanel(context));
        tabs.put("My Profile", new ProfileTabPanel(context));
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(SharedStyles.HEADER_BG);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        header.add(new JLabel("APU Automotive Service Centre") {{
            setFont(new Font("SansSerif", Font.BOLD, 18));
        }}, BorderLayout.WEST);

        JLabel who = new JLabel(currentUser.getFullName() + "  |  Counter Staff");
        who.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        JButton logout = SharedStyles.createActionButton("Logout", SharedStyles.BTN_LOGOUT);
        logout.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });

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
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
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
            JPanel panel = tabs.get(item);
            if (panel != null) cardPanel.add(panel, item);
        }

        navList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) refresh(); });
        navList.setSelectedIndex(0);

        wrap.add(side, BorderLayout.WEST);
        wrap.add(cardPanel, BorderLayout.CENTER);
        return wrap;
    }

    @Override
    public void refresh() {
        String selected = navList.getSelectedValue();
        if (selected == null) return;

        JPanel panel = tabs.get(selected);
        if (panel instanceof Refreshable) ((Refreshable) panel).refresh();
        cardLayout.show(cardPanel, selected);
    }
}
