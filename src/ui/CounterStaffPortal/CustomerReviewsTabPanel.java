package ui.CounterStaffPortal;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.appointment.Appointment;
import model.feedback.Review;
import ui.shared.SharedStyles;

public class CustomerReviewsTabPanel extends CounterStaffTabPanel {

    private final DefaultTableModel model;
    private final JTextField searchField;

    public CustomerReviewsTabPanel(CounterStaffContext context) {
        super(context);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        String[] columns = {
            "Review ID", "Appointment ID", "Customer ID", "Rating", "Review", "Review Date & time"
        };
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(new JLabel("Search Customer ID:"));
        searchField = SharedStyles.createFilterField(20);
        topPanel.add(searchField);

        root.add(topPanel, BorderLayout.NORTH);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 2));
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        root.add(new JScrollPane(table), BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        List<Review> reviews = context.reviewService().getAllReviews();
        List<Appointment> appointments = context.appointmentService().getAllAppointments();

        for (Review r : reviews) {
            for (Appointment a : appointments) {
                if (a.getAppointmentId().equals(r.getAppointmentId())) {
                    if (currentUser().getUserId().equals(a.getCounterStaffId())) {
                        model.addRow(new Object[]{
                            r.getReviewId(),
                            r.getAppointmentId(),
                            a.getCustomerId(),
                            r.getRating() + " / 5",
                            r.getDescription(),
                            r.getDateTime()
                        });
                        break;
                    }
                }
            }
        }
    }
}
