package ui.TechnicianPortal;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.appointment.Appointment;
import model.feedback.Review;
import model.users.User;
import ui.SharedStyles;

public class ReviewsTabPanel extends TechnicianTabPanel {

    public ReviewsTabPanel(TechnicianContext context) {
        super(context);
        setLayout(new BorderLayout());
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        
        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        String[] columns = {
                "Review ID",
                "Appointment ID",
                "Customer",
                "Rating",
                "Review",
                "Review Date & time"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        List<Review> allReviews = context.reviewService().getAllReviews();
        List<Appointment> allAppts = context.appointmentService().getAllAppointments();

        for (Review r : allReviews) {
            // Find appointment to check if it's for this technician
            Appointment appt = null;
            for (Appointment a : allAppts) {
                if (a.getAppointmentId().equals(r.getAppointmentId())) {
                    appt = a;
                    break;
                }
            }

            if (appt != null && currentUser().getUserId().equals(appt.getTechnicianId())) {
                User cust = context.userService().findByUserId(appt.getCustomerId());
                String customerDisplay = (cust != null) ? cust.getFullName() : appt.getCustomerId();

                model.addRow(new Object[]{
                        r.getReviewId(),
                        r.getAppointmentId(),
                        customerDisplay,
                        r.getRating() + " / 5",
                        r.getDescription(),
                        r.getDateTime()
                });
            }
        }

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        
        add(root, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }
}
