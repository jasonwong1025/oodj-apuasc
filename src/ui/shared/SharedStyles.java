package ui.shared;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
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
        table.setRowHeight(32); // Baseline height
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setReorderingAllowed(false);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(200, 230, 250));
        table.setSelectionForeground(Color.BLACK);
        
        TableCellRenderer multiLineRenderer = new TableCellRenderer() {
            private final JTextArea area = new JTextArea();
            {
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setFont(new Font("SansSerif", Font.PLAIN, 13));
                area.setBorder(new EmptyBorder(8, 12, 8, 12));
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                area.setText(value == null ? "" : value.toString());
                if (isSelected) {
                    area.setBackground(table.getSelectionBackground());
                    area.setForeground(table.getSelectionForeground());
                } else {
                    area.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ZEBRA);
                    area.setForeground(table.getForeground());
                }
                return area;
            }
        };

        table.setDefaultRenderer(Object.class, multiLineRenderer);

        // Dynamic Sizing: Recalculate heights on resize or data change
        table.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateRowHeights(table);
            }
        });

        // Trigger on initial visibility
        table.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && table.isShowing()) {
                updateRowHeights(table);
            }
        });
    }

    public static void updateRowHeights(JTable table) {
        for (int row = 0; row < table.getRowCount(); row++) {
            int maxH = 32; // Minimum baseline
            for (int col = 0; col < table.getColumnCount(); col++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = table.prepareRenderer(renderer, row, col);
                int width = table.getColumnModel().getColumn(col).getWidth();
                comp.setSize(width, 1000); // Fixed width, arbitrary large height
                maxH = Math.max(maxH, comp.getPreferredSize().height);
            }
            if (table.getRowHeight(row) != maxH) {
                table.setRowHeight(row, maxH);
            }
        }
    }

    public static void addFormRow(JPanel p, GridBagConstraints gbc, int y, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = 1;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        p.add(comp, gbc);
    }

    // --- DIALOG & MESSAGE WRAPPERS ---

    public static void showMessage(Component parent, String msg) {
        JOptionPane.showMessageDialog(getWrapperWindow(parent), msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String msg) {
        JOptionPane.showMessageDialog(getWrapperWindow(parent), msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(getWrapperWindow(parent), msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String msg) {
        int res = JOptionPane.showConfirmDialog(getWrapperWindow(parent), msg, "Confirm Action", JOptionPane.YES_NO_OPTION);
        return res == JOptionPane.YES_OPTION;
    }

    public static void showSelectionError(Component parent) {
        showWarning(parent, "Please select a row from the table first to proceed.");
    }

    public static void showValidationError(Component parent, String message) {
        showError(parent, message);
    }

    public static Window getWrapperWindow(Component c) {
        if (c == null) return findShowingFrame();
        if (c instanceof Window && c.isShowing()) return (Window) c;
        Window w = SwingUtilities.getWindowAncestor(c);
        return (w != null && w.isShowing()) ? w : findShowingFrame();
    }

    private static Window findShowingFrame() {
        for (Window w : Window.getWindows()) {
            if (w.isShowing() && w instanceof Frame) return w;
        }
        return null;
    }

    public static void showDialogCentered(JDialog dialog, Component parent) {
        dialog.pack();
        Window owner = getWrapperWindow(parent);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
