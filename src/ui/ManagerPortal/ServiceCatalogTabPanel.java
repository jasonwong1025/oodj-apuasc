package ui.ManagerPortal;

import model.service.Category;
import model.service.Service;
import service_layer.CategoryService;
import service_layer.ServiceService;
import ui.Refreshable;
import ui.SharedStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ServiceCatalogTabPanel extends JPanel implements Refreshable {
    private final JFrame owner;
    private final ServiceService serviceService;
    private final CategoryService categoryService;

    private DefaultTableModel serviceTableModel;
    private JTable serviceTable;
    private JTextField serviceSearchField;
    private JComboBox<String> serviceCategoryFilter;

    public ServiceCatalogTabPanel(JFrame owner, ServiceService serviceService, CategoryService categoryService) {
        this.owner = owner;
        this.serviceService = serviceService;
        this.categoryService = categoryService;
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
        serviceSearchField = SharedStyles.createFilterField(24);
        row1.add(serviceSearchField);
        row1.add(new JLabel("Category:"));
        serviceCategoryFilter = SharedStyles.createFilterCombo(new String[]{"ALL"});
        row1.add(serviceCategoryFilter);
        JButton filterBtn = SharedStyles.createActionButton("Filter", SharedStyles.BTN_BLUE);
        filterBtn.addActionListener(e -> refresh());
        row1.add(filterBtn);
        top.add(row1);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        row2.setOpaque(false);
        JButton addBtn = SharedStyles.createActionButton("Add Service", SharedStyles.BTN_GREEN);
        addBtn.addActionListener(e -> showAddServiceDialog());
        row2.add(addBtn);
        JButton editBtn = SharedStyles.createActionButton("Edit Selected", SharedStyles.BTN_BLUE);
        editBtn.addActionListener(e -> { if (serviceTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(owner); return; } showEditServiceDialog();});
        row2.add(editBtn);
        JButton deleteBtn = SharedStyles.createActionButton("Delete Selected", SharedStyles.BTN_RED);
        deleteBtn.addActionListener(e -> { if (serviceTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(owner); return; } deleteSelectedService();});
        row2.add(deleteBtn);
        JButton refreshBtn = SharedStyles.createActionButton("Refresh", SharedStyles.BTN_BLUE);
        refreshBtn.addActionListener(e -> refresh());
        row2.add(refreshBtn);
        top.add(row2);
        add(top, BorderLayout.NORTH);

        String[] cols = {"Service ID", "Service Name", "Category", "Price (RM)", "In Normal Service"};
        serviceTableModel = new DefaultTableModel(cols, 0) {@Override public boolean isCellEditable(int r, int c) { return false; }};
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                return c;
            }
        });
        JScrollPane sp = new JScrollPane(serviceTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        add(sp, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        refreshServiceCategoryFilter();
        String keyword = serviceSearchField == null ? "" : serviceSearchField.getText().trim();
        String categoryDisplay = serviceCategoryFilter == null ? "ALL" : String.valueOf(serviceCategoryFilter.getSelectedItem());
        String categoryId = "ALL".equals(categoryDisplay) ? "ALL" : extractCategoryId(categoryDisplay);
        List<Service> rows = serviceService.filter(keyword, categoryId);
        serviceTableModel.setRowCount(0);
        for (Service s : rows) {
            String categoryName = categoryService.getCategoryNameById(s.getCategoryId());
            serviceTableModel.addRow(new Object[]{s.getServiceId(), s.getServiceName(), categoryName != null ? categoryName : s.getCategoryId(), String.format("%.2f", s.getPrice()), s.isIncludedInNormalService() ? "YES" : "NO"});
        }
    }

    private void refreshServiceCategoryFilter() {
        String selected = (String) serviceCategoryFilter.getSelectedItem();
        serviceCategoryFilter.removeAllItems();
        serviceCategoryFilter.addItem("ALL");
        for (Category c : categoryService.listAll()) serviceCategoryFilter.addItem(c.getCategoryId() + " - " + c.getCategoryName());
        if (selected != null) serviceCategoryFilter.setSelectedItem(selected);
    }

    private Service getSelectedServiceFromTable() {
        int r = serviceTable.getSelectedRow();
        if (r < 0) return null;
        String id = String.valueOf(serviceTableModel.getValueAt(r, 0));
        return serviceService.findById(id);
    }

    private void showAddServiceDialog() {
        List<Category> categories = categoryService.listAll();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(owner, "No categories found. Please add categories first under Manage Categories.", "Add Service", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JDialog d = new JDialog(owner, "Add Service", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = SharedStyles.createFilterField(22);
        String[] categoryItems = categories.stream().map(c -> c.getCategoryId() + " - " + c.getCategoryName()).toArray(String[]::new);
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
        gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            String err = serviceService.addService(nameField.getText().trim(), extractCategoryId(String.valueOf(categoryField.getSelectedItem())), priceField.getText().trim(), includeInNormalService.isSelected());
            if (err != null) JOptionPane.showMessageDialog(d, err, "Add Service", JOptionPane.ERROR_MESSAGE);
            else { d.dispose(); refresh(); }
        });
        d.add(save, gbc);
        d.pack(); d.setLocationRelativeTo(owner); d.setVisible(true);
    }

    private void showEditServiceDialog() {
        Service target = getSelectedServiceFromTable();
        if (target == null) { JOptionPane.showMessageDialog(owner, "Select a service to edit.", "Edit Service", JOptionPane.WARNING_MESSAGE); return; }
        List<Category> categories = categoryService.listAll();

        JDialog d = new JDialog(owner, "Edit Service", true);
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
        String[] categoryItems = categories.stream().map(c -> c.getCategoryId() + " - " + c.getCategoryName()).toArray(String[]::new);
        JComboBox<String> categoryField = SharedStyles.createFilterCombo(categoryItems);
        String selectedCategoryDisplay = target.getCategoryId() + " - " + (categoryService.getCategoryNameById(target.getCategoryId()) != null ? categoryService.getCategoryNameById(target.getCategoryId()) : target.getCategoryId());
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
        gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            String err = serviceService.updateService(target.getServiceId(), nameField.getText().trim(), extractCategoryId(String.valueOf(categoryField.getSelectedItem())), priceField.getText().trim(), includeInNormalService.isSelected());
            if (err != null) JOptionPane.showMessageDialog(d, err, "Edit Service", JOptionPane.ERROR_MESSAGE);
            else { d.dispose(); refresh(); }
        });
        d.add(save, gbc);
        d.pack(); d.setLocationRelativeTo(owner); d.setVisible(true);
    }

    private void deleteSelectedService() {
        Service target = getSelectedServiceFromTable();
        if (target == null) { JOptionPane.showMessageDialog(owner, "Select a service to delete.", "Delete Service", JOptionPane.WARNING_MESSAGE); return; }
        int confirm = JOptionPane.showConfirmDialog(owner, "Delete this service?\n\n" + target.getServiceName() + " (" + target.getServiceId() + ")", "Delete Service", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        String err = serviceService.deleteService(target.getServiceId());
        if (err != null) JOptionPane.showMessageDialog(owner, err, "Delete Service", JOptionPane.ERROR_MESSAGE);
        else refresh();
    }

    private void addDialogRow(JDialog d, GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = y; d.add(new JLabel(label), gbc);
        gbc.gridx = 1; d.add(field, gbc);
    }

    private String extractCategoryId(String categoryDisplay) {
        if (categoryDisplay == null) return "";
        int sep = categoryDisplay.indexOf(" - ");
        if (sep < 0) return categoryDisplay.trim();
        return categoryDisplay.substring(0, sep).trim();
    }
}
