package ui;

import ui.auth.LoginFrame;
import ui.shared.SharedStyles;
import ui.core.BaseFrame;
import ui.core.Refreshable;

import abstracts.AbstractUser;
import model.users.User;
import service_layer.*;
import ui.CustomerPortal.*;
import ui.shared.ProfileTabPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomerDashboard extends BaseFrame implements Refreshable, CustomerNavigator {

    private static final String[] NAV_ITEMS = {
            "Dashboard", "Manage Vehicles", "Book Appointment", "My Appointments", "Service History", "Reviews", "My Profile"
    };

    private final AbstractUser currentUser;
    private final VehicleService vehicleService;
    private final AppointmentService appointmentService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final ServiceService serviceLookup;
    private final UserService userService;

    private final CustomerContext context;
    private final DashboardTabPanel dashboardTab;
    private final VehiclesTabPanel vehiclesTab;
    private final BookingTabPanel bookingTab;
    private final AppointmentsTabPanel appointmentsTab;
    private final ServiceHistoryTabPanel historyTab;
    private final ReviewsTabPanel reviewsTab;
    private final ProfileTabPanel profileTab;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DefaultListModel<String> navModel;
    private JList<String> navList;
    private JLabel headerWho;

    public CustomerDashboard(AbstractUser user) {
        super("APU-ASC | Customer - " + user.getFullName());
        this.currentUser = user;
        this.vehicleService = new VehicleService();
        this.appointmentService = new AppointmentService();
        this.paymentService = new PaymentService();
        this.reviewService = new ReviewService();
        this.serviceLookup = new ServiceService();
        this.userService = new UserService();

        this.context = new CustomerContext(
            this, currentUser, vehicleService, appointmentService, paymentService,
            reviewService, serviceLookup, userService, this::refresh, this
        );

        this.dashboardTab = new DashboardTabPanel(context);
        this.vehiclesTab = new VehiclesTabPanel(context);
        this.bookingTab = new BookingTabPanel(context);
        this.appointmentsTab = new AppointmentsTabPanel(context);
        this.historyTab = new ServiceHistoryTabPanel(context);
        this.reviewsTab = new ReviewsTabPanel(context);
        this.profileTab = new ProfileTabPanel(context);

        init();
        refresh();
    }

    @Override
    protected void initContent() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebarAndContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(SharedStyles.HEADER_BG);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        header.add(new JLabel("APU Automotive Service Centre") {{
            setFont(new Font("SansSerif", Font.BOLD, 18));
        }}, BorderLayout.WEST);

        headerWho = new JLabel("", SwingConstants.RIGHT);
        headerWho.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        JButton logout = SharedStyles.createActionButton("Logout", SharedStyles.BTN_LOGOUT);
        logout.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });

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
        cardPanel.add(dashboardTab, "Dashboard");
        cardPanel.add(vehiclesTab, "Manage Vehicles");
        cardPanel.add(bookingTab, "Book Appointment");
        cardPanel.add(appointmentsTab, "My Appointments");
        cardPanel.add(historyTab, "Service History");
        cardPanel.add(reviewsTab, "Reviews");
        cardPanel.add(profileTab, "My Profile");

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

        JPanel panel = getPanelForNav(selected);
        if (panel instanceof Refreshable refreshable) refreshable.refresh();
        cardLayout.show(cardPanel, selected);

        User current = userService.findByUserId(currentUser.getUserId());
        if (current != null) {
            headerWho.setText(current.getFullName() + "  |  Customer");
            setTitle("APU-ASC | Customer - " + current.getFullName());
        }
    }

    private JPanel getPanelForNav(String selected) {
        return switch (selected) {
            case "Dashboard" -> dashboardTab;
            case "Manage Vehicles" -> vehiclesTab;
            case "Book Appointment" -> bookingTab;
            case "My Appointments" -> appointmentsTab;
            case "Service History" -> historyTab;
            case "Reviews" -> reviewsTab;
            case "My Profile" -> profileTab;
            default -> dashboardTab;
        };
    }

    @Override
    public void navigateTo(String navItem) {
        if (navList != null) navList.setSelectedValue(navItem, true);
    }
}
