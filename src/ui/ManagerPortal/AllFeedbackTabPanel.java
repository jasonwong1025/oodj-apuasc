package ui.ManagerPortal;

import model.appointment.Appointment;
import model.service.Service;
import model.users.User;
import service_layer.AppointmentService;
import service_layer.FeedbackService;
import service_layer.ReviewService;
import service_layer.ServiceService;
import service_layer.UserService;
import ui.core.Refreshable;
import ui.shared.SharedStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AllFeedbackTabPanel extends JPanel implements Refreshable {
    private final FeedbackService feedbackService;
    private final ReviewService reviewService;
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final ServiceService serviceService;

    private final List<FeedbackRow> allRows = new ArrayList<>();
    private final List<FeedbackRow> visibleTechnicianRows = new ArrayList<>();
    private final List<FeedbackRow> visibleReviewRows = new ArrayList<>();

    private DefaultTableModel technicianTableModel;
    private DefaultTableModel reviewTableModel;
    private JTable technicianTable;
    private JTable reviewTable;
    private JTextField searchField;
    private JComboBox<String> ratingFilter;
    private JLabel totalEntriesValue;
    private JLabel feedbackCountValue;
    private JLabel reviewCountValue;
    private JLabel avgRatingValue;

    public AllFeedbackTabPanel(FeedbackService feedbackService,
                               ReviewService reviewService,
                               AppointmentService appointmentService,
                               UserService userService,
                               ServiceService serviceService) {
        this.feedbackService = feedbackService;
        this.reviewService = reviewService;
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.serviceService = serviceService;

        setLayout(new BorderLayout(0, 0));
        setBackground(SharedStyles.MAIN_BG);
        setBorder(new EmptyBorder(16, 20, 20, 20));
        buildUi();
        refresh();
    }

    private void buildUi() {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));
        add(card, BorderLayout.CENTER);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("All Feedback");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(title);
        top.add(Box.createVerticalStrut(10));

        JPanel summary = new JPanel(new GridLayout(1, 4, 10, 0));
        summary.setOpaque(false);
        totalEntriesValue = new JLabel("0");
        feedbackCountValue = new JLabel("0");
        reviewCountValue = new JLabel("0");
        avgRatingValue = new JLabel("-");
        summary.add(createStatCard("Total Entries", totalEntriesValue, new Color(0, 120, 215)));
        summary.add(createStatCard("Technician Feedback", feedbackCountValue, new Color(230, 126, 34)));
        summary.add(createStatCard("Customer Reviews", reviewCountValue, new Color(46, 160, 67)));
        summary.add(createStatCard("Average Rating", avgRatingValue, new Color(120, 86, 255)));
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(summary);
        top.add(Box.createVerticalStrut(10));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel("Search:"));
        searchField = SharedStyles.createFilterField(18);
        filterRow.add(searchField);
        filterRow.add(new JLabel("Review Rating:"));
        ratingFilter = SharedStyles.createFilterCombo(new String[]{"ALL", "5", "4", "3", "2", "1", "UNRATED"});
        ratingFilter.setPreferredSize(new Dimension(100, 28));
        filterRow.add(ratingFilter);
        JButton applyBtn = SharedStyles.createActionButton("Apply", SharedStyles.BTN_BLUE);
        applyBtn.addActionListener(e -> applyFilters());
        filterRow.add(applyBtn);
        JButton resetBtn = SharedStyles.createActionButton("Reset", SharedStyles.BTN_ORANGE);
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            ratingFilter.setSelectedItem("ALL");
            applyFilters();
        });
        filterRow.add(resetBtn);
        JButton refreshBtn = SharedStyles.createActionButton("Refresh", SharedStyles.BTN_BLUE);
        refreshBtn.addActionListener(e -> refresh());
        filterRow.add(refreshBtn);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(filterRow);

        card.add(top, BorderLayout.NORTH);

        JTabbedPane splitTabs = new JTabbedPane();
        splitTabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel techPanel = new JPanel(new BorderLayout());
        techPanel.setOpaque(false);
        technicianTableModel = new DefaultTableModel(
                new String[]{"Feedback ID", "Apt ID", "Customer", "Service(s)", "Date & Time", "Technician Feedback"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        technicianTable = new JTable(technicianTableModel);
        configureTableBase(technicianTable);
        technicianTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = technicianTable.rowAtPoint(e.getPoint());
                if (row < 0 || row >= visibleTechnicianRows.size()) return;
                showRowDetailsDialog(visibleTechnicianRows.get(row));
            }
        });
        JScrollPane techScroll = new JScrollPane(technicianTable);
        techScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        techPanel.add(techScroll, BorderLayout.CENTER);

        JPanel reviewPanel = new JPanel(new BorderLayout());
        reviewPanel.setOpaque(false);
        reviewTableModel = new DefaultTableModel(
                new String[]{"Review ID", "Apt ID", "Customer", "Service(s)", "Date & Time", "Rating", "Customer Review"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reviewTable = new JTable(reviewTableModel);
        configureTableBase(reviewTable);
        reviewTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = reviewTable.rowAtPoint(e.getPoint());
                if (row < 0 || row >= visibleReviewRows.size()) return;
                showRowDetailsDialog(visibleReviewRows.get(row));
            }
        });
        JScrollPane reviewScroll = new JScrollPane(reviewTable);
        reviewScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));
        reviewPanel.add(reviewScroll, BorderLayout.CENTER);

        splitTabs.addTab("Technician Feedback", techPanel);
        splitTabs.addTab("Customer Reviews", reviewPanel);
        card.add(splitTabs, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        allRows.clear();

        Map<String, Appointment> appointmentById = new HashMap<>();
        for (Appointment a : appointmentService.getAllAppointments()) {
            appointmentById.put(a.getAppointmentId(), a);
        }

        Map<String, User> userById = new HashMap<>();
        for (User user : userService.listAllUsers()) {
            userById.put(user.getUserId(), user);
        }

        Map<String, Service> serviceById = new HashMap<>();
        for (Service service : serviceService.listAll()) {
            serviceById.put(service.getServiceId(), service);
        }

        for (String[] fbRow : feedbackService.getAllFeedbackRows()) {
            String appointmentId = fbRow[1];
            Appointment appointment = appointmentById.get(appointmentId);
            allRows.add(FeedbackRow.forTechnicianFeedback(
                    fbRow[0],
                    appointmentId,
                    fbRow[2],
                    fbRow[3],
                    appointment,
                    userById,
                    serviceById
            ));
        }

        for (String[] reviewRow : reviewService.getAllReviewRows()) {
            String reviewId = reviewRow[0];
            String appointmentId = reviewRow[1];
            Integer rating = null;
            try {
                rating = Integer.parseInt(reviewRow[3]);
            } catch (NumberFormatException ignored) {
            }
            Appointment appointment = appointmentById.get(appointmentId);
            allRows.add(FeedbackRow.forCustomerReview(
                    reviewId,
                    appointmentId,
                    reviewRow[5],
                    rating,
                    reviewRow[4],
                    appointment,
                    userById,
                    serviceById
            ));
        }

        updateSummaryCards();
        applyFilters();
    }

    private void applyFilters() {
        String keyword = searchField.getText().trim().toLowerCase(Locale.ROOT);
        String rating = String.valueOf(ratingFilter.getSelectedItem());

        technicianTableModel.setRowCount(0);
        reviewTableModel.setRowCount(0);
        visibleTechnicianRows.clear();
        visibleReviewRows.clear();

        for (FeedbackRow row : allRows) {
            String text = (row.source + row.entryId + row.appointmentId + row.customer + row.services + row.date
                    + row.comment + row.feedbackMeta).toLowerCase(Locale.ROOT);
            if (!keyword.isEmpty() && !text.contains(keyword)) continue;

            if ("Technician Feedback".equals(row.source)) {
                technicianTableModel.addRow(new Object[]{
                        row.entryId,
                        row.appointmentId,
                        row.customer,
                        row.services,
                        row.date,
                        row.comment
                });
                visibleTechnicianRows.add(row);
                continue;
            }

            if (!matchesRatingFilter(row, rating)) continue;
            reviewTableModel.addRow(new Object[]{
                    row.entryId,
                    row.appointmentId,
                    row.customer,
                    row.services,
                    row.date,
                    row.rating == null ? "-" : row.rating,
                    row.comment
            });
            visibleReviewRows.add(row);
        }
    }

    private boolean matchesRatingFilter(FeedbackRow row, String ratingFilterValue) {
        if (!"Customer Review".equals(row.source)) {
            return true;
        }
        if ("ALL".equals(ratingFilterValue)) {
            return true;
        }
        if ("UNRATED".equalsIgnoreCase(ratingFilterValue)) {
            return row.rating == null;
        }
        try {
            int selected = Integer.parseInt(ratingFilterValue);
            return row.rating != null && row.rating == selected;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private void updateSummaryCards() {
        int feedbackCount = 0;
        int reviewCount = 0;
        int ratingTotal = 0;

        for (FeedbackRow row : allRows) {
            if ("Technician Feedback".equals(row.source)) {
                feedbackCount++;
            } else if ("Customer Review".equals(row.source)) {
                reviewCount++;
                if (row.rating != null) ratingTotal += row.rating;
            }
        }

        totalEntriesValue.setText(String.valueOf(allRows.size()));
        feedbackCountValue.setText(String.valueOf(feedbackCount));
        reviewCountValue.setText(String.valueOf(reviewCount));
        if (reviewCount == 0) {
            avgRatingValue.setText("-");
        } else {
            avgRatingValue.setText(String.format(Locale.ROOT, "%.2f / 5", ratingTotal / (double) reviewCount));
        }
    }

    private JPanel createStatCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel text = new JLabel(label);
        text.setFont(new Font("SansSerif", Font.PLAIN, 12));
        text.setForeground(new Color(90, 90, 90));
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueLabel.setForeground(accent);

        card.add(text, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void configureTableBase(JTable t) {
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setRowHeight(28);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        t.getTableHeader().setBackground(SharedStyles.TABLE_HEADER_BG);
        t.setGridColor(new Color(220, 220, 225));
        t.setShowGrid(true);
        t.setFillsViewportHeight(true);
        t.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });
    }

    private void showRowDetailsDialog(FeedbackRow row) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Entry Details", true);
        dialog.setLayout(new BorderLayout(0, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(12, 12, 0, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int y = 0;
        addReadOnlyRow(form, gbc, y++, "Source:", row.source);
        addReadOnlyRow(form, gbc, y++, "Entry ID:", row.entryId);
        addReadOnlyRow(form, gbc, y++, "Appointment ID:", row.appointmentId);
        addReadOnlyRow(form, gbc, y++, "Customer:", row.customer);
        addReadOnlyRow(form, gbc, y++, "Service(s):", row.services);
        addReadOnlyRow(form, gbc, y++, "Date & Time:", row.date);
        if ("Customer Review".equals(row.source)) {
            addReadOnlyRow(form, gbc, y++, "Rating:", row.rating == null ? "-" : String.valueOf(row.rating));
        }

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        form.add(new JLabel("Details:"), gbc);
        JTextArea details = new JTextArea(row.comment == null ? "-" : row.comment);
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(new Font("SansSerif", Font.PLAIN, 13));
        details.setBackground(new Color(248, 248, 250));
        JScrollPane detailsScroll = new JScrollPane(details);
        detailsScroll.setPreferredSize(new Dimension(520, 160));
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(detailsScroll, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setOpaque(false);
        JButton closeBtn = SharedStyles.createActionButton("Close", SharedStyles.BTN_BLUE);
        closeBtn.addActionListener(e -> dialog.dispose());
        actions.add(closeBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addReadOnlyRow(JPanel form, GridBagConstraints gbc, int y, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        form.add(new JLabel(label), gbc);

        JTextField field = SharedStyles.createFilterField(32);
        field.setText(value == null || value.trim().isEmpty() ? "-" : value);
        field.setEditable(false);
        field.setBackground(new Color(248, 248, 250));
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    private static String resolveCustomer(Appointment appointment, Map<String, User> userById) {
        if (appointment == null) return "Unknown";
        User customer = userById.get(appointment.getCustomerId());
        if (customer == null) return appointment.getCustomerId();
        return customer.getFullName() + " (" + customer.getUserId() + ")";
    }

    private static String resolveServices(Appointment appointment, Map<String, Service> serviceById) {
        if (appointment == null || appointment.getServiceId() == null || appointment.getServiceId().trim().isEmpty()) {
            return "N/A";
        }
        String[] ids = appointment.getServiceId().split(",");
        List<String> names = new ArrayList<>();
        for (String raw : ids) {
            String id = normalizeLegacyServiceId(raw);
            Service service = serviceById.get(id);
            names.add(service == null ? ("Unknown Service (" + id + ")") : service.getServiceName());
        }
        return String.join(", ", names);
    }

    private static String normalizeLegacyServiceId(String rawId) {
        String id = rawId == null ? "" : rawId.trim().toUpperCase(Locale.ROOT);
        if (id.startsWith("SEV") && id.length() > 3) {
            return "SV" + id.substring(3);
        }
        return id;
    }

    private static final class FeedbackRow {
        private final String source;
        private final String entryId;
        private final String appointmentId;
        private final String customer;
        private final String services;
        private final String date;
        private final Integer rating;
        private final String comment;
        /** Extra searchable text for technician rows (e.g. feedback type). */
        private final String feedbackMeta;

        private FeedbackRow(String source, String entryId, String appointmentId, String customer,
                            String services, String date, Integer rating, String comment, String feedbackMeta) {
            this.source = source;
            this.entryId = entryId;
            this.appointmentId = appointmentId;
            this.customer = customer;
            this.services = services;
            this.date = date;
            this.rating = rating;
            this.comment = comment;
            this.feedbackMeta = feedbackMeta == null ? "" : feedbackMeta;
        }

        private static FeedbackRow forTechnicianFeedback(String feedbackId, String appointmentId, String description,
                                                         String type, Appointment appointment,
                                                         Map<String, User> userById, Map<String, Service> serviceById) {
            String feedbackText = description == null || description.trim().isEmpty()
                    ? "-"
                    : description.trim();
            return new FeedbackRow(
                    "Technician Feedback",
                    feedbackId,
                    appointmentId,
                    resolveCustomer(appointment, userById),
                    resolveServices(appointment, serviceById),
                    type == null || type.trim().isEmpty() ? "-" : type,
                    null,
                    feedbackText,
                    ""
            );
        }

        private static FeedbackRow forCustomerReview(String reviewId, String appointmentId, String reviewDate,
                                                     Integer reviewRating, String reviewDescription,
                                                     Appointment appointment, Map<String, User> userById,
                                                     Map<String, Service> serviceById) {
            String reviewText = reviewDescription == null || reviewDescription.trim().isEmpty()
                    ? "-"
                    : reviewDescription.trim();
            return new FeedbackRow(
                    "Customer Review",
                    reviewId,
                    appointmentId,
                    resolveCustomer(appointment, userById),
                    resolveServices(appointment, serviceById),
                    reviewDate == null || reviewDate.trim().isEmpty() ? "-" : reviewDate.trim(),
                    reviewRating,
                    reviewText,
                    ""
            );
        }
    }
}
