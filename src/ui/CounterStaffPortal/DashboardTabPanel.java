package ui.CounterStaffPortal;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.appointment.Appointment;
import ui.SharedStyles;
import java.util.List;

public class DashboardTabPanel extends CounterStaffTabPanel {

    public DashboardTabPanel(CounterStaffContext context) {
        super(context);
        setLayout(new BorderLayout());
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        int totalCustomers = context.userService().getAllCustomers().size();
        List<Appointment> appointments = context.appointmentService().getAllAppointments();

        int pending = 0;
        for (Appointment a : appointments) {
            if ("PENDING".equals(a.getStatus())) {
                pending++;
            }
        }

        JLabel title = new JLabel("Welcome, " + currentUser().getFullName());
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel c1 = new JLabel("Total Customers: " + totalCustomers);
        JLabel c2 = new JLabel("Total Appointments: " + appointments.size());
        JLabel c3 = new JLabel("Pending Appointments: " + pending);

        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        c1.setBorder(new EmptyBorder(5, 0, 5, 0));
        c2.setBorder(new EmptyBorder(5, 0, 5, 0));
        c3.setBorder(new EmptyBorder(5, 0, 5, 0));

        card.add(title);
        card.add(c1);
        card.add(c2);
        card.add(c3);

        root.add(card, BorderLayout.NORTH);
        add(root, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }
}
