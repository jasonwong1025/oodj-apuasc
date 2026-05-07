package ui;

import abstracts.AbstractUser;
import model.users.User;
import service_layer.AppointmentService;
import service_layer.CategoryService;
import service_layer.FeedbackService;
import service_layer.PaymentService;
import service_layer.ReviewService;
import service_layer.ServiceService;
import service_layer.UserService;
import ui.ManagerPortal.AllFeedbackTabPanel;
import ui.ManagerPortal.CategoriesTabPanel;
import ui.ManagerPortal.DashboardTabPanel;
import ui.ManagerPortal.MyProfileTabPanel;
import ui.ManagerPortal.ReportsTabPanel;
import ui.ManagerPortal.ServiceCatalogTabPanel;
import ui.ManagerPortal.UserManagementTabPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ManagerDashboard extends JFrame implements Refreshable {
    private static final String[] NAV_ITEMS = {"Dashboard", "User Management", "Service Management", "All Feedback", "Reports", "My Profile"};
    private static final String SVC_HEADER = "Service Management";
    private static final String SVC_CATALOG = "Manage Service Catalog";
    private static final String SVC_CATEGORIES = "Manage Categories";

    private final AbstractUser currentUser;
    private final UserService userService;
    private final ServiceService serviceService;
    private final CategoryService categoryService;
    private final AppointmentService appointmentService;
    private final FeedbackService feedbackService;
    private final ReviewService reviewService;
    private final PaymentService paymentService;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DefaultListModel<String> navModel;
    private JList<String> navList;
    private boolean serviceExpanded = false;
    private boolean updatingNav = false;

    private final DashboardTabPanel dashboardTab;
    private final UserManagementTabPanel userManagementTab;
    private final ServiceCatalogTabPanel serviceCatalogTab;
    private final CategoriesTabPanel categoriesTab;
    private final AllFeedbackTabPanel feedbackTab;
    private final ReportsTabPanel reportsTab;
    private final MyProfileTabPanel profileTab;

    public ManagerDashboard(AbstractUser user) {
        this.currentUser = user;
        this.userService = new UserService();
        this.serviceService = new ServiceService();
        this.categoryService = new CategoryService();
        this.appointmentService = new AppointmentService();
        this.feedbackService = new FeedbackService();
        this.reviewService = new ReviewService();
        this.paymentService = new PaymentService();

        this.dashboardTab = new DashboardTabPanel(userService, appointmentService, this::refresh);
        this.userManagementTab = new UserManagementTabPanel(this, currentUser, userService);
        this.serviceCatalogTab = new ServiceCatalogTabPanel(this, serviceService, categoryService);
        this.categoriesTab = new CategoriesTabPanel(this, categoryService);
        this.feedbackTab = new AllFeedbackTabPanel(feedbackService, reviewService, appointmentService, userService, serviceService);
        this.reportsTab = new ReportsTabPanel(appointmentService, paymentService, serviceService, categoryService, reviewService, userService);
        this.profileTab = new MyProfileTabPanel(this, currentUser, userService);

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
                    if (SVC_HEADER.equals(text)) l.setText(text + (serviceExpanded ? "  \u25BE" : "  \u25B8"));
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

        navList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = navList.locationToIndex(e.getPoint());
                if (idx < 0) return;
                if (SVC_HEADER.equals(navModel.get(idx))) toggleServiceDropdown();
            }
        });
        navList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || updatingNav) return;
            refresh();
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
        cardPanel.add(dashboardTab, "DASHBOARD");
        cardPanel.add(userManagementTab, "USER");
        cardPanel.add(serviceCatalogTab, "SVC_CATALOG");
        cardPanel.add(categoriesTab, "SVC_CATEGORIES");
        cardPanel.add(feedbackTab, "FEED");
        cardPanel.add(reportsTab, "REPORT");
        cardPanel.add(profileTab, "PROFILE");

        wrap.add(side, BorderLayout.WEST);
        wrap.add(cardPanel, BorderLayout.CENTER);
        navList.setSelectedIndex(0);
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
                dashboardTab.refresh();
                cardLayout.show(cardPanel, "DASHBOARD");
                break;
            case "User Management":
                userManagementTab.refresh();
                cardLayout.show(cardPanel, "USER");
                break;
            case SVC_CATALOG:
                serviceCatalogTab.refresh();
                cardLayout.show(cardPanel, "SVC_CATALOG");
                break;
            case SVC_CATEGORIES:
                categoriesTab.refresh();
                cardLayout.show(cardPanel, "SVC_CATEGORIES");
                break;
            case "All Feedback":
                feedbackTab.refresh();
                cardLayout.show(cardPanel, "FEED");
                break;
            case "Reports":
                reportsTab.refresh();
                cardLayout.show(cardPanel, "REPORT");
                break;
            case "My Profile":
                profileTab.refresh();
                cardLayout.show(cardPanel, "PROFILE");
                break;
            default:
                break;
        }
        cardPanel.revalidate();
        cardPanel.repaint();
        User self = userService.findByUserId(currentUser.getUserId());
        if (self != null) setTitle("APU-ASC | Manager - " + self.getFullName());
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

    private static String roleDisplay(String role) {
        if ("CounterStaff".equals(role)) return "Counter Staff";
        return role;
    }
}
