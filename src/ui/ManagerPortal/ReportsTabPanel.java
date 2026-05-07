package ui.ManagerPortal;

import model.appointment.Appointment;
import model.feedback.Review;
import model.payment.Payment;
import model.service.Category;
import model.service.Service;
import model.users.User;
import service_layer.AppointmentService;
import service_layer.CategoryService;
import service_layer.PaymentService;
import service_layer.ReviewService;
import service_layer.ServiceService;
import service_layer.UserService;
import ui.Refreshable;
import ui.SharedStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsTabPanel extends JPanel implements Refreshable {
    private static final String REPORT_REVENUE_BY_CATEGORY = "Revenue by Service Category";
    private static final String REPORT_REVENUE_BY_SERVICE = "Revenue by Service";
    private static final String REPORT_SALES_SUMMARY = "Total Sales Summary";
    private static final String REPORT_TECH_WORKLOAD = "Technician Workload Volume";
    private static final String REPORT_APPOINTMENT_STATUS = "Appointment Status & Cancellation Rate";
    private static final String REPORT_CUSTOMER_SATISFACTION = "Customer Satisfaction Summary";
    private static final String REPORT_TOP_VIP = "Top VIP Customers";

    private static final DateTimeFormatter DATE_ONLY_FORMATTER = AppointmentService.DATE_FORMATTER;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DecimalFormat MONEY_FMT = new DecimalFormat("RM #,##0.00");
    private static final DecimalFormat PCT_FMT = new DecimalFormat("0.0");

    private final AppointmentService appointmentService;
    private final PaymentService paymentService;
    private final ServiceService serviceService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final UserService userService;

    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> periodCombo;
    private JComboBox<String> vipMetricCombo;
    private JButton generateBtn;
    private JButton clearBtn;

    private JLabel summaryLabel;
    private JTable reportTable;
    private DefaultTableModel reportModel;
    private JPanel controlsWrap;
    private JPanel controlsRow;

    public ReportsTabPanel(AppointmentService appointmentService,
                           PaymentService paymentService,
                           ServiceService serviceService,
                           CategoryService categoryService,
                           ReviewService reviewService,
                           UserService userService) {
        this.appointmentService = appointmentService;
        this.paymentService = paymentService;
        this.serviceService = serviceService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.userService = userService;
        setLayout(new BorderLayout(0, 14));
        setBackground(SharedStyles.MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));
        buildUi();
        refresh();
    }

    private void buildUi() {
        JPanel topCard = SharedStyles.createCardPanel();
        topCard.setLayout(new BorderLayout(0, 12));

        JLabel heading = new JLabel("Reports");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        topCard.add(heading, BorderLayout.NORTH);

        controlsWrap = new JPanel(new BorderLayout());
        controlsWrap.setOpaque(false);
        controlsRow = new JPanel(new GridBagLayout());
        controlsRow.setOpaque(false);
        controlsWrap.add(controlsRow, BorderLayout.CENTER);
        topCard.add(controlsWrap, BorderLayout.CENTER);

        reportTypeCombo = SharedStyles.createFilterCombo(new String[]{
                REPORT_REVENUE_BY_CATEGORY,
                REPORT_REVENUE_BY_SERVICE,
                REPORT_SALES_SUMMARY,
                REPORT_TECH_WORKLOAD,
                REPORT_APPOINTMENT_STATUS,
                REPORT_CUSTOMER_SATISFACTION,
                REPORT_TOP_VIP
        });
        periodCombo = SharedStyles.createFilterCombo(new String[]{
                "Last 7 Days", "Last 30 Days", "This Month", "Last Month", "This Year", "All Time"
        });
        vipMetricCombo = SharedStyles.createFilterCombo(new String[]{"By Total Spend", "By Total Visits"});
        generateBtn = SharedStyles.createActionButton("Generate", SharedStyles.BTN_BLUE);
        clearBtn = SharedStyles.createActionButton("Reset", SharedStyles.BTN_ORANGE);

        generateBtn.addActionListener(e -> generateSelectedReport(true));
        clearBtn.addActionListener(e -> resetFilters());
        periodCombo.addActionListener(e -> applyPeriodPreset());
        periodCombo.setSelectedItem("This Month");

        layoutControls();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutControls();
            }
        });

        add(topCard, BorderLayout.NORTH);

        JPanel resultCard = SharedStyles.createCardPanel();
        resultCard.setLayout(new BorderLayout(0, 10));
        summaryLabel = new JLabel("Generate a report to view analytics.");
        summaryLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resultCard.add(summaryLabel, BorderLayout.NORTH);

        reportModel = new DefaultTableModel(new String[]{"Info"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable = new JTable(reportModel);
        SharedStyles.applyTableStyle(reportTable);
        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScroll = new JScrollPane(reportTable);
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);
        resultCard.add(tableScroll, BorderLayout.CENTER);
        add(resultCard, BorderLayout.CENTER);
    }

    private void layoutControls() {
        controlsRow.removeAll();
        boolean compact = getWidth() > 0 && getWidth() < 1160;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = compact ? 1.0 : 0.0;

        int x = 0;
        addControl(gbc, x++, new JLabel("Report"));
        addControl(gbc, x++, reportTypeCombo);
        addControl(gbc, x++, new JLabel("Period"));
        addControl(gbc, x++, periodCombo);
        addControl(gbc, x++, new JLabel("VIP Rank"));
        addControl(gbc, x++, vipMetricCombo);
        addControl(gbc, x++, generateBtn);
        addControl(gbc, x, clearBtn);

        controlsRow.revalidate();
        controlsRow.repaint();
    }

    private void addControl(GridBagConstraints gbc, int gridx, JComponent comp) {
        gbc.gridx = gridx;
        controlsRow.add(comp, gbc);
    }

    private void resetFilters() {
        periodCombo.setSelectedItem("This Month");
        applyPeriodPreset();
        reportTypeCombo.setSelectedIndex(0);
        vipMetricCombo.setSelectedIndex(0);
        generateSelectedReport(false);
    }

    @Override
    public void refresh() {
        applyPeriodPreset();
        generateSelectedReport(false);
    }

    private void applyPeriodPreset() {
        // No-op now that custom/manual date fields are removed.
        controlsRow.revalidate();
        controlsRow.repaint();
    }

    private LocalDate[] computePresetDateRange(String preset) {
        LocalDate now = LocalDate.now();
        LocalDate from;
        LocalDate to;
        switch (preset) {
            case "Last 30 Days":
                from = now.minusDays(29);
                to = now;
                break;
            case "This Month":
                from = YearMonth.now().atDay(1);
                to = YearMonth.now().atEndOfMonth();
                break;
            case "Last Month":
                YearMonth lastMonth = YearMonth.now().minusMonths(1);
                from = lastMonth.atDay(1);
                to = lastMonth.atEndOfMonth();
                break;
            case "This Year":
                from = LocalDate.of(now.getYear(), 1, 1);
                to = LocalDate.of(now.getYear(), 12, 31);
                break;
            case "All Time":
                from = LocalDate.of(2000, 1, 1);
                to = LocalDate.of(2099, 12, 31);
                break;
            case "Last 7 Days":
            default:
                from = now.minusDays(6);
                to = now;
                break;
        }
        return new LocalDate[]{from, to};
    }

    private void generateSelectedReport(boolean showValidationError) {
        String preset = String.valueOf(periodCombo.getSelectedItem());
        LocalDate[] range = computePresetDateRange(preset);
        if (range == null) {
            if (showValidationError) {
                SharedStyles.showWarning(this, "Please select a valid period.");
            }
            return;
        }
        LocalDate fromDate = range[0];
        LocalDate toDate = range[1];

        String report = String.valueOf(reportTypeCombo.getSelectedItem());
        if (REPORT_REVENUE_BY_CATEGORY.equals(report)) {
            renderRevenueByCategory(fromDate, toDate);
        } else if (REPORT_REVENUE_BY_SERVICE.equals(report)) {
            renderRevenueByService(fromDate, toDate);
        } else if (REPORT_SALES_SUMMARY.equals(report)) {
            renderSalesSummary(fromDate, toDate);
        } else if (REPORT_TECH_WORKLOAD.equals(report)) {
            renderTechnicianWorkload(fromDate, toDate);
        } else if (REPORT_APPOINTMENT_STATUS.equals(report)) {
            renderAppointmentStatus(fromDate, toDate);
        } else if (REPORT_CUSTOMER_SATISFACTION.equals(report)) {
            renderCustomerSatisfaction(fromDate, toDate);
        } else if (REPORT_TOP_VIP.equals(report)) {
            renderTopVip(fromDate, toDate);
        }
    }

    private void renderRevenueByCategory(LocalDate fromDate, LocalDate toDate) {
        Map<String, Appointment> appointmentsById = new HashMap<>();
        for (Appointment appointment : appointmentService.getAllAppointments()) {
            appointmentsById.put(appointment.getAppointmentId(), appointment);
        }

        Map<String, Service> servicesById = new HashMap<>();
        for (Service service : serviceService.listAll()) {
            servicesById.put(service.getServiceId(), service);
        }
        Map<String, String> categoryNameById = new HashMap<>();
        for (Category category : categoryService.listAll()) {
            categoryNameById.put(category.getCategoryId(), category.getCategoryName());
        }

        Map<String, Double> revenueByCategory = new HashMap<>();
        Map<String, Integer> requestCountByCategory = new HashMap<>();
        double grandTotal = 0.0;
        int paidAppointments = 0;
        int totalServiceRequests = 0;

        for (Payment payment : paymentService.getAllPayments()) {
            if (!"PAID".equalsIgnoreCase(payment.getStatus())) continue;
            LocalDate paymentDate = parseDateFlexible(payment.getDate());
            if (!isWithinRange(paymentDate, fromDate, toDate)) continue;
            Appointment appointment = appointmentsById.get(payment.getAppointmentId());
            if (appointment == null) continue;

            paidAppointments++;
            double paymentTotal = 0.0;
            Map<String, Double> splitByCategory = new HashMap<>();
            for (String serviceId : splitServiceIds(appointment.getServiceId())) {
                Service service = servicesById.get(serviceId);
                if (service == null) continue;
                paymentTotal += service.getPrice();
                splitByCategory.merge(service.getCategoryId(), service.getPrice(), Double::sum);
                requestCountByCategory.merge(service.getCategoryId(), 1, Integer::sum);
                totalServiceRequests++;
            }
            if (paymentTotal <= 0.0) continue;
            for (Map.Entry<String, Double> entry : splitByCategory.entrySet()) {
                double allocated = payment.getAmount() * (entry.getValue() / paymentTotal);
                revenueByCategory.merge(entry.getKey(), allocated, Double::sum);
                grandTotal += allocated;
            }
        }

        List<Map.Entry<String, Double>> rows = new ArrayList<>(revenueByCategory.entrySet());
        rows.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        setTableColumns("Category ID", "Category Name", "Service Requests", "Revenue", "Share");
        reportModel.setRowCount(0);
        for (Map.Entry<String, Double> row : rows) {
            String categoryId = row.getKey();
            double revenue = row.getValue();
            int requests = requestCountByCategory.getOrDefault(categoryId, 0);
            String share = grandTotal <= 0 ? "0.0%" : PCT_FMT.format((revenue * 100.0) / grandTotal) + "%";
            reportModel.addRow(new Object[]{
                    categoryId,
                    categoryNameById.getOrDefault(categoryId, "Unknown"),
                    requests,
                    MONEY_FMT.format(revenue),
                    share
            });
        }
        summaryLabel.setText("Revenue by category from " + fromDate + " to " + toDate
                + " | Paid appointments: " + paidAppointments
                + " | Service requests: " + totalServiceRequests
                + " | Total: " + MONEY_FMT.format(grandTotal));
    }

    private void renderRevenueByService(LocalDate fromDate, LocalDate toDate) {
        Map<String, Appointment> appointmentsById = new HashMap<>();
        for (Appointment appointment : appointmentService.getAllAppointments()) {
            appointmentsById.put(appointment.getAppointmentId(), appointment);
        }

        Map<String, Service> servicesById = new HashMap<>();
        for (Service service : serviceService.listAll()) {
            servicesById.put(service.getServiceId(), service);
        }

        Map<String, Double> revenueByService = new HashMap<>();
        Map<String, Integer> requestCountByService = new HashMap<>();
        double grandTotal = 0.0;
        int paidAppointments = 0;
        int totalServiceRequests = 0;

        for (Payment payment : paymentService.getAllPayments()) {
            if (!"PAID".equalsIgnoreCase(payment.getStatus())) continue;
            LocalDate paymentDate = parseDateFlexible(payment.getDate());
            if (!isWithinRange(paymentDate, fromDate, toDate)) continue;
            Appointment appointment = appointmentsById.get(payment.getAppointmentId());
            if (appointment == null) continue;

            paidAppointments++;
            double paymentTotal = 0.0;
            Map<String, Double> splitByService = new HashMap<>();
            for (String serviceId : splitServiceIds(appointment.getServiceId())) {
                Service service = servicesById.get(serviceId);
                if (service == null) continue;
                paymentTotal += service.getPrice();
                splitByService.merge(serviceId, service.getPrice(), Double::sum);
                requestCountByService.merge(serviceId, 1, Integer::sum);
                totalServiceRequests++;
            }
            if (paymentTotal <= 0.0) continue;
            for (Map.Entry<String, Double> entry : splitByService.entrySet()) {
                double allocated = payment.getAmount() * (entry.getValue() / paymentTotal);
                revenueByService.merge(entry.getKey(), allocated, Double::sum);
                grandTotal += allocated;
            }
        }

        List<Map.Entry<String, Double>> rows = new ArrayList<>(revenueByService.entrySet());
        rows.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        setTableColumns("Service ID", "Service Name", "Service Requests", "Revenue", "Share");
        reportModel.setRowCount(0);
        for (Map.Entry<String, Double> row : rows) {
            String serviceId = row.getKey();
            double revenue = row.getValue();
            int requests = requestCountByService.getOrDefault(serviceId, 0);
            String share = grandTotal <= 0 ? "0.0%" : PCT_FMT.format((revenue * 100.0) / grandTotal) + "%";
            Service service = servicesById.get(serviceId);
            reportModel.addRow(new Object[]{
                    serviceId,
                    service == null ? "Unknown" : service.getServiceName(),
                    requests,
                    MONEY_FMT.format(revenue),
                    share
            });
        }
        summaryLabel.setText("Revenue by service from " + fromDate + " to " + toDate
                + " | Paid appointments: " + paidAppointments
                + " | Service requests: " + totalServiceRequests
                + " | Total: " + MONEY_FMT.format(grandTotal));
    }

    private void renderSalesSummary(LocalDate fromDate, LocalDate toDate) {
        double total = 0.0;
        int paidTransactions = 0;
        for (Payment payment : paymentService.getAllPayments()) {
            if (!"PAID".equalsIgnoreCase(payment.getStatus())) continue;
            LocalDate date = parseDateFlexible(payment.getDate());
            if (!isWithinRange(date, fromDate, toDate)) continue;
            total += payment.getAmount();
            paidTransactions++;
        }

        long days = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        LocalDate prevTo = fromDate.minusDays(1);
        LocalDate prevFrom = prevTo.minusDays(days - 1);

        double previousTotal = 0.0;
        int previousTransactions = 0;
        for (Payment payment : paymentService.getAllPayments()) {
            if (!"PAID".equalsIgnoreCase(payment.getStatus())) continue;
            LocalDate date = parseDateFlexible(payment.getDate());
            if (!isWithinRange(date, prevFrom, prevTo)) continue;
            previousTotal += payment.getAmount();
            previousTransactions++;
        }

        double delta = total - previousTotal;
        String trend;
        if (delta > 0.001) trend = "UP +" + MONEY_FMT.format(delta);
        else if (delta < -0.001) trend = "DOWN " + MONEY_FMT.format(delta);
        else trend = "FLAT";

        setTableColumns("Metric", "Current Period", "Previous Period");
        reportModel.setRowCount(0);
        reportModel.addRow(new Object[]{"Revenue", MONEY_FMT.format(total), MONEY_FMT.format(previousTotal)});
        reportModel.addRow(new Object[]{"Paid Transactions", String.valueOf(paidTransactions), String.valueOf(previousTransactions)});
        reportModel.addRow(new Object[]{"Date Window", fromDate + " to " + toDate, prevFrom + " to " + prevTo});
        summaryLabel.setText("Sales summary | " + trend + " compared with previous " + days + "-day window.");
    }

    private void renderTechnicianWorkload(LocalDate fromDate, LocalDate toDate) {
        Map<String, Integer> completedByTechnician = new HashMap<>();
        List<User> allUsers = userService.listAllUsers();
        Map<String, LocalDate> paidDateByAppointmentId = new HashMap<>();
        for (Payment payment : paymentService.getAllPayments()) {
            if (!"PAID".equalsIgnoreCase(payment.getStatus())) continue;
            LocalDate paidDate = parseDateFlexible(payment.getDate());
            if (paidDate == null) continue;
            LocalDate existing = paidDateByAppointmentId.get(payment.getAppointmentId());
            if (existing == null || paidDate.isAfter(existing)) {
                paidDateByAppointmentId.put(payment.getAppointmentId(), paidDate);
            }
        }
        for (User user : allUsers) {
            if ("Technician".equalsIgnoreCase(user.getRole())) {
                completedByTechnician.put(user.getUserId(), 0);
            }
        }
        for (Appointment appointment : appointmentService.getAllAppointments()) {
            if (!"COMPLETED".equalsIgnoreCase(appointment.getStatus())) continue;
            // Count completed work if either appointment date OR paid date falls in range.
            LocalDate appointmentDate = parseDateFlexible(appointment.getDate());
            LocalDate paidDate = paidDateByAppointmentId.get(appointment.getAppointmentId());
            boolean inRange = isWithinRange(appointmentDate, fromDate, toDate)
                    || isWithinRange(paidDate, fromDate, toDate);
            if (!inRange) continue;
            String technicianId = appointment.getTechnicianId();
            if (technicianId == null || technicianId.isBlank() || "NONE".equalsIgnoreCase(technicianId)) continue;
            completedByTechnician.merge(technicianId, 1, Integer::sum);
        }

        Map<String, User> usersById = new HashMap<>();
        for (User user : allUsers) {
            usersById.put(user.getUserId(), user);
        }

        List<Map.Entry<String, Integer>> rows = new ArrayList<>(completedByTechnician.entrySet());
        rows.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        int totalJobs = 0;
        for (Map.Entry<String, Integer> row : rows) {
            totalJobs += row.getValue();
        }
        int technicianCount = rows.size();
        double avgJobs = technicianCount == 0 ? 0.0 : (double) totalJobs / technicianCount;

        setTableColumns("Rank", "Technician ID", "Technician Name", "Completed Jobs", "Workload Share", "Vs Team Avg", "Load Status");
        reportModel.setRowCount(0);
        int rank = 1;
        for (Map.Entry<String, Integer> row : rows) {
            User technician = usersById.get(row.getKey());
            int jobs = row.getValue();
            double sharePct = totalJobs == 0 ? 0.0 : (jobs * 100.0) / totalJobs;
            double deltaFromAvg = jobs - avgJobs;
            String loadStatus = classifyLoad(deltaFromAvg);
            reportModel.addRow(new Object[]{
                    rank++,
                    row.getKey(),
                    technician == null ? "Unknown" : technician.getFullName(),
                    jobs,
                    PCT_FMT.format(sharePct) + "%",
                    String.format(Locale.US, "%+.1f", deltaFromAvg),
                    loadStatus
            });
        }
        if (totalJobs == 0) {
            summaryLabel.setText("Technician workload from " + fromDate + " to " + toDate
                    + " | Completed jobs: 0 (try a wider date range if needed).");
        } else {
            int topLoad = rows.isEmpty() ? 0 : rows.get(0).getValue();
            int lowLoad = rows.isEmpty() ? 0 : rows.get(rows.size() - 1).getValue();
            boolean fair = (topLoad - lowLoad) <= 1;
            String fairness = fair ? "Balanced" : "Uneven";
            summaryLabel.setText("Technician workload from " + fromDate + " to " + toDate
                    + " | Completed jobs: " + totalJobs
                    + " | Team avg: " + String.format(Locale.US, "%.1f", avgJobs)
                    + " | Distribution: " + fairness);
        }
    }

    private String classifyLoad(double deltaFromAvg) {
        if (deltaFromAvg >= 1.0) return "Overloaded";
        if (deltaFromAvg <= -1.0) return "Underloaded";
        return "Balanced";
    }

    private void renderAppointmentStatus(LocalDate fromDate, LocalDate toDate) {
        Map<String, Integer> byStatus = new LinkedHashMap<>();
        byStatus.put("COMPLETED", 0);
        byStatus.put("CONFIRMED", 0);
        byStatus.put("IN PROGRESS", 0);
        byStatus.put("PENDING", 0);
        byStatus.put("CANCELLED", 0);

        int total = 0;
        for (Appointment appointment : appointmentService.getAllAppointments()) {
            LocalDate date = parseDateFlexible(appointment.getDate());
            if (!isWithinRange(date, fromDate, toDate)) continue;
            total++;
            String status = normalizeStatus(appointment.getStatus());
            if (!byStatus.containsKey(status)) {
                byStatus.put(status, 0);
            }
            byStatus.put(status, byStatus.get(status) + 1);
        }

        setTableColumns("Status", "Count", "Percentage");
        reportModel.setRowCount(0);
        int cancelled = byStatus.getOrDefault("CANCELLED", 0);
        for (Map.Entry<String, Integer> row : byStatus.entrySet()) {
            String pct = total == 0 ? "0.0%" : PCT_FMT.format((row.getValue() * 100.0) / total) + "%";
            reportModel.addRow(new Object[]{row.getKey(), row.getValue(), pct});
        }
        String cancelledRate = total == 0 ? "0.0%" : PCT_FMT.format((cancelled * 100.0) / total) + "%";
        if (total == 0) {
            LocalDate minDate = null;
            LocalDate maxDate = null;
            for (Appointment appointment : appointmentService.getAllAppointments()) {
                LocalDate date = parseDateFlexible(appointment.getDate());
                if (date == null) continue;
                if (minDate == null || date.isBefore(minDate)) minDate = date;
                if (maxDate == null || date.isAfter(maxDate)) maxDate = date;
            }
            if (minDate != null && maxDate != null) {
                summaryLabel.setText("Appointment status mix from " + fromDate + " to " + toDate
                        + " | Total: 0 | Cancellation rate: 0.0%"
                        + " | Available appointment dates: " + minDate + " to " + maxDate
                        + " (try This Month / All Time).");
            } else {
                summaryLabel.setText("Appointment status mix from " + fromDate + " to " + toDate
                        + " | Total: 0 | Cancellation rate: 0.0% | No appointment data found.");
            }
        } else {
            summaryLabel.setText("Appointment status mix from " + fromDate + " to " + toDate
                    + " | Total: " + total + " | Cancellation rate: " + cancelledRate);
        }
    }

    private void renderCustomerSatisfaction(LocalDate fromDate, LocalDate toDate) {
        int[] countByStar = new int[6];
        for (Review review : reviewService.getAllReviews()) {
            LocalDate date = parseDateFlexible(review.getDateTime());
            if (!isWithinRange(date, fromDate, toDate)) continue;
            int rating = review.getRating();
            if (rating >= 1 && rating <= 5) {
                countByStar[rating]++;
            }
        }

        int total = 0;
        for (int s = 1; s <= 5; s++) {
            total += countByStar[s];
        }

        double avg = 0.0;
        if (total > 0) {
            for (int s = 1; s <= 5; s++) {
                avg += (double) s * countByStar[s];
            }
            avg /= total;
        }

        setTableColumns("Rating Tier", "Number of Reviews", "Percentage of Total");
        reportModel.setRowCount(0);
        for (int stars = 5; stars >= 1; stars--) {
            int count = countByStar[stars];
            String tier = stars == 1 ? "1 Star" : stars + " Stars";
            String pct = total == 0 ? "0.0%" : PCT_FMT.format((count * 100.0) / total) + "%";
            reportModel.addRow(new Object[]{tier, count, pct});
        }

        summaryLabel.setText("Satisfaction from " + fromDate + " to " + toDate
                + " | Avg rating: " + String.format(Locale.US, "%.2f", avg) + "/5.00"
                + " | Total reviews: " + total
                + " | Use \"All Feedback\" to investigate low-tier spikes.");
    }

    private void renderTopVip(LocalDate fromDate, LocalDate toDate) {
        boolean bySpend = "By Total Spend".equals(String.valueOf(vipMetricCombo.getSelectedItem()));
        Map<String, Appointment> appointmentById = new HashMap<>();
        for (Appointment appointment : appointmentService.getAllAppointments()) {
            appointmentById.put(appointment.getAppointmentId(), appointment);
        }
        Map<String, User> usersById = new HashMap<>();
        for (User user : userService.listAllUsers()) {
            usersById.put(user.getUserId(), user);
        }

        Map<String, Integer> visitsByCustomer = new HashMap<>();
        Map<String, Double> spendByCustomer = new HashMap<>();

        for (Appointment appointment : appointmentService.getAllAppointments()) {
            if (!"COMPLETED".equalsIgnoreCase(appointment.getStatus())) continue;
            LocalDate date = parseDateFlexible(appointment.getDate());
            if (!isWithinRange(date, fromDate, toDate)) continue;
            visitsByCustomer.merge(appointment.getCustomerId(), 1, Integer::sum);
        }

        for (Payment payment : paymentService.getAllPayments()) {
            if (!"PAID".equalsIgnoreCase(payment.getStatus())) continue;
            LocalDate date = parseDateFlexible(payment.getDate());
            if (!isWithinRange(date, fromDate, toDate)) continue;
            Appointment appointment = appointmentById.get(payment.getAppointmentId());
            if (appointment == null) continue;
            spendByCustomer.merge(appointment.getCustomerId(), payment.getAmount(), Double::sum);
        }

        List<String> customerIds = new ArrayList<>(usersById.keySet());
        customerIds.removeIf(id -> {
            User user = usersById.get(id);
            return user == null || !"Customer".equalsIgnoreCase(user.getRole());
        });

        customerIds.sort((a, b) -> {
            if (bySpend) {
                return Double.compare(spendByCustomer.getOrDefault(b, 0.0), spendByCustomer.getOrDefault(a, 0.0));
            }
            return Integer.compare(visitsByCustomer.getOrDefault(b, 0), visitsByCustomer.getOrDefault(a, 0));
        });

        setTableColumns("Rank", "Customer ID", "Customer Name", "Visits", "Total Spend");
        reportModel.setRowCount(0);
        int rank = 1;
        int limit = 10;
        for (String customerId : customerIds) {
            if (rank > limit) break;
            int visits = visitsByCustomer.getOrDefault(customerId, 0);
            double spend = spendByCustomer.getOrDefault(customerId, 0.0);
            if (visits <= 0 && spend <= 0.0) continue;
            reportModel.addRow(new Object[]{
                    rank++,
                    customerId,
                    usersById.get(customerId).getFullName(),
                    visits,
                    MONEY_FMT.format(spend)
            });
        }
        summaryLabel.setText("Top VIP customers from " + fromDate + " to " + toDate
                + " | Ranking mode: " + (bySpend ? "Spend" : "Visits"));
    }

    private void setTableColumns(String... columns) {
        reportModel.setColumnIdentifiers(columns);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return "UNKNOWN";
        return status.trim().toUpperCase(Locale.ENGLISH);
    }

    private List<String> splitServiceIds(String csv) {
        List<String> ids = new ArrayList<>();
        if (csv == null || csv.isBlank()) return ids;
        String[] parts = csv.split(",");
        for (String part : parts) {
            String id = part.trim();
            if (!id.isEmpty()) ids.add(id);
        }
        return ids;
    }

    private boolean isWithinRange(LocalDate date, LocalDate fromDate, LocalDate toDate) {
        return date != null && !date.isBefore(fromDate) && !date.isAfter(toDate);
    }

    private LocalDate parseDateFlexible(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String trimmed = raw.trim();
        try {
            return LocalDate.parse(trimmed, DATE_ONLY_FORMATTER);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(trimmed, DATE_TIME_FORMATTER).toLocalDate();
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }
}
