package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Visual theme for all portals (header + sidebar + action buttons).
 */
public final class SharedStyles {

    public static final Color MAIN_BG = new Color(240, 240, 242);
    public static final Color HEADER_BG = new Color(250, 250, 252);
    public static final Color SIDEBAR_BG = new Color(38, 38, 42);
    public static final Color SIDEBAR_BUTTON = new Color(55, 55, 60);
    public static final Color SIDEBAR_BUTTON_HOVER = new Color(70, 70, 78);
    public static final Color NAV_ACTIVE_TOP = new Color(0, 120, 215);
    public static final Color NAV_ACTIVE_BOTTOM = new Color(0, 90, 170);
    public static final Color TEXT_ON_DARK = new Color(245, 245, 245);
    public static final Color TABLE_HEADER_BG = new Color(220, 220, 225);
    public static final Color TABLE_ZEBRA = new Color(248, 248, 250);

    public static final Color BTN_GREEN = new Color(46, 160, 67);
    public static final Color BTN_BLUE = new Color(0, 120, 215);
    public static final Color BTN_ORANGE = new Color(230, 126, 34);
    public static final Color BTN_RED = new Color(192, 57, 43);
    public static final Color BTN_LOGOUT = new Color(255, 140, 0);

    private SharedStyles() {}

    public static JButton createActionButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        return b;
    }

    public static JTextField createFilterField(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(6, 8, 6, 8)));
        return f;
    }

    public static JComboBox<String> createFilterCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        c.setBackground(Color.WHITE);
        return c;
    }

    public static JPanel createCardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(20, 20, 20, 20)));
        return p;
    }

    public static void applyTableStyle(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setReorderingAllowed(false);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(200, 230, 250));
        table.setSelectionForeground(Color.BLACK);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ZEBRA);
                }
                return c;
            }
        });
    }

    public static void addFormRow(JPanel p, GridBagConstraints gbc, int y, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = 1;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        p.add(comp, gbc);
    }

    public static void showSelectionError(Component parent) {
        JOptionPane.showMessageDialog(parent, 
            "Please select a row from the table first to proceed.", 
            "Selection Required", JOptionPane.WARNING_MESSAGE);
    }

    public static void showValidationError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
