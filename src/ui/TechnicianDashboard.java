package ui;

import abstracts.AbstractUser;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TechnicianDashboard extends JFrame {
    private CardLayout cardLayout;
    private JPanel centerPanel;
    private final Color SIDEBAR_COLOR = new Color(50, 50, 50);
    private final Color SIDEBAR_ACTIVE_COLOR = new Color(0, 120, 215);
    private final Color TOPBAR_COLOR = new Color(240, 240, 240);

    public TechnicianDashboard(AbstractUser user) {
        setTitle("APU Automotive Service Centre - Technician");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Top Bar
        JPanel topBar = createTopBar(user);
        add(topBar, BorderLayout.NORTH);

        // 2. Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 3. Center Content (CardLayout)
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBackground(Color.WHITE);

        centerPanel.add(createJobQueuePanel(), "JobQueue");
        centerPanel.add(createPlaceholderPanel("Feedback Received Content"), "Feedback");
        centerPanel.add(createPlaceholderPanel("My Profile Content"), "Profile");

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createTopBar(AbstractUser user) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TOPBAR_COLOR);
        bar.setPreferredSize(new Dimension(1200, 60));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JLabel titleLabel = new JLabel("  APU Automotive Service Centre", SwingConstants.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        bar.add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        rightPanel.setOpaque(false);

        // Language Dropdown
        String[] languages = {"English", "Malay", "Mandarin"};
        JComboBox<String> langCombo = new JComboBox<>(languages);
        langCombo.setPreferredSize(new Dimension(100, 30));
        rightPanel.add(langCombo);

        // User Info
        JLabel userInfo = new JLabel(user.getFullName().toLowerCase() + " | " + user.getRole().toUpperCase());
        userInfo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        userInfo.setForeground(Color.DARK_GRAY);
        rightPanel.add(userInfo);

        // Logout
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        rightPanel.add(logoutBtn);

        bar.add(rightPanel, BorderLayout.EAST);
        return bar;
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(SIDEBAR_COLOR);
        side.setPreferredSize(new Dimension(220, 800));
        side.setBorder(new EmptyBorder(10, 0, 10, 0));

        side.add(createSideButton("\uD83D\uDD27 My Job Queue", "JobQueue", true)); // Wrench icon
        side.add(createSideButton("\uD83D\uDCAC Feedback Received", "Feedback", false)); // Chat icon
        side.add(createSideButton("\uD83D\uDC64 My Profile", "Profile", false)); // Profile icon

        return side;
    }

    private JButton createSideButton(String text, String cardName, boolean active) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(active ? SIDEBAR_ACTIVE_COLOR : SIDEBAR_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMargin(new Insets(0, 15, 0, 0));
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            // Update UI for all buttons (reset color)
            Container parent = btn.getParent();
            for (Component c : parent.getComponents()) {
                if (c instanceof JButton) {
                    c.setBackground(SIDEBAR_COLOR);
                }
            }
            btn.setBackground(SIDEBAR_ACTIVE_COLOR);
            cardLayout.show(centerPanel, cardName);
        });

        return btn;
    }

    private JPanel createJobQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Tool bar (Search + Buttons)
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tools.setOpaque(false);
        tools.add(new JLabel("Search:"));
        JTextField searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(200, 30));
        tools.add(searchField);

        JButton completeBtn = createActionBtn("Mark as Complete");
        JButton noteBtn = createActionBtn("Add/Edit Notes");
        JButton refreshBtn = createActionBtn("Refresh");

        tools.add(completeBtn);
        tools.add(noteBtn);
        tools.add(refreshBtn);

        panel.add(tools, BorderLayout.NORTH);

        // Table
        String[] columns = {"Appt ID", "Customer", "Vehicle", "Service", "Date & Time", "Status", "Tech Notes"};
        Object[][] data = {}; // Placeholder data
        DefaultTableModel model = new DefaultTableModel(data, columns);
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createActionBtn(String text) {
        JButton b = new JButton(text);
        return b;
    }

    private JPanel createPlaceholderPanel(String text) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.add(new JLabel(text));
        return p;
    }
}
