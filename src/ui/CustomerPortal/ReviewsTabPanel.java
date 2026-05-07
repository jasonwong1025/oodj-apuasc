package ui.CustomerPortal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.appointment.Appointment;
import model.feedback.Review;
import ui.SharedStyles;

public class ReviewsTabPanel extends CustomerTabPanel {
    public ReviewsTabPanel(CustomerContext context) {
        super(context);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(16, 20, 20, 20));
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();

        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Reviews");
        title.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        JButton reviewBtn = SharedStyles.createActionButton("Write Review", SharedStyles.BTN_BLUE);
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRight.setOpaque(false);
        topRight.add(reviewBtn);
        top.add(title, BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Vehicle", "Service Name(s)", "Date", "Status", "Rating", "Comment", "Review Date & time"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> list = appointmentService().getCustomerAppointments(currentUser().getUserId());
        List<Review> reviews = reviewService().getCustomerReviews(currentUser().getUserId());
        java.util.Map<String, Review> reviewByAppointment = new java.util.HashMap<>();
        for (Review r : reviews) {
            reviewByAppointment.put(r.getAppointmentId(), r);
        }

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel("Search:"));
        JTextField searchField = SharedStyles.createFilterField(10);
        filterRow.add(searchField);
        filterRow.add(new JLabel("Status:"));
        JComboBox<String> statusFilter = SharedStyles.createFilterCombo(new String[]{"ALL", "PENDING", "REVIEWED"});
        statusFilter.setPreferredSize(new java.awt.Dimension(90, 28));
        filterRow.add(statusFilter);
        filterRow.add(new JLabel("Rating:"));
        JComboBox<String> ratingFilter = SharedStyles.createFilterCombo(new String[]{"ALL", "1", "2", "3", "4", "5", "UNRATED"});
        ratingFilter.setPreferredSize(new java.awt.Dimension(80, 28));
        filterRow.add(ratingFilter);

        final String[] fromDateValue = {""};
        final String[] toDateValue = {""};
        JButton rangeBtn = SharedStyles.createActionButton("Select Date", SharedStyles.BTN_BLUE);
        rangeBtn.setPreferredSize(new java.awt.Dimension(140, 28));
        filterRow.add(new JLabel("Date:"));
        filterRow.add(rangeBtn);

        Runnable applyFilter = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            String status = String.valueOf(statusFilter.getSelectedItem());
            String rating = String.valueOf(ratingFilter.getSelectedItem());
            LocalDate fromDate = parseDate(fromDateValue[0]);
            LocalDate toDate = parseDate(toDateValue[0]);

