package ui.TechnicianPortal;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.appointment.Appointment;
import model.feedback.Feedback;
import model.service.Service;
import model.vehicle.Vehicle;
import repository.FeedbackRepository;
import ui.shared.SharedStyles;

public class ProvideFeedbackTabPanel extends TechnicianTabPanel {

    public ProvideFeedbackTabPanel(TechnicianContext context) {
        super(context);
        setLayout(new BorderLayout());
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        List<Appointment> allAppointments = context.appointmentService().getAllAppointments();

        List<Appointment> myTasks = new ArrayList<>();
        for (Appointment a : allAppointments) {
            if (currentUser().getUserId().equals(a.getTechnicianId()) &&
                "COMPLETED".equalsIgnoreCase(a.getStatus())) {
                myTasks.add(a);
            }
        }

        String[] columns = {"ID", "Vehicle", "Service", "Appt Date", "Appt Time", "Status", "Feedback", "Feedback Date & Time"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        FeedbackRepository fbRepo = new FeedbackRepository();

        for (Appointment a : myTasks) {
            String serviceDisplay = resolveServiceNames(a.getServiceId());
            Vehicle v = context.vehicleService().findById(a.getVehicleId());
            String vehicleDisplay = (v != null) 
                ? String.format("%s (%s %s)", v.getPlateNumber(), v.getBrand(), v.getModel()) 
                : "Unknown Vehicle";

            Feedback fbObj = fbRepo.findByAppointmentId(a.getAppointmentId());
            boolean hasFeedback = (fbObj != null && fbObj.getDescription() != null && 
                                  !fbObj.getDescription().trim().isEmpty() && 
                                  !"NONE".equalsIgnoreCase(fbObj.getDescription()));
            
            String existingFb = hasFeedback ? fbObj.getDescription() : "-";
            String fbTime = hasFeedback ? fbObj.getDateTime() : "-";
            String status = hasFeedback ? "Submitted" : "Pending Feedback";

            model.addRow(new Object[]{
                    a.getAppointmentId(),
                    vehicleDisplay,
                    serviceDisplay,
                    a.getDate(),
                    a.getTime(),
                    status,
                    existingFb,
                    fbTime
            });
        }

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        // Button bar
        JButton writeFeedbackBtn = SharedStyles.createActionButton("Write Feedback", SharedStyles.BTN_BLUE);
        writeFeedbackBtn.setEnabled(false);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setOpaque(false);
        topBar.add(writeFeedbackBtn);

        root.add(topBar, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        // Enable button only when a row is selected
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                writeFeedbackBtn.setEnabled(table.getSelectedRow() != -1);
            }
        });

        // Open JDialog modal on button click
        writeFeedbackBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String id = table.getValueAt(row, 0).toString();
            String svcName = table.getValueAt(row, 2).toString();
            String existingFb = table.getValueAt(row, 6).toString();
            openFeedbackDialog(id, svcName, "-".equals(existingFb) ? "" : existingFb);
        });

        add(root, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }

    private void openFeedbackDialog(String apptId, String svcName, String existingFb) {
        JDialog dialog = new JDialog(context.owner(), "Write Feedback", true);
        dialog.setSize(500, 560);
        dialog.setLocationRelativeTo(context.owner());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel titleLbl = new JLabel("Feedback Form", SwingConstants.LEFT);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setForeground(new Color(38, 38, 42));
        titleLbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        content.add(titleLbl, gbc);

        gbc.gridy++;
        JLabel idLabel = new JLabel("Appointment ID:");
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        idLabel.setForeground(new Color(100, 100, 100));
        content.add(idLabel, gbc);

        gbc.gridy++;
        JTextField idField = SharedStyles.createFilterField(20);
        idField.setText(apptId);
        idField.setEditable(false);
        idField.setBackground(new Color(245, 245, 247));
        content.add(idField, gbc);

        gbc.gridy++;
        JLabel svcLabel = new JLabel("Service Name:");
        svcLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        svcLabel.setForeground(new Color(100, 100, 100));
        content.add(svcLabel, gbc);

        gbc.gridy++;
        JTextField svcField = SharedStyles.createFilterField(20);
        svcField.setText(svcName);
        svcField.setEditable(false);
        svcField.setBackground(new Color(245, 245, 247));
        content.add(svcField, gbc);

        gbc.gridy++;
        JLabel fbLabel = new JLabel("Your Feedback:");
        fbLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        fbLabel.setForeground(new Color(100, 100, 100));
        content.add(fbLabel, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JTextArea fbArea = new JTextArea(6, 20);
        fbArea.setText(existingFb);
        fbArea.setLineWrap(true);
        fbArea.setWrapStyleWord(true);
        fbArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane fbScroll = new JScrollPane(fbArea);
        fbScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        content.add(fbScroll, gbc);

        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy++;
        gbc.insets = new Insets(14, 4, 4, 4);
        JButton submitBtn = SharedStyles.createActionButton("Save Feedback", SharedStyles.BTN_BLUE);
        content.add(submitBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 4, 6, 4);
        JButton cancelBtn = SharedStyles.createActionButton("Cancel", new Color(150, 150, 150));
        content.add(cancelBtn, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());

        submitBtn.addActionListener(e -> {
            String fb = fbArea.getText().trim();
            if (fb.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Feedback content cannot be empty.");
                return;
            }
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            FeedbackRepository fRepo = new FeedbackRepository();
            Feedback fbObj = fRepo.findByAppointmentId(apptId);
            if (fbObj == null) {
                fbObj = new Feedback(fRepo.generateNextId(), apptId, fb, currentDateTime);
            } else {
                fbObj.setDescription(fb);
                fbObj.setDateTime(currentDateTime);
            }
            fRepo.addOrUpdate(fbObj);
            dialog.dispose();
            JOptionPane.showMessageDialog(context.owner(), "Feedback saved successfully!");
            context.refreshAction().run();
        });

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private String resolveServiceNames(String serviceIds) {
        if (serviceIds == null || serviceIds.trim().isEmpty()) return "N/A";
        String[] parts = serviceIds.split(",");
        List<String> names = new ArrayList<>();
        service_layer.ServiceService serviceSvc = new service_layer.ServiceService();
        for (String p : parts) {
            Service svc = serviceSvc.findById(p.trim());
            names.add(svc != null ? svc.getServiceName() : p.trim());
        }
        return String.join(", ", names);
    }
}
