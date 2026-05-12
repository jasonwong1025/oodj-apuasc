package ui.CounterStaffPortal;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.appointment.Appointment;
import ui.shared.SharedStyles;

public class DashboardTabPanel extends CounterStaffTabPanel {

    public DashboardTabPanel(CounterStaffContext context) {
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

        JLabel welcome =
                new JLabel("Welcome, "
                        + currentUser().getFullName());

        welcome.setFont(
                new Font("SansSerif", Font.BOLD, 28));

        topRow.add(welcome, BorderLayout.WEST);

        add(topRow, BorderLayout.NORTH);

        List<Appointment> appointments =
                context.appointmentService().getAllAppointments();

        int totalCustomers =
                context.userService().getAllCustomers().size();

        long pending =
                appointments.stream()
                        .filter(a ->
                                "PENDING".equalsIgnoreCase(a.getStatus()))
                        .count();

        long completed =
                appointments.stream()
                        .filter(a ->
                                "COMPLETED".equalsIgnoreCase(a.getStatus()))
                        .count();

        JPanel statsGrid =
                new JPanel(new GridLayout(1, 4, 20, 0));

        statsGrid.setOpaque(false);

        statsGrid.add(createStatCard(
                "Total Customers",
                String.valueOf(totalCustomers)
        ));

        statsGrid.add(createStatCard(
                "Appointments",
                String.valueOf(appointments.size())
        ));

        statsGrid.add(createStatCard(
                "Pending",
                String.valueOf(pending)
        ));

        statsGrid.add(createStatCard(
                "Completed",
                String.valueOf(completed)
        ));

        JPanel contentRow =
                new JPanel(new GridLayout(1, 1, 20, 0));

        contentRow.setOpaque(false);

        JPanel recentCard =
                SharedStyles.createCardPanel();

        recentCard.setLayout(
                new BorderLayout(0, 15));

        JLabel recentTitle =
                new JLabel("Recent Appointments");

        recentTitle.setFont(
                new Font("SansSerif", Font.BOLD, 18));

        recentCard.add(recentTitle, BorderLayout.NORTH);

        String[] cols = {
                "Appointment ID",
                "Customer",
                "Date & Time",
                "Status"
        };

        DefaultTableModel model =
                new DefaultTableModel(cols, 0) {

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        List<Appointment> latest =
                appointments.stream()
                        .sorted((a1, a2) -> {

                            String d1 =
                                    a1.getDate()
                                            + " "
                                            + a1.getTime();

                            String d2 =
                                    a2.getDate()
                                            + " "
                                            + a2.getTime();

                            return d2.compareTo(d1);
                        })
                        .limit(8)
                        .toList();

        for (Appointment a : latest) {

            model.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getCustomerId(),
                    a.getDate() + " " + a.getTime(),
                    a.getStatus()
            });
        }

        JTable table = new JTable(model);

        table.setFont(
                new Font("SansSerif",
                        Font.PLAIN,
                        13));

        table.setRowHeight(28);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        table.getTableHeader().setFont(
                new Font("SansSerif",
                        Font.BOLD,
                        13));

        table.getTableHeader().setBackground(
                SharedStyles.TABLE_HEADER_BG);

        table.setGridColor(
                new Color(220, 220, 225));

        table.setShowGrid(true);

        table.setFillsViewportHeight(true);

        table.setDefaultRenderer(
                Object.class,
                new javax.swing.table.DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable tbl,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column) {

                        Component c =
                                super.getTableCellRendererComponent(
                                        tbl,
                                        value,
                                        isSelected,
                                        hasFocus,
                                        row,
                                        column);

                        if (!isSelected) {

                            String status =
                                    String.valueOf(
                                            tbl.getValueAt(row, 3));

                            if ("CONFIRMED".equalsIgnoreCase(status)) {

                                c.setBackground(
                                        new Color(235, 243, 255));

                            } else if ("IN PROGRESS".equalsIgnoreCase(status)) {

                                c.setBackground(
                                        new Color(255, 253, 235));

                            } else if ("PENDING".equalsIgnoreCase(status)) {

                                c.setBackground(
                                        new Color(255, 253, 235));

                            } else if ("COMPLETED".equalsIgnoreCase(status)) {

                                c.setBackground(
                                        new Color(236, 253, 242));

                            } else if ("CANCELLED".equalsIgnoreCase(status)) {

                                c.setBackground(
                                        new Color(255, 242, 242));

                            } else {

                                c.setBackground(
                                        row % 2 == 0
                                                ? Color.WHITE
                                                : SharedStyles.TABLE_ZEBRA);
                            }
                        }

                        return c;
                    }
                });

        recentCard.add(
                new JScrollPane(table),
                BorderLayout.CENTER);

        contentRow.add(recentCard);

        JPanel mainCenter =
                new JPanel(new BorderLayout(0, 20));

        mainCenter.setOpaque(false);

        mainCenter.add(statsGrid, BorderLayout.NORTH);

        mainCenter.add(contentRow, BorderLayout.CENTER);

        add(mainCenter, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createStatCard(
            String title,
            String value) {

        JPanel card =
                SharedStyles.createCardPanel();

        card.setLayout(new BorderLayout());

        JLabel t = new JLabel(title);

        t.setFont(
                new Font("SansSerif",
                        Font.PLAIN,
                        15));

        JLabel v = new JLabel(value);

        v.setFont(
                new Font("SansSerif",
                        Font.BOLD,
                        36));

        v.setForeground(
                SharedStyles.NAV_ACTIVE_TOP);

        card.add(t, BorderLayout.NORTH);

        card.add(v, BorderLayout.CENTER);

        return card;
    }
}