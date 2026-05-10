package ui;

import ui.auth.LoginFrame;
import ui.shared.SharedStyles;
import ui.core.BaseFrame;
import ui.core.Refreshable;

import abstracts.AbstractUser;
import model.users.User;
import service_layer.*;
import ui.TechnicianPortal.*;
import ui.shared.ProfileTabPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TechnicianDashboard extends BaseFrame implements Refreshable {

    private final AbstractUser currentUser;
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final ReviewService reviewService;
    private final VehicleService vehicleService;
    private final ServiceService serviceService;
    private final PaymentService paymentService;
    private final RegistrationService registrationService;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DefaultListModel<String> navModel;
    private JList<String> navList;
    private JLabel headerWho;

    private final TechnicianContext context;
    private final Map<String, JPanel> tabs = new HashMap<>();

    private static final String[] NAV_ITEMS = {
            "Dashboard", "My Tasks", "Task History", "Provide Feedback", "Customer Reviews", "My Profile"
    };

    public TechnicianDashboard(AbstractUser user) {
        super("APU-ASC | Technician - " + user.getFullName());
        this.currentUser = user;
        this.userService = new UserService();
        this.appointmentService = new AppointmentService();
        this.reviewService = new ReviewService();
        this.vehicleService = new VehicleService();
        this.serviceService = new ServiceService();
        this.paymentService = new PaymentService();
        this.registrationService = new RegistrationService();
        FeedbackService feedbackService = new FeedbackService();

        this.context = new TechnicianContext(
            this, currentUser, userService, appointmentService, reviewService,
            vehicleService, serviceService, paymentService, registrationService,
            feedbackService, this::refresh
        );

        initializeTabs();
        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebarAndContent(), BorderLayout.CENTER);
        refresh();
    }

    @Override
    protected void initContent() {
        setLayout(new BorderLayout());
    }

    private void initializeTabs() {
        tabs.put("Dashboard", new DashboardTabPanel(context));
        tabs.put("My Tasks", new MyTasksTabPanel(context));
        tabs.put("Task History", new TaskHistoryTabPanel(context));
        tabs.put("Provide Feedback", new ProvideFeedbackTabPanel(context));
        tabs.put("Customer Reviews", new ReviewsTabPanel(context));
        tabs.put("My Profile", new ProfileTabPanel(context));
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(SharedStyles.HEADER_BG);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        header.add(new JLabel("APU Automotive Service Centre", SwingConstants.LEFT) {{
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

        User current = userService.findByUserId(currentUser.getUserId());
        if (current != null) {
            String serviceType = current.getTechnicianServiceType();
            String displayRole = "Technician" + (serviceType != null && !serviceType.isBlank() && !serviceType.equals("-") ? " (" + serviceType + ")" : "");
            headerWho.setText(current.getFullName() + "  |  " + displayRole);
            setTitle("APU-ASC | Technician - " + current.getFullName());
        }
    }
}
