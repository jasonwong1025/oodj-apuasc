package ui;

import model.users.User;
import service_layer.AppointmentService;
import service_layer.PaymentService;
import service_layer.ReviewService;
import service_layer.ServiceService;
import service_layer.UserService;
import service_layer.VehicleService;
import ui.CustomerPortal.AppointmentsTabPanel;
import ui.CustomerPortal.BookingTabPanel;
import ui.CustomerPortal.CustomerContext;
import ui.CustomerPortal.CustomerNavigator;
import ui.CustomerPortal.DashboardTabPanel;
import ui.CustomerPortal.MyProfileTabPanel;
import ui.CustomerPortal.ReviewsTabPanel;
import ui.CustomerPortal.ServiceHistoryTabPanel;
import ui.CustomerPortal.VehiclesTabPanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import abstracts.AbstractUser;

public class CustomerDashboard extends JFrame implements Refreshable, CustomerNavigator {

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

    private final CustomerContext context;
    private final DashboardTabPanel dashboardTab;
    private final VehiclesTabPanel vehiclesTab;
    private final BookingTabPanel bookingTab;
    private final AppointmentsTabPanel appointmentsTab;
    private final ServiceHistoryTabPanel historyTab;
    private final ReviewsTabPanel reviewsTab;
    private final MyProfileTabPanel profileTab;

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

        this.context = new CustomerContext(
            this,
            currentUser,
            vehicleService,
            appointmentService,
            paymentService,
            reviewService,
            serviceLookup,
            userService,
            this::refresh,
            this
        );
        this.dashboardTab = new DashboardTabPanel(context);
        this.vehiclesTab = new VehiclesTabPanel(context);
        this.bookingTab = new BookingTabPanel(context);
        this.appointmentsTab = new AppointmentsTabPanel(context);
        this.historyTab = new ServiceHistoryTabPanel(context);
        this.reviewsTab = new ReviewsTabPanel(context);
        this.profileTab = new MyProfileTabPanel(context);

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

        cardPanel.add(dashboardTab, "Dashboard");
        cardPanel.add(vehiclesTab, "Manage Vehicles");
        cardPanel.add(bookingTab, "Book Appointment");
        cardPanel.add(appointmentsTab, "My Appointments");
        cardPanel.add(historyTab, "Service History");
        cardPanel.add(reviewsTab, "Reviews");
        cardPanel.add(profileTab, "My Profile");

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

        JPanel panel = getPanelForNav(selected);
        if (panel instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
        cardLayout.show(cardPanel, selected);

        // Update header just in case name changed
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
        if (navList == null) return;
        navList.setSelectedValue(navItem, true);
    }
}
