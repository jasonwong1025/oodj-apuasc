package ui;

import abstracts.AbstractUser;
import model.users.User;
import service_layer.UserService;
import utils.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ManagerDashboard extends JFrame {

    private static final String[] NAV_ITEMS = {
            "User Management",
            "Service Management",
            "All Feedback",
            "Audit Log",
            "Reports",
            "Settings",
            "My Profile"
    };

    private final AbstractUser currentUser;
    private final UserService userService;

    private CardLayout cardLayout;
    private JPanel cardPanel;

    private DefaultTableModel userTableModel;
    private JTable userTable;
    private JTextField userSearchField;
    private JComboBox<String> roleFilterCombo;

    public ManagerDashboard(AbstractUser user) {
        this.currentUser = user;
        this.userService = new UserService();

        setTitle("APU Automotive Service Centre");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ManagerPortalStyles.MAIN_BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebarAndContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(ManagerPortalStyles.HEADER_BG);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel brand = new JLabel("APU Automotive Service Centre");
        brand.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(brand, BorderLayout.WEST);

        JComboBox<String> lang = ManagerPortalStyles.createFilterCombo(new String[]{"English", "Bahasa Melayu"});
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        center.setOpaque(false);
        center.add(lang);
        header.add(center, BorderLayout.CENTER);

        JLabel who = new JLabel(currentUser.getFullName() + "  |  " + roleDisplay(currentUser.getRole()));
        who.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JButton logout = ManagerPortalStyles.createActionButton("Logout", ManagerPortalStyles.BTN_LOGOUT);
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

        DefaultListModel<String> navModel = new DefaultListModel<>();
        for (String s : NAV_ITEMS) {
            navModel.addElement(s);
        }
        JList<String> navList = new JList<>(navModel);
        navList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navList.setBackground(ManagerPortalStyles.SIDEBAR_BG);
        navList.setForeground(ManagerPortalStyles.TEXT_ON_DARK);
        navList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        navList.setFixedCellHeight(46);
        navList.setBorder(new EmptyBorder(12, 0, 12, 0));
        navList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                            boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(12, 20, 12, 16));
                l.setOpaque(true);
                if (isSelected) {
                    l.setBackground(ManagerPortalStyles.NAV_ACTIVE_TOP);
                    l.setForeground(Color.WHITE);
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                } else {
                    l.setBackground(ManagerPortalStyles.SIDEBAR_BG);
                    l.setForeground(ManagerPortalStyles.TEXT_ON_DARK);
                    l.setFont(l.getFont().deriveFont(Font.PLAIN));
                }
                return l;
            }
        });

        JScrollPane navScroll = new JScrollPane(navList);
        navScroll.setBorder(null);
        navScroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(ManagerPortalStyles.SIDEBAR_BG);
        side.setPreferredSize(new Dimension(240, 0));
        side.add(navScroll, BorderLayout.CENTER);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);
        cardPanel.add(buildUserManagementPanel(), "USER");
        cardPanel.add(buildPlaceholderPanel("Service Management", "Configure normal and major service prices (link to data layer next)."), "PRICE");
        cardPanel.add(buildPlaceholderPanel("All Feedback", "View customer and staff feedback (link to data layer next)."), "FEED");
        cardPanel.add(buildPlaceholderPanel("Audit Log", "System audit trail (optional text log under data/)."), "AUDIT");
        cardPanel.add(buildPlaceholderPanel("Reports", "Export analysis summaries (link to appointments/payments next)."), "REPORT");
        cardPanel.add(buildPlaceholderPanel("Settings", "Application preferences."), "SETTINGS");
        cardPanel.add(buildMyProfilePanel(), "PROFILE");

        navList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int i = navList.getSelectedIndex();
            if (i < 0) return;
            switch (i) {
                case 0: cardLayout.show(cardPanel, "USER"); refreshUserTable(); break;
                case 1: cardLayout.show(cardPanel, "PRICE"); break;
                case 2: cardLayout.show(cardPanel, "FEED"); break;
                case 3: cardLayout.show(cardPanel, "AUDIT"); break;
                case 4: cardLayout.show(cardPanel, "REPORT"); break;
                case 5: cardLayout.show(cardPanel, "SETTINGS"); break;
                case 6: cardLayout.show(cardPanel, "PROFILE"); break;
                default: break;
            }
        });
        navList.setSelectedIndex(0);

        wrap.add(side, BorderLayout.WEST);
        wrap.add(cardPanel, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildPlaceholderPanel(String title, String body) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(ManagerPortalStyles.MAIN_BG);
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

    private JPanel buildUserManagementPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(ManagerPortalStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        row1.setOpaque(false);
        row1.add(new JLabel("Search:"));
        userSearchField = ManagerPortalStyles.createFilterField(24);
        row1.add(userSearchField);
        row1.add(new JLabel("Role:"));
        roleFilterCombo = ManagerPortalStyles.createFilterCombo(new String[]{
                "ALL", "Manager", "Counter Staff", "Technician", "Customer"
        });
        row1.add(roleFilterCombo);
        JButton filterBtn = ManagerPortalStyles.createActionButton("Filter", ManagerPortalStyles.BTN_BLUE);
        filterBtn.addActionListener(e -> refreshUserTable());
        row1.add(filterBtn);
        top.add(row1);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        row2.setOpaque(false);
        JButton addBtn = ManagerPortalStyles.createActionButton("Add User", ManagerPortalStyles.BTN_GREEN);
        addBtn.addActionListener(e -> showAddUserDialog());
        row2.add(addBtn);

        JButton editBtn = ManagerPortalStyles.createActionButton("Edit Selected", ManagerPortalStyles.BTN_BLUE);
        editBtn.addActionListener(e -> showEditUserDialog());
        row2.add(editBtn);

        JButton deleteBtn = ManagerPortalStyles.createActionButton("Delete Selected", ManagerPortalStyles.BTN_RED);
        deleteBtn.addActionListener(e -> deleteSelectedUser());
        row2.add(deleteBtn);

        JButton deactBtn = ManagerPortalStyles.createActionButton("Deactivate", ManagerPortalStyles.BTN_ORANGE);
        deactBtn.addActionListener(e -> setSelectedActive(false));
        row2.add(deactBtn);

        JButton reactBtn = ManagerPortalStyles.createActionButton("Reactivate", ManagerPortalStyles.BTN_GREEN);
        reactBtn.addActionListener(e -> setSelectedActive(true));
        row2.add(reactBtn);

        JButton refreshBtn = ManagerPortalStyles.createActionButton("Refresh", ManagerPortalStyles.BTN_BLUE);
        refreshBtn.addActionListener(e -> refreshUserTable());
        row2.add(refreshBtn);

        JButton exportBtn = ManagerPortalStyles.createActionButton("Export CSV", ManagerPortalStyles.BTN_BLUE);
        exportBtn.addActionListener(e -> exportCsv());
        row2.add(exportBtn);

        JButton importBtn = ManagerPortalStyles.createActionButton("Import CSV", ManagerPortalStyles.BTN_GREEN);
        importBtn.addActionListener(e -> importCsv());
        row2.add(importBtn);

        top.add(row2);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Username", "Full Name", "Email", "Contact", "Role", "Status"};
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
        userTable.getTableHeader().setBackground(ManagerPortalStyles.TABLE_HEADER_BG);
        userTable.setGridColor(new Color(220, 220, 225));
        userTable.setShowGrid(true);
        userTable.setFillsViewportHeight(true);
        userTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ManagerPortalStyles.TABLE_ZEBRA);
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
        p.setBackground(ManagerPortalStyles.MAIN_BG);
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

        JTextField fullName = ManagerPortalStyles.createFilterField(28);
        fullName.setText(self.getFullName());
        JTextField email = ManagerPortalStyles.createFilterField(28);
        email.setText(self.getEmail());
        JTextField contact = ManagerPortalStyles.createFilterField(28);
        contact.setText(self.getContact());
        JPasswordField pass = new JPasswordField(28);
        pass.setBorder(userSearchField.getBorder());

        int y = 1;
        addProfileRow(p, gbc, y++, "Full Name:", fullName);
        addProfileRow(p, gbc, y++, "Email:", email);
        addProfileRow(p, gbc, y++, "Contact:", contact);
        addProfileRow(p, gbc, y++, "New Password (optional):", pass);

        JButton save = ManagerPortalStyles.createActionButton("Save Profile", ManagerPortalStyles.BTN_GREEN);
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
                    u.getUsername(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getContact(),
                    roleDisplay(u.getRole()),
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
        String who = u.getFullName() + " (" + u.getUsername() + ", " + u.getUserId() + ")";
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

        JComboBox<String> role = ManagerPortalStyles.createFilterCombo(new String[]{
                "Manager", "Counter Staff", "Technician", "Customer"
        });
        JTextField username = ManagerPortalStyles.createFilterField(22);
        JTextField fullName = ManagerPortalStyles.createFilterField(22);
        JTextField email = ManagerPortalStyles.createFilterField(22);
        JTextField contact = ManagerPortalStyles.createFilterField(22);
        JPasswordField password = new JPasswordField(22);

        int y = 0;
        addDialogRow(d, gbc, y++, "Role:", role);
        addDialogRow(d, gbc, y++, "Username:", username);
        addDialogRow(d, gbc, y++, "Full Name:", fullName);
        addDialogRow(d, gbc, y++, "Email:", email);
        addDialogRow(d, gbc, y++, "Contact:", contact);
        addDialogRow(d, gbc, y++, "Password:", password);

        JButton save = ManagerPortalStyles.createActionButton("Save", ManagerPortalStyles.BTN_GREEN);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            String rk = mapRoleFilter((String) role.getSelectedItem());
            String err = userService.addUser(rk, username.getText(), fullName.getText(), email.getText(),
                    contact.getText(), new String(password.getPassword()));
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

        JTextField idF = ManagerPortalStyles.createFilterField(22);
        idF.setText(u.getUserId());
        idF.setEditable(false);
        JTextField username = ManagerPortalStyles.createFilterField(22);
        username.setText(u.getUsername());
        JTextField fullName = ManagerPortalStyles.createFilterField(22);
        fullName.setText(u.getFullName());
        JTextField email = ManagerPortalStyles.createFilterField(22);
        email.setText(u.getEmail());
        JTextField contact = ManagerPortalStyles.createFilterField(22);
        contact.setText(u.getContact());
        JComboBox<String> status = ManagerPortalStyles.createFilterCombo(new String[]{"ACTIVE", "INACTIVE"});
        status.setSelectedItem(u.isActive() ? "ACTIVE" : "INACTIVE");
        JPasswordField password = new JPasswordField(22);

        int y = 0;
        addDialogRow(d, gbc, y++, "ID:", idF);
        addDialogRow(d, gbc, y++, "Username:", username);
        addDialogRow(d, gbc, y++, "Full Name:", fullName);
        addDialogRow(d, gbc, y++, "Email:", email);
        addDialogRow(d, gbc, y++, "Contact:", contact);
        addDialogRow(d, gbc, y++, "Status:", status);
        addDialogRow(d, gbc, y++, "New Password (optional):", password);

        JButton save = ManagerPortalStyles.createActionButton("Update", ManagerPortalStyles.BTN_BLUE);
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            User copy = userService.findByUserId(u.getUserId());
            if (copy == null) return;
            copy.setUsername(username.getText().trim());
            copy.setFullName(fullName.getText().trim());
            copy.setEmail(email.getText().trim());
            copy.setContact(contact.getText().trim());
            copy.setActive("ACTIVE".equals(status.getSelectedItem()));
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

    private void importCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Import CSV", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int opt = JOptionPane.showOptionDialog(this,
                "Replace all users with file contents, or merge by user ID?",
                "Import CSV",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Replace all", "Merge by ID", "Cancel"},
                "Merge by ID");
        if (opt == 2 || opt == JOptionPane.CLOSED_OPTION) return;
        boolean replaceAll = (opt == 0);
        String err = userService.importUsersFromCsvLines(lines, replaceAll);
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Import CSV", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Import completed.", "Import CSV", JOptionPane.INFORMATION_MESSAGE);
            refreshUserTable();
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
