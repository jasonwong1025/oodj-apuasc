package ui.TechnicianPortal;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.appointment.Appointment;
import model.service.Service;
import model.users.User;
import model.vehicle.Vehicle;
import ui.shared.SharedStyles;
import java.util.ArrayList;
import java.util.List;

public class DashboardTabPanel extends TechnicianTabPanel {

    public DashboardTabPanel(TechnicianContext context) {
        super(context);
        setLayout(new BorderLayout());
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top section contains Header and Stats cards
        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);

        // Welcome Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Welcome back, " + currentUser().getFullName() + "!");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(38, 38, 42));
        JLabel subtitle = new JLabel("Here's your task overview for today.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(110, 110, 115));
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        topContainer.add(headerPanel, BorderLayout.NORTH);

        // Compute Statistics
        List<Appointment> allAppointments = context.appointmentService().getAllAppointments();
        int total = 0, inProgress = 0, completed = 0, pendingConfirmed = 0;
        
        List<Appointment> myUpcoming = new ArrayList<>();

        for (Appointment a : allAppointments) {
            if (currentUser().getUserId().equals(a.getTechnicianId())) {
                total++;
                if ("IN PROGRESS".equalsIgnoreCase(a.getStatus())) {
                    inProgress++;
                    myUpcoming.add(a);
                }
                else if ("COMPLETED".equalsIgnoreCase(a.getStatus())) {
                    completed++;
                }
                else {
                    pendingConfirmed++;
                    myUpcoming.add(a);
                }
            }
        }

        // Stats Cards Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 100));

        statsPanel.add(createStatCard("Total Tasks", String.valueOf(total), new Color(0, 120, 215)));
        statsPanel.add(createStatCard("Pending/Confirmed", String.valueOf(pendingConfirmed), new Color(230, 126, 34)));
        statsPanel.add(createStatCard("In Progress", String.valueOf(inProgress), new Color(46, 160, 67)));
        statsPanel.add(createStatCard("Completed", String.valueOf(completed), new Color(38, 38, 42)));
        topContainer.add(statsPanel, BorderLayout.SOUTH);

        root.add(topContainer, BorderLayout.NORTH);

        // Center section: Upcoming Tasks table
        JPanel upcomingPanel = new JPanel(new BorderLayout(0, 10));
        upcomingPanel.setOpaque(false);

        JLabel upcomingLbl = new JLabel("Your Upcoming Tasks");
        upcomingLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        upcomingLbl.setForeground(new Color(38, 38, 42));
        upcomingPanel.add(upcomingLbl, BorderLayout.NORTH);

        String[] columns = {"ID", "Customer", "Vehicle", "Service", "Date", "Time", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Appointment a : myUpcoming) {
            String serviceDisplay = resolveServiceNames(a.getServiceId());
            
            User cust = context.userService().findByUserId(a.getCustomerId());
            String customerDisplay = (cust != null) ? String.format("%s (%s)", cust.getFullName(), cust.getUserId()) : a.getCustomerId();

            Vehicle v = context.vehicleService().findById(a.getVehicleId());
            String vehicleDisplay = (v != null) ? String.format("%s (%s %s)", v.getPlateNumber(), v.getBrand(), v.getModel()) : a.getVehicleId();

            tblModel.addRow(new Object[]{
                a.getAppointmentId(),
                customerDisplay,
                vehicleDisplay,
                serviceDisplay,
                a.getDate(),
                a.getTime(),
                a.getStatus()
            });
        }

        JTable table = new JTable(tblModel);
        SharedStyles.applyTableStyle(table);
        upcomingPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        root.add(upcomingPanel, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }

    private JPanel createStatCard(String label, String value, Color accentColor) {
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        valLbl.setForeground(accentColor);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(new Color(110, 110, 115));

        card.add(valLbl, BorderLayout.CENTER);
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }

    private String resolveServiceNames(String serviceIds) {
        if (serviceIds == null || serviceIds.trim().isEmpty()) return "N/A";
        String[] parts = serviceIds.split(",");
        List<String> names = new ArrayList<>();
        for (String p : parts) {
            Service svc = context.serviceService().findById(p.trim());
            names.add(svc != null ? svc.getServiceName() : p.trim());
        }
        return String.join(", ", names);
    }
}