            model.setRowCount(0);
            for (Appointment a : list) {
                if (!"COMPLETED".equalsIgnoreCase(a.getStatus())) continue;
                if (!paymentService().isPaid(a.getAppointmentId())) continue;
                Review review = reviewByAppointment.get(a.getAppointmentId());
                boolean reviewed = review != null;
                String rowStatus = reviewed ? "Reviewed" : "Pending";
                String ratingValue = reviewed ? String.valueOf(review.getRating()) : "-";
                String comment = reviewed
                        ? ((review.getDescription() == null || review.getDescription().trim().isEmpty()) ? "-" : review.getDescription())
                        : "-";
                String reviewDate = reviewed ? review.getDate() : "-";

                if (!"ALL".equals(status) && !status.equalsIgnoreCase(rowStatus)) continue;
                if (!"ALL".equals(rating)) {
                    if ("UNRATED".equalsIgnoreCase(rating)) {
                        if (reviewed) continue;
                    } else if (!rating.equals(ratingValue)) {
                        continue;
                    }
                }

                if (fromDate != null || toDate != null) {
                    try {
                        LocalDate apptDate = LocalDate.parse(a.getDate(), service_layer.AppointmentService.DATE_FORMATTER);
                        if (fromDate != null && apptDate.isBefore(fromDate)) continue;
                        if (toDate != null && apptDate.isAfter(toDate)) continue;
                    } catch (java.time.format.DateTimeParseException ignore) {
                        continue;
                    }
                }

                String rowText = (a.getAppointmentId()
                        + resolveVehicleInfo(a.getVehicleId())
                        + resolveServiceNames(a.getServiceId())
                        + a.getDate()
                        + rowStatus
                        + ratingValue
                        + comment
                        + reviewDate).toLowerCase();
                if (!keyword.isEmpty() && !rowText.contains(keyword)) continue;

                model.addRow(new Object[]{
                    a.getAppointmentId(),
                    resolveVehicleInfo(a.getVehicleId()),
                    resolveServiceNames(a.getServiceId()),
                    a.getDate(),
                    rowStatus,
                    ratingValue,
                    comment,
                    reviewDate
                });
            }
        };

        javax.swing.event.DocumentListener autoFilter = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
        };
        searchField.getDocument().addDocumentListener(autoFilter);
        statusFilter.addActionListener(e -> applyFilter.run());
        ratingFilter.addActionListener(e -> applyFilter.run());

        rangeBtn.addActionListener(e -> {
            String[] picked = showDateRangePicker(fromDateValue[0], toDateValue[0]);
            if (picked == null) return;
            fromDateValue[0] = picked[0] == null ? "" : picked[0];
            toDateValue[0] = picked[1] == null ? "" : picked[1];
            applyFilter.run();
        });

        applyFilter.run();

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
                    if ("PENDING".equalsIgnoreCase(status)) c.setBackground(new Color(255, 253, 235));
                    else if ("REVIEWED".equalsIgnoreCase(status)) c.setBackground(new Color(236, 253, 242));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : SharedStyles.TABLE_ZEBRA);
                }
                return c;
            }
        });
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 205)));

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);
        body.add(filterRow, BorderLayout.NORTH);
        body.add(tableScroll, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        reviewBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                SharedStyles.showSelectionError(context.getOwner());
                return;
            }
            String status = table.getValueAt(row, 4).toString();
            if (status.equals("Pending")) {
                showReviewDialog(table.getValueAt(row, 0).toString());
            } else {
                javax.swing.JOptionPane.showMessageDialog(context.getOwner(), "System: " + status);
            }
        });

        revalidate();
        repaint();
    }

    private void showReviewDialog(String aptId) {
        Appointment appointment = findAppointmentById(
                appointmentService().getCustomerAppointments(currentUser().getUserId()),
                aptId);

        String vehicle = appointment == null ? "N/A" : resolveVehicleInfo(appointment.getVehicleId());
        String services = appointment == null ? "N/A" : resolveServiceNames(appointment.getServiceId());
        String date = appointment == null ? "N/A" : appointment.getDate();

        JDialog dialog = new JDialog(context.getOwner(), "Submit Review", true);
        dialog.setLayout(new BorderLayout(0, 12));

        JLabel header = new JLabel("Share your experience");
        header.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 0, 2));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("Appointment: " + aptId));
        infoPanel.add(new JLabel("Vehicle: " + vehicle));
        infoPanel.add(new JLabel("Services: " + services));
        infoPanel.add(new JLabel("Date: " + date));

        JPanel infoCard = new JPanel(new BorderLayout());
        infoCard.setOpaque(false);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        infoCard.add(infoPanel, BorderLayout.CENTER);

        int[] selectedRating = {0};
        JLabel ratingValueLabel = new JLabel("No rating");
        ratingValueLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        ratingValueLabel.setForeground(Color.GRAY);

        JPanel starPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        starPanel.setOpaque(false);
        List<JToggleButton> starButtons = new ArrayList<>();
        ButtonGroup starGroup = new ButtonGroup();
        for (int i = 1; i <= 5; i++) {
            JToggleButton btn = new JToggleButton("☆");
            btn.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 22));
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            final int rating = i;
            btn.addActionListener(e -> {
                selectedRating[0] = rating;
                updateStarButtons(starButtons, rating);
                ratingValueLabel.setText(rating + "/5");
                ratingValueLabel.setForeground(Color.DARK_GRAY);
            });
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    updateStarButtons(starButtons, rating);
                    ratingValueLabel.setText(rating + "/5");
                    ratingValueLabel.setForeground(Color.DARK_GRAY);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    int current = selectedRating[0];
                    updateStarButtons(starButtons, current);
                    if (current == 0) {
                        ratingValueLabel.setText("No rating");
                        ratingValueLabel.setForeground(Color.GRAY);
                    } else {
                        ratingValueLabel.setText(current + "/5");
                        ratingValueLabel.setForeground(Color.DARK_GRAY);
                    }
                }
            });
            starGroup.add(btn);
            starButtons.add(btn);
            starPanel.add(btn);
        }

        JTextArea comment = new JTextArea(5, 28);
        comment.setLineWrap(true);
        comment.setWrapStyleWord(true);
        JScrollPane commentScroll = new JScrollPane(comment);
        JLabel wordCountLabel = new JLabel("0/50 words");
        wordCountLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 12));
        wordCountLabel.setForeground(Color.GRAY);

        Runnable updateWordCount = () -> {
            int words = countWords(comment.getText());
            wordCountLabel.setText(words + "/50 words");
            wordCountLabel.setForeground(words > 50 ? Color.RED : Color.GRAY);
        };
        comment.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateWordCount.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateWordCount.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateWordCount.run(); }
        });

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(header);
        form.add(Box.createVerticalStrut(8));
        form.add(infoCard);
        form.add(Box.createVerticalStrut(12));
        form.add(new JLabel("Rating *"));
        JPanel ratingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        ratingRow.setOpaque(false);
        ratingRow.add(starPanel);
        ratingRow.add(ratingValueLabel);
        form.add(ratingRow);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Comment (optional, max 50 words)"));
        form.add(commentScroll);
        form.add(Box.createVerticalStrut(4));
        form.add(wordCountLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton submit = SharedStyles.createActionButton("Submit", SharedStyles.BTN_GREEN);
        JButton cancel = SharedStyles.createActionButton("Cancel", SharedStyles.BTN_RED);
        actions.add(cancel);
        actions.add(submit);

        submit.addActionListener(e -> {
            int words = countWords(comment.getText());
            if (selectedRating[0] == 0) {
                SharedStyles.showWarning(dialog, "Please select a rating.");
                return;
            }
            if (words > 50) {
                SharedStyles.showWarning(dialog, "Comment must be 50 words or fewer.");
                return;
            }
            String result = reviewService().submitReview(currentUser().getUserId(), aptId, selectedRating[0], comment.getText().trim());
            if (result != null && result.startsWith("Success")) {
                SharedStyles.showMessage(dialog, result);
                dialog.dispose();
                context.getRefreshAction().run();
            } else {
                SharedStyles.showWarning(dialog, result == null ? "Unable to submit review." : result);
            }
        });
        cancel.addActionListener(e -> dialog.dispose());

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 12, 12, 12));
        body.add(form, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);

        dialog.add(body, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(context.getOwner());
        dialog.setVisible(true);
    }

    private void updateStarButtons(List<JToggleButton> buttons, int rating) {
        for (int i = 0; i < buttons.size(); i++) {
            JToggleButton btn = buttons.get(i);
            boolean filled = i < rating;
            btn.setText(filled ? "★" : "☆");
            btn.setForeground(filled ? new Color(245, 166, 35) : Color.GRAY);
        }
    }

    private int countWords(String text) {
        if (text == null) return 0;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return 0;
        return trimmed.split("\\s+").length;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return LocalDate.parse(raw, service_layer.AppointmentService.DATE_FORMATTER);
        } catch (java.time.format.DateTimeParseException ex) {
            return null;
        }
    }
}
