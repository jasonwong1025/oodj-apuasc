package ui.ManagerPortal;

import model.service.Category;
import service_layer.CategoryService;
import ui.Refreshable;
import ui.SharedStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoriesTabPanel extends JPanel implements Refreshable {
    private final JFrame owner;
    private final CategoryService categoryService;
    private DefaultTableModel categoryTableModel;
    private JTable categoryTable;
    private JTextField categorySearchField;

    public CategoriesTabPanel(JFrame owner, CategoryService categoryService) {
        this.owner = owner;
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
        categorySearchField = SharedStyles.createFilterField(24);
        row1.add(categorySearchField);
        JButton filterBtn = SharedStyles.createActionButton("Filter", SharedStyles.BTN_BLUE);
        filterBtn.addActionListener(e -> refresh());
        row1.add(filterBtn);
        top.add(row1);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        row2.setOpaque(false);
        JButton addBtn = SharedStyles.createActionButton("Add Category", SharedStyles.BTN_GREEN);
        addBtn.addActionListener(e -> showAddCategoryDialog());
        row2.add(addBtn);
        JButton editBtn = SharedStyles.createActionButton("Edit Selected", SharedStyles.BTN_BLUE);
        editBtn.addActionListener(e -> { if (categoryTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(owner); return; } showEditCategoryDialog(); });
        row2.add(editBtn);
        JButton deleteBtn = SharedStyles.createActionButton("Delete Selected", SharedStyles.BTN_RED);
        deleteBtn.addActionListener(e -> { if (categoryTable.getSelectedRow() == -1) { SharedStyles.showSelectionError(owner); return; } deleteSelectedCategory(); });
        row2.add(deleteBtn);
        JButton refreshBtn = SharedStyles.createActionButton("Refresh", SharedStyles.BTN_BLUE);
        refreshBtn.addActionListener(e -> refresh());
        row2.add(refreshBtn);
        top.add(row2);
        add(top, BorderLayout.NORTH);

        String[] cols = {"Category ID", "Category Name"};
        categoryTableModel = new DefaultTableModel(cols, 0) {@Override public boolean isCellEditable(int r, int c) { return false; }};
        categoryTable = new JTable(categoryTableModel);
        categoryTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        categoryTable.setRowHeight(28);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        categoryTable.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        categoryTable.setGridColor(new Color(220, 220, 225));
        categoryTable.setShowGrid(true);
        categoryTable.setFillsViewportHeight(true);
        categoryTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                return c;
            }
        });
        JScrollPane sp = new JScrollPane(categoryTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        add(sp, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        String keyword = categorySearchField == null ? "" : categorySearchField.getText().trim();
        List<Category> rows = categoryService.filter(keyword);
        categoryTableModel.setRowCount(0);
        for (Category c : rows) categoryTableModel.addRow(new Object[]{c.getCategoryId(), c.getCategoryName()});
    }

    private Category getSelectedCategoryFromTable() {
        int r = categoryTable.getSelectedRow();
        if (r < 0) return null;
        String id = String.valueOf(categoryTableModel.getValueAt(r, 0));
        return categoryService.findById(id);
    }

    private void showAddCategoryDialog() {
        JDialog d = new JDialog(owner, "Add Category", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        JTextField nameField = SharedStyles.createFilterField(22);
        addDialogRow(d, gbc, 0, "Category Name:", nameField);
        JButton save = SharedStyles.createActionButton("Save", SharedStyles.BTN_GREEN);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            String err = categoryService.addCategory(nameField.getText().trim());
            if (err != null) JOptionPane.showMessageDialog(d, err, "Add Category", JOptionPane.ERROR_MESSAGE);
            else { d.dispose(); refresh(); }
        });
        d.add(save, gbc);
        d.pack(); d.setLocationRelativeTo(owner); d.setVisible(true);
    }

    private void showEditCategoryDialog() {
        Category target = getSelectedCategoryFromTable();
        if (target == null) { JOptionPane.showMessageDialog(owner, "Select a category to edit.", "Edit Category", JOptionPane.WARNING_MESSAGE); return; }
        JDialog d = new JDialog(owner, "Edit Category", true);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        JTextField idField = SharedStyles.createFilterField(22); idField.setText(target.getCategoryId()); idField.setEditable(false);
        JTextField nameField = SharedStyles.createFilterField(22); nameField.setText(target.getCategoryName());
        int y = 0;
        addDialogRow(d, gbc, y++, "Category ID:", idField);
        addDialogRow(d, gbc, y++, "Category Name:", nameField);
        JButton save = SharedStyles.createActionButton("Update", SharedStyles.BTN_BLUE);
        gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            String err = categoryService.updateCategory(target.getCategoryId(), nameField.getText().trim());
            if (err != null) JOptionPane.showMessageDialog(d, err, "Edit Category", JOptionPane.ERROR_MESSAGE);
            else { d.dispose(); refresh(); }
        });
        d.add(save, gbc);
        d.pack(); d.setLocationRelativeTo(owner); d.setVisible(true);
    }

    private void deleteSelectedCategory() {
        Category target = getSelectedCategoryFromTable();
        if (target == null) { JOptionPane.showMessageDialog(owner, "Select a category to delete.", "Delete Category", JOptionPane.WARNING_MESSAGE); return; }
        int confirm = JOptionPane.showConfirmDialog(owner, "Delete this category?\n\n" + target.getCategoryName() + " (" + target.getCategoryId() + ")", "Delete Category", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        String err = categoryService.deleteCategory(target.getCategoryId());
        if (err != null) JOptionPane.showMessageDialog(owner, err, "Delete Category", JOptionPane.ERROR_MESSAGE);
        else refresh();
    }

    private void addDialogRow(JDialog d, GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = y; d.add(new JLabel(label), gbc);
        gbc.gridx = 1; d.add(field, gbc);
    }
}
