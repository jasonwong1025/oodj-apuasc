package ui.ManagerPortal;

import abstracts.AbstractUser;
import model.users.Role;
import model.users.User;
import service_layer.UserService;
import ui.core.Refreshable;
import ui.shared.SharedStyles;
import utils.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class UserManagementTabPanel extends JPanel implements Refreshable {
    private final JFrame owner;
    private final AbstractUser currentUser;
    private final UserService userService;

    private DefaultTableModel userTableModel;
    private JTable userTable;
    private JTextField userSearchField;
    private JComboBox<String> roleFilterCombo;

    public UserManagementTabPanel(JFrame owner, AbstractUser currentUser, UserService userService) {
        this.owner = owner;
        this.currentUser = currentUser;
        this.userService = userService;
        setLayout(new BorderLayout(0, 0));
        setBackground(SharedStyles.MAIN_BG);
        setBorder(new EmptyBorder(16, 20, 20, 20));
        buildUi();
        refresh();
    }

    private void buildUi() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        row1.setOpaque(false);
        row1.add(new JLabel("Search:"));
        userSearchField = SharedStyles.createFilterField(24);
        row1.add(userSearchField);
        row1.add(new JLabel("Role:"));
        roleFilterCombo = SharedStyles.createFilterCombo(new String[]{"ALL", "Manager", "Counter Staff", "Technician", "Customer"});
        row1.add(roleFilterCombo);
        JButton filterBtn = SharedStyles.createActionButton("Filter", SharedStyles.BTN_BLUE);
        filterBtn.addActionListener(e -> refresh());
        row1.add(filterBtn);
        top.add(row1);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        row2.setOpaque(false);
        JButton addBtn = SharedStyles.createActionButton("Add User", SharedStyles.BTN_GREEN);
        addBtn.addActionListener(e -> showAddUserDialog());
        row2.add(addBtn);
        JButton editBtn = SharedStyles.createActionButton("Edit Selected", SharedStyles.BTN_BLUE);
        editBtn.addActionListener(e -> { if (userTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(owner); return; } showEditUserDialog();});
        row2.add(editBtn);
        JButton deleteBtn = SharedStyles.createActionButton("Delete Selected", SharedStyles.BTN_RED);
        deleteBtn.addActionListener(e -> { if (userTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(owner); return; } deleteSelectedUser();});
        row2.add(deleteBtn);
        JButton deactBtn = SharedStyles.createActionButton("Deactivate", SharedStyles.BTN_ORANGE);
        deactBtn.addActionListener(e -> { if (userTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(owner); return; } setSelectedActive(false);});
        row2.add(deactBtn);
        JButton reactBtn = SharedStyles.createActionButton("Reactivate", SharedStyles.BTN_GREEN);
        reactBtn.addActionListener(e -> { if (userTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(owner); return; } setSelectedActive(true);});
        row2.add(reactBtn);
        JButton refreshBtn = SharedStyles.createActionButton("Refresh", SharedStyles.BTN_BLUE);
        refreshBtn.addActionListener(e -> refresh());
        row2.add(refreshBtn);
        JButton exportBtn = SharedStyles.createActionButton("Export CSV", SharedStyles.BTN_BLUE);
        exportBtn.addActionListener(e -> exportCsv());
        row2.add(exportBtn);
        top.add(row2);
        add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Email", "Contact", "Role", "Service Type", "Status"};
        userTableModel = new DefaultTableModel(cols, 0) {@Override public boolean isCellEditable(int r, int c) { return false; }};
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                return c;
            }
        });
        JScrollPane sp = new JScrollPane(userTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        add(sp, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        String roleKey = mapRoleFilter((String) roleFilterCombo.getSelectedItem());
        List<User> rows = userService.filterUsers(userSearchField.getText(), roleKey);
        userTableModel.setRowCount(0);
        for (User u : rows) {
            userTableModel.addRow(new Object[]{
                    u.getUserId(), u.getFullName(), u.getEmail(), u.getContact(), roleDisplay(u.getRole() != null ? u.getRole().getLabel() : ""),
                    u.getRole() == Role.TECHNICIAN ? u.getTechnicianServiceType() : "-",
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
        if (u == null) { JOptionPane.showMessageDialog(owner, "Select a user first.", "User Management", JOptionPane.WARNING_MESSAGE); return; }
        utils.Result<Void> result = userService.setUserActive(u.getUserId(), active, currentUser.getUserId());
        if (result.isFailure()) JOptionPane.showMessageDialog(owner, result.getError(), "User Management", JOptionPane.ERROR_MESSAGE);
        else refresh();
    }

    private void deleteSelectedUser() {
        User u = getSelectedUserFromTable();
        if (u == null) { JOptionPane.showMessageDialog(owner, "Select a user to delete.", "Delete User", JOptionPane.WARNING_MESSAGE); return; }
        if (currentUser.getUserId().equals(u.getUserId())) {
            JOptionPane.showMessageDialog(owner, "You cannot delete your own account.\nUse another manager account if this user must be removed.", "Delete User", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String who = u.getFullName() + " (" + u.getEmail() + ", " + u.getUserId() + ")";
        int confirm = JOptionPane.showConfirmDialog(owner, "Permanently delete this user? This cannot be undone.\n\n" + who, "Delete User", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        utils.Result<Void> result = userService.deleteUser(u.getUserId(), currentUser.getUserId());
        if (result.isFailure()) JOptionPane.showMessageDialog(owner, result.getError(), "Delete User", JOptionPane.ERROR_MESSAGE);
        else { JOptionPane.showMessageDialog(owner, "User deleted.", "Delete User", JOptionPane.INFORMATION_MESSAGE); refresh(); }
    }

    private void showAddUserDialog() {
        JDialog d = new JDialog(owner, "Add User", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JComboBox<String> role = SharedStyles.createFilterCombo(new String[]{"Manager", "Counter Staff", "Technician", "Customer"});
        JTextField fullName = SharedStyles.createFilterField(22);
        JTextField email = SharedStyles.createFilterField(22);
        JTextField contact = SharedStyles.createFilterField(22);
        JPasswordField password = new JPasswordField(22);
        JComboBox<String> technicianServiceType = SharedStyles.createFilterCombo(new String[]{"Select Service Type", "Normal Service", "Major Service"});
        technicianServiceType.setEnabled(false);
        role.addActionListener(e -> {
            String selectedRole = mapRoleFilter((String) role.getSelectedItem());
            boolean isTechnician = "Technician".equals(selectedRole);
            technicianServiceType.setEnabled(isTechnician);
            if (!isTechnician) technicianServiceType.setSelectedIndex(0);
        });

        int y = 0;
        addDialogRow(d, gbc, y++, "Role:", role);
        addDialogRow(d, gbc, y++, "Full Name:", fullName);
        addDialogRow(d, gbc, y++, "Email:", email);
        addDialogRow(d, gbc, y++, "Contact:", contact);
        addDialogRow(d, gbc, y++, "Password:", password);
        addDialogRow(d, gbc, y++, "Technician Service:", technicianServiceType);

        JButton save = SharedStyles.createActionButton("Save", SharedStyles.BTN_GREEN);
        gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            String rk = mapRoleFilter((String) role.getSelectedItem());
            String selectedTechServiceType = String.valueOf(technicianServiceType.getSelectedItem());
            if (!"Technician".equals(rk)) selectedTechServiceType = "-";
            utils.Result<User> result = userService.addUser(rk, fullName.getText(), email.getText(), contact.getText(), new String(password.getPassword()), selectedTechServiceType);
            if (result.isFailure()) JOptionPane.showMessageDialog(d, result.getError(), "Add User", JOptionPane.ERROR_MESSAGE);
            else { d.dispose(); refresh(); }
        });
        d.add(save, gbc);
        d.pack(); d.setLocationRelativeTo(owner); d.setVisible(true);
    }

    private void showEditUserDialog() {
        User u = getSelectedUserFromTable();
        if (u == null) { JOptionPane.showMessageDialog(owner, "Select a user to edit.", "Edit User", JOptionPane.WARNING_MESSAGE); return; }
        JDialog d = new JDialog(owner, "Edit User", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField idF = SharedStyles.createFilterField(22); idF.setText(u.getUserId()); idF.setEditable(false);
        JTextField fullName = SharedStyles.createFilterField(22); fullName.setText(u.getFullName());
        JTextField email = SharedStyles.createFilterField(22); email.setText(u.getEmail());
        JTextField contact = SharedStyles.createFilterField(22); contact.setText(u.getContact());
        JComboBox<String> status = SharedStyles.createFilterCombo(new String[]{"ACTIVE", "INACTIVE"});
        status.setSelectedItem(u.isActive() ? "ACTIVE" : "INACTIVE");
        JComboBox<String> technicianServiceType = SharedStyles.createFilterCombo(new String[]{"Normal Service", "Major Service"});
        boolean isTechnician = u.getRole() == Role.TECHNICIAN;
        technicianServiceType.setEnabled(isTechnician);
        String currentServiceType = u.getTechnicianServiceType();
        if (!"Normal Service".equals(currentServiceType) && !"Major Service".equals(currentServiceType)) currentServiceType = "Normal Service";
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
        gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            User copy = userService.findByUserId(u.getUserId());
            if (copy == null) return;
            copy.setFullName(fullName.getText().trim());
            copy.setEmail(email.getText().trim());
            copy.setContact(contact.getText().trim());
            copy.setActive("ACTIVE".equals(status.getSelectedItem()));
            if (copy.getRole() == Role.TECHNICIAN) copy.setTechnicianServiceType(String.valueOf(technicianServiceType.getSelectedItem()));
            else copy.setTechnicianServiceType("-");
            String np = new String(password.getPassword());
            if (ValidationUtil.isNotEmpty(np)) copy.setPassword(np); else copy.setPassword(u.getPassword());
            utils.Result<Void> result = userService.updateUser(copy, ValidationUtil.isNotEmpty(np) ? np : null);
            if (result.isFailure()) JOptionPane.showMessageDialog(d, result.getError(), "Edit User", JOptionPane.ERROR_MESSAGE);
            else { d.dispose(); refresh(); }
        });
        d.add(save, gbc);
        d.pack(); d.setLocationRelativeTo(owner); d.setVisible(true);
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        if (fc.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        String path = f.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) { path += ".csv"; f = new File(path); }
        try (FileWriter fw = new FileWriter(f)) {
            for (String line : userService.exportUsersToCsvLines()) {
                fw.write(line);
                fw.write(System.lineSeparator());
            }
            JOptionPane.showMessageDialog(owner, "Exported to " + f.getName(), "Export CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage(), "Export CSV", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addDialogRow(JDialog d, GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = y; d.add(new JLabel(label), gbc);
        gbc.gridx = 1; d.add(field, gbc);
    }

    private static String mapRoleFilter(String display) {
        if (display == null) return "ALL";
        if ("Counter Staff".equals(display)) return "CounterStaff";
        return display;
    }

    private static String roleDisplay(String role) {
        if ("CounterStaff".equals(role)) return "Counter Staff";
        return role;
    }
}
