package ui.CustomerPortal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.appointment.Appointment;
import model.vehicle.Vehicle;
import ui.SharedStyles;

public class DashboardTabPanel extends CustomerTabPanel {
    public DashboardTabPanel(CustomerContext context) {
        super(context);
        setLayout(new BorderLayout(0, 30));
        setBorder(new EmptyBorder(30, 40, 40, 40));
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel welcome = new JLabel("Hello, " + currentUser().getFullName());
        welcome.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 28));
        topRow.add(welcome, BorderLayout.WEST);
        add(topRow, BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        statsGrid.setOpaque(false);
        List<Vehicle> vehicles = vehicleService().getCustomerVehicles(currentUser().getUserId());
        List<Appointment> appointments = appointmentService().getCustomerAppointments(currentUser().getUserId());
        long pending = appointments.stream().filter(a -> a.getStatus().equals("PENDING")).count();
        statsGrid.add(createStatCard("Registered Vehicles", String.valueOf(vehicles.size())));
        statsGrid.add(createStatCard("Active Appointments", String.valueOf(pending)));
        statsGrid.add(createStatCard("Total Services", String.valueOf(appointments.size())));

        JPanel contentRow = new JPanel(new GridLayout(1, 1, 20, 0));
        contentRow.setOpaque(false);

        JPanel recentCard = SharedStyles.createCardPanel();
        recentCard.setLayout(new BorderLayout(0, 15));
        JLabel recentTitle = new JLabel("Upcoming Appointments");
        recentTitle.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        recentCard.add(recentTitle, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Vehicle", "Service(s)", "Date & Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> upcoming = appointments.stream()
                .filter(a -> a.getStatus().equals("PENDING"))
                .toList();
        for (Appointment a : upcoming) {
            model.addRow(new Object[]{
                    a.getAppointmentId(),
                    resolveVehicleInfo(a.getVehicleId()),
                    resolveServiceNames(a.getServiceId()),
                    a.getDate() + " " + a.getTime(),
                    a.getStatus()
            });
        }

        JTable table = new JTable(model);
        table.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        table.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        table.setGridColor(new Color(220, 220, 225));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                                    boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = String.valueOf(tbl.getValueAt(row, 4));
                    if ("CONFIRMED".equalsIgnoreCase(status)) c.setBackground(new Color(235, 243, 255));
                    else if ("IN PROGRESS".equalsIgnoreCase(status)) c.setBackground(new Color(255, 253, 235));
                    else if ("PENDING".equalsIgnoreCase(status)) c.setBackground(new Color(255, 253, 235));
                    else if ("COMPLETED".equalsIgnoreCase(status)) c.setBackground(new Color(236, 253, 242));
                    else if ("CANCELLED".equalsIgnoreCase(status)) c.setBackground(new Color(255, 242, 242));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });
        recentCard.add(new JScrollPane(table), BorderLayout.CENTER);
        contentRow.add(recentCard);

        JPanel mainCenter = new JPanel(new BorderLayout(0, 20));
        mainCenter.setOpaque(false);
        mainCenter.add(statsGrid, BorderLayout.NORTH);
        mainCenter.add(contentRow, BorderLayout.CENTER);

        add(mainCenter, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout());
        JLabel t = new JLabel(title);
        t.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 15));
        JLabel v = new JLabel(value);
        v.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 36));
        v.setForeground(SharedStyles.NAV_ACTIVE_TOP);
        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }
}
