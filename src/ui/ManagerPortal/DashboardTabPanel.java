package ui.ManagerPortal;

import model.appointment.Appointment;
import model.payment.Payment;
import model.users.User;
import service_layer.AppointmentService;
import service_layer.PaymentService;
import service_layer.UserService;
import ui.core.Refreshable;
import ui.shared.SharedStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class DashboardTabPanel extends JPanel implements Refreshable {
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final PaymentService paymentService;
    private final Runnable refreshDashboardAction;

    public DashboardTabPanel(UserService userService, AppointmentService appointmentService, Runnable refreshDashboardAction) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.paymentService = new PaymentService();
        this.refreshDashboardAction = refreshDashboardAction;
        setLayout(new BorderLayout(0, 18));
        setBackground(SharedStyles.MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        List<User> allUsers = userService.listAllUsers();
        long normalServiceTechs = allUsers.stream()
                .filter(u -> "Technician".equals(u.getRole()))
                .filter(u -> "Normal Service".equals(u.getTechnicianServiceType()))
                .count();
        long majorServiceTechs = allUsers.stream()
                .filter(u -> "Technician".equals(u.getRole()))
                .filter(u -> "Major Service".equals(u.getTechnicianServiceType()))
                .count();
        List<Appointment> appointments = appointmentService.getAllAppointments();

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel heading = new JLabel("Dashboard");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        topRow.add(heading, BorderLayout.WEST);

        JButton refreshDashboardBtn = SharedStyles.createActionButton("Refresh", SharedStyles.BTN_BLUE);
        refreshDashboardBtn.addActionListener(e -> refreshDashboardAction.run());
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRight.setOpaque(false);
        topRight.add(refreshDashboardBtn);
        topRow.add(topRight, BorderLayout.EAST);
        content.add(topRow, BorderLayout.NORTH);

        JPanel analyticsRow = new JPanel(new GridBagLayout());
        analyticsRow.setOpaque(false);
        List<Payment> payments = paymentService.getAllPayments();
        JPanel yearlyEarningsCard = buildYearlyEarningsStatCard(appointments, payments);
        JPanel technicianDistributionCard = buildTechnicianServiceTypeCard(normalServiceTechs, majorServiceTechs);
        applyResponsiveAnalyticsLayout(analyticsRow, yearlyEarningsCard, technicianDistributionCard);
        analyticsRow.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyResponsiveAnalyticsLayout(analyticsRow, yearlyEarningsCard, technicianDistributionCard);
            }
        });

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        center.add(analyticsRow, BorderLayout.NORTH);
        center.add(buildAppointmentsTableCard(appointments), BorderLayout.CENTER);
        content.add(center, BorderLayout.CENTER);

        JScrollPane rightPanelScroll = new JScrollPane(
                content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        rightPanelScroll.setBorder(null);
        rightPanelScroll.getViewport().setOpaque(false);
        rightPanelScroll.setOpaque(false);
        rightPanelScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(rightPanelScroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel buildTechnicianServiceTypeCard(long normalServiceTechs, long majorServiceTechs) {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(420, 290));

        JLabel title = new JLabel("Technician Distribution");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 12, 0));
        content.setOpaque(false);
        content.add(createMetricTile("Normal Service", String.valueOf(normalServiceTechs), new Color(88, 97, 255)));
        content.add(createMetricTile("Major Service", String.valueOf(majorServiceTechs), new Color(255, 138, 101)));
        card.add(content, BorderLayout.CENTER);

        JLabel total = new JLabel("Total technicians: " + (normalServiceTechs + majorServiceTechs));
        total.setFont(new Font("SansSerif", Font.PLAIN, 12));
        total.setForeground(new Color(95, 98, 110));
        card.add(total, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createMetricTile(String label, String value, Color accent) {
        JPanel tile = new JPanel(new BorderLayout(0, 8));
        tile.setOpaque(true);
        tile.setBackground(new Color(245, 247, 255));
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 240)),
                new EmptyBorder(14, 14, 14, 14)));

        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(new Color(84, 88, 104));

        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 34));
        v.setForeground(accent);

        tile.add(l, BorderLayout.NORTH);
        tile.add(v, BorderLayout.CENTER);
        return tile;
    }

    private JPanel buildYearlyEarningsStatCard(List<Appointment> appointments, List<Payment> payments) {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(12, 0));
        card.setPreferredSize(new Dimension(620, 290));

        JLabel title = new JLabel("General Statistic");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(title, BorderLayout.NORTH);

        int currentYear = YearMonth.now().getYear();
        int[] monthlyTotalEarned = new int[12];
        int[] monthlyEarnedTrend = new int[12];
        double totalEarned = 0.0;
        int yearlyPaidTransactions = 0;
        for (Payment payment : payments) {
            if (!"PAID".equalsIgnoreCase(payment.getStatus())) continue;
            LocalDate paymentDate = parsePaymentDate(payment.getDate());
            if (paymentDate == null || paymentDate.getYear() != currentYear) continue;

            int monthIndex = paymentDate.getMonthValue() - 1;
            int roundedAmount = (int) Math.round(payment.getAmount());
            monthlyTotalEarned[monthIndex] += roundedAmount;
            monthlyEarnedTrend[monthIndex] += 1;
            totalEarned += payment.getAmount();
            yearlyPaidTransactions++;
        }

        JPanel chartAndStats = new JPanel(new BorderLayout(12, 0));
        chartAndStats.setOpaque(false);

        JPanel chartHolder = new JPanel(new BorderLayout(0, 6));
        chartHolder.setOpaque(false);
        chartHolder.add(new MiniChartPanel(monthlyTotalEarned, monthlyEarnedTrend), BorderLayout.CENTER);
        JLabel chartNote = new JLabel("Total earned graph (yearly, from paid transactions)");
        chartNote.setFont(new Font("SansSerif", Font.PLAIN, 11));
        chartNote.setForeground(new Color(120, 125, 142));
        chartHolder.add(chartNote, BorderLayout.SOUTH);
        chartAndStats.add(chartHolder, BorderLayout.CENTER);

        int yearlyTotalBookings = countByYear(appointments, currentYear, null);
        int yearlyCancelledBookings = countByYear(appointments, currentYear, "CANCELLED");

        JPanel statsColumn = new JPanel();
        statsColumn.setOpaque(false);
        statsColumn.setLayout(new BoxLayout(statsColumn, BoxLayout.Y_AXIS));
        statsColumn.setPreferredSize(new Dimension(220, 0));
        statsColumn.add(createMetricLine("Total Earned (Year)", String.format("RM %.2f", totalEarned)));
        statsColumn.add(createMetricLine("Paid Transactions (Year)", String.valueOf(yearlyPaidTransactions)));
        statsColumn.add(createMetricLine("Total Bookings (Year)", String.valueOf(yearlyTotalBookings)));
        statsColumn.add(createMetricLine("Cancelled Bookings (Year)", String.valueOf(yearlyCancelledBookings)));

        chartAndStats.add(statsColumn, BorderLayout.EAST);
        card.add(chartAndStats, BorderLayout.CENTER);
        return card;
    }

    private void applyResponsiveAnalyticsLayout(JPanel analyticsRow, JPanel yearlyEarningsCard, JPanel technicianDistributionCard) {
        analyticsRow.removeAll();
        boolean stackCards = analyticsRow.getWidth() > 0 && analyticsRow.getWidth() < 1120;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        if (stackCards) {
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 12, 0);
            analyticsRow.add(yearlyEarningsCard, gbc);

            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 0, 0);
            analyticsRow.add(technicianDistributionCard, gbc);
        } else {
            gbc.gridy = 0;
            gbc.gridx = 0;
            gbc.weightx = 0.62;
            gbc.insets = new Insets(0, 0, 0, 8);
            analyticsRow.add(yearlyEarningsCard, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.38;
            gbc.insets = new Insets(0, 8, 0, 0);
            analyticsRow.add(technicianDistributionCard, gbc);
        }

        analyticsRow.revalidate();
        analyticsRow.repaint();
    }

    private JPanel createMetricLine(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(2, 0, 4, 0));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(new Color(95, 98, 110));
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 18));
        v.setForeground(SharedStyles.NAV_ACTIVE_TOP);
        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildAppointmentsTableCard(List<Appointment> appointments) {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("All Bookings");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 4, 0));
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Appointment ID", "Customer ID", "Vehicle ID", "Service ID(s)", "Date", "Time", "Status", "Technician ID"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Appointment a : appointments) {
            model.addRow(new Object[]{
                    a.getAppointmentId(), a.getCustomerId(), a.getVehicleId(), a.getServiceId(),
                    a.getDate(), a.getTime(), a.getStatus(), a.getTechnicianId()
            });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        table.setGridColor(new Color(220, 220, 225));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = (String) tbl.getValueAt(row, 6);
                    if ("PENDING".equalsIgnoreCase(status)) c.setBackground(new Color(255, 253, 235));
                    else if ("COMPLETED".equalsIgnoreCase(status)) c.setBackground(new Color(236, 253, 242));
                    else if ("CANCELLED".equalsIgnoreCase(status)) c.setBackground(new Color(255, 242, 242));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel("Search:"));
        JTextField searchField = SharedStyles.createFilterField(20);
        filterRow.add(searchField);
        filterRow.add(new JLabel("Status:"));
        JComboBox<String> statusFilter = SharedStyles.createFilterCombo(new String[]{"ALL", "PENDING", "COMPLETED", "CANCELLED"});
        filterRow.add(statusFilter);
        JButton applyBtn = SharedStyles.createActionButton("Filter", SharedStyles.BTN_BLUE);
        filterRow.add(applyBtn);
        JButton clearBtn = SharedStyles.createActionButton("Clear", SharedStyles.BTN_BLUE);
        filterRow.add(clearBtn);

        Runnable applyFilter = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            String status = String.valueOf(statusFilter.getSelectedItem());
            model.setRowCount(0);
            for (Appointment a : appointments) {
                if (!"ALL".equals(status) && !status.equalsIgnoreCase(a.getStatus())) continue;
                String row = (a.getAppointmentId() + a.getCustomerId() + a.getVehicleId()
                        + a.getServiceId() + a.getDate() + a.getTime()
                        + a.getStatus() + a.getTechnicianId()).toLowerCase();
                if (!keyword.isEmpty() && !row.contains(keyword)) continue;
                model.addRow(new Object[]{
                        a.getAppointmentId(), a.getCustomerId(), a.getVehicleId(),
                        a.getServiceId(), a.getDate(), a.getTime(), a.getStatus(), a.getTechnicianId()
                });
            }
        };
        applyBtn.addActionListener(e -> applyFilter.run());
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            applyFilter.run();
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        tableScroll.setPreferredSize(new Dimension(0, 280));
        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);
        body.add(filterRow, BorderLayout.NORTH);
        body.add(tableScroll, BorderLayout.CENTER);
        JScrollPane outerScroll = new JScrollPane(body, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outerScroll.setBorder(null);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(outerScroll, BorderLayout.CENTER);
        return card;
    }

    private int countByYear(List<Appointment> appointments, int year, String statusFilter) {
        int total = 0;
        for (Appointment a : appointments) {
            LocalDate date = parseDate(a.getDate());
            if (date == null || date.getYear() != year) continue;
            if (statusFilter != null && !statusFilter.equalsIgnoreCase(a.getStatus())) continue;
            total++;
        }
        return total;
    }

    private LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) return null;
        try {
            return LocalDate.parse(rawDate, AppointmentService.DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private LocalDate parsePaymentDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) return null;
        try {
            return LocalDate.parse(rawDate, AppointmentService.DATE_FORMATTER);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(rawDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate();
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    private static class MiniChartPanel extends JPanel {
        private final int[] barSeries;
        private final int[] lineSeries;

        private MiniChartPanel(int[] barSeries, int[] lineSeries) {
            this.barSeries = barSeries;
            this.lineSeries = lineSeries;
            setOpaque(false);
            setPreferredSize(new Dimension(300, 230));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int left = 20;
                int right = w - 10;
                int top = 14;
                int bottom = h - 26;

                g2.setColor(new Color(235, 236, 246));
                for (int i = 0; i < 4; i++) {
                    int y = top + i * (bottom - top) / 3;
                    g2.drawLine(left, y, right, y);
                }

                int max = 1;
                for (int v : barSeries) max = Math.max(max, v);
                for (int v : lineSeries) max = Math.max(max, v);

                int slotW = Math.max(1, (right - left) / 12);
                int barW = Math.max(4, slotW - 6);

                int prevX = -1;
                int prevY = -1;
                for (int i = 0; i < 12; i++) {
                    int barVal = i < barSeries.length ? barSeries[i] : 0;
                    int lineVal = i < lineSeries.length ? lineSeries[i] : 0;
                    int x = left + i * slotW + 3;

                    int barH = (int) ((barVal / (double) max) * (bottom - top));
                    int yBar = bottom - barH;
                    g2.setColor(new Color(206, 212, 247));
                    g2.fillRoundRect(x, yBar, barW, barH, 8, 8);

                    int yPoint = bottom - (int) ((lineVal / (double) max) * (bottom - top));
                    int pointX = x + barW / 2;
                    g2.setColor(new Color(88, 97, 255));
                    g2.fillOval(pointX - 3, yPoint - 3, 6, 6);
                    if (prevX >= 0) g2.drawLine(prevX, prevY, pointX, yPoint);
                    prevX = pointX;
                    prevY = yPoint;
                }

                g2.setColor(new Color(120, 125, 142));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                String[] months = {"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"};
                for (int i = 0; i < 12; i++) {
                    int x = left + i * slotW + slotW / 2;
                    g2.drawString(months[i], x - 3, h - 8);
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
