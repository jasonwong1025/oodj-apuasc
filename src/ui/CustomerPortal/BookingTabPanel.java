package ui.CustomerPortal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.service.Service;
import model.vehicle.Vehicle;
import service_layer.AppointmentService;
import service_layer.AppointmentService.SlotCapacity;
import service_layer.AppointmentService.SlotType;
import ui.SharedStyles;

public class BookingTabPanel extends CustomerTabPanel {
    public BookingTabPanel(CustomerContext context) {
        super(context);
        setLayout(new BorderLayout());
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(SharedStyles.MAIN_BG);

        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Vehicle> vehicles = vehicleService().getCustomerVehicles(currentUser().getUserId());
        if (vehicles.isEmpty()) {
            card.add(new JLabel("Please register a vehicle first!"), gbc);
            GridBagConstraints emptyGbc = new GridBagConstraints();
            emptyGbc.gridx = 0;
            emptyGbc.gridy = 0;
            emptyGbc.weightx = 1;
            emptyGbc.weighty = 1;
            emptyGbc.fill = GridBagConstraints.BOTH;
            center.add(card, emptyGbc);
            add(center, BorderLayout.CENTER);
            return;
        }

        JComboBox<String> vehicleCombo = SharedStyles.createFilterCombo(
                vehicles.stream().map(v -> v.getVehicleId() + " - " + v.getPlateNumber()).toArray(String[]::new)
        );

        List<Service> allServices = serviceLookup().listAll();
        List<Service> normalServices = allServices.stream().filter(Service::isIncludedInNormalService).collect(Collectors.toList());
        List<Service> majorServices = allServices.stream().filter(s -> !s.isIncludedInNormalService()).collect(Collectors.toList());

        final int NORMAL_LIMIT = 3;
        final int MAJOR_LIMIT = 8;

        List<JCheckBox> normalChecks = new ArrayList<>();
        List<JCheckBox> majorChecks = new ArrayList<>();
        List<JCheckBox> allChecks = new ArrayList<>();

        for (Service s : normalServices) {
            JCheckBox cb = new JCheckBox(s.getServiceName() + " (RM " + String.format("%.2f", s.getPrice()) + ")");
            cb.setOpaque(false);
            cb.putClientProperty("service", s);
            normalChecks.add(cb);
            allChecks.add(cb);
        }
        for (Service s : majorServices) {
            JCheckBox cb = new JCheckBox(s.getServiceName() + " (RM " + String.format("%.2f", s.getPrice()) + ")");
            cb.setOpaque(false);
            cb.putClientProperty("service", s);
            majorChecks.add(cb);
            allChecks.add(cb);
        }

        javax.swing.JRadioButton normalCategoryBtn = new javax.swing.JRadioButton("Normal");
        javax.swing.JRadioButton majorCategoryBtn = new javax.swing.JRadioButton("Major");
        normalCategoryBtn.setSelected(true);
        normalCategoryBtn.setOpaque(false);
        majorCategoryBtn.setOpaque(false);
        javax.swing.ButtonGroup categoryGroup = new javax.swing.ButtonGroup();
        categoryGroup.add(normalCategoryBtn);
        categoryGroup.add(majorCategoryBtn);

        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        categoryPanel.setOpaque(false);
        categoryPanel.add(new JLabel("Category:"));
        categoryPanel.add(normalCategoryBtn);
        categoryPanel.add(majorCategoryBtn);

        JPanel serviceListPanel = new JPanel();
        serviceListPanel.setLayout(new BoxLayout(serviceListPanel, BoxLayout.Y_AXIS));
        serviceListPanel.setOpaque(false);

        JScrollPane serviceScroll = new JScrollPane(serviceListPanel);
        serviceScroll.setPreferredSize(new Dimension(320, 220));
        serviceScroll.setBorder(BorderFactory.createTitledBorder("Normal Services (max 3)"));
        serviceScroll.setOpaque(false);
        serviceScroll.getViewport().setOpaque(false);

        JLabel selectionStatusLabel = new JLabel("0 selected (max 3)");
        selectionStatusLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 12));
        selectionStatusLabel.setForeground(Color.GRAY);

        JLabel totalStatusLabel = new JLabel("Total selected: 0/3");
        totalStatusLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        totalStatusLabel.setForeground(SharedStyles.NAV_ACTIVE_TOP);

        LocalDate today = LocalDate.now();
        LocalDate defaultDate = today;
        if (LocalTime.now().isAfter(LocalTime.of(16, 30))) {
            defaultDate = today.plusDays(1);
        }
        final LocalDate[] selectedDate = {defaultDate};
        final String[] selectedTime = {null};
        final LocalDate[] displayedMonth = {defaultDate.withDayOfMonth(1)};

        JLabel monthLabel = new JLabel();
        monthLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        JButton prevMonth = SharedStyles.createActionButton("<", SharedStyles.BTN_BLUE);
        JButton nextMonth = SharedStyles.createActionButton(">", SharedStyles.BTN_BLUE);
        JPanel calendarHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        calendarHeader.setOpaque(false);
        calendarHeader.add(prevMonth);
        calendarHeader.add(monthLabel);
        calendarHeader.add(nextMonth);

        JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarGrid.setOpaque(false);

        JComboBox<SlotOption> slotCombo = new JComboBox<>();
        slotCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SlotOption opt) {
                    l.setText(opt.label);
                    if (!opt.available && !isSelected) {
                        l.setForeground(Color.GRAY);
                    }
                }
                return l;
            }
        });

        Runnable refreshSlots = () -> {
            slotCombo.removeAllItems();
            String dateValue = selectedDate[0].format(AppointmentService.DATE_FORMATTER);
            SlotType requestedType = majorCategoryBtn.isSelected() ? SlotType.MAJOR : SlotType.NORMAL;
            for (String slotTime : AppointmentService.getAllowedSlotTimes()) {
                SlotCapacity cap = appointmentService().getSlotCapacity(dateValue, slotTime);
                boolean available = appointmentService().isSlotAvailable(dateValue, slotTime, requestedType);

                LocalDateTime slotDateTime = LocalDateTime.of(selectedDate[0], LocalTime.parse(slotTime, AppointmentService.TIME_FORMATTER));
                if (slotDateTime.isBefore(LocalDateTime.now())) {
                    available = false;
                }

                int majorLimit = appointmentService().getCapacityLimitForSlotType(SlotType.MAJOR);
                int normalLimit = appointmentService().getCapacityLimitForSlotType(SlotType.NORMAL);
                int totalLimit = appointmentService().getTotalCapacityLimit();
                String label = String.format("%s  [Major: %d/%d | Normal: %d/%d | Total: %d/%d]%s",
                        slotTime,
                        cap.getMajorCount(), majorLimit,
                        cap.getNormalCount(), normalLimit,
                        cap.getTotalCount(), totalLimit,
                        available ? "" : " (FULL)");
                if (available) {
                    slotCombo.addItem(new SlotOption(slotTime, label, available));
                }
            }
            if (slotCombo.getItemCount() == 0) {
                slotCombo.addItem(new SlotOption(null, "No slots available", false));
            }
        };

        java.util.function.Supplier<List<JCheckBox>> activeChecks = () ->
                majorCategoryBtn.isSelected() ? allChecks : normalChecks;
        java.util.function.Supplier<Integer> maxSelection = () ->
                majorCategoryBtn.isSelected() ? MAJOR_LIMIT : NORMAL_LIMIT;

        Runnable refreshServiceList = () -> {
            if (normalCategoryBtn.isSelected()) {
                for (JCheckBox cb : majorChecks) {
                    if (cb.isSelected()) cb.setSelected(false);
                }
                serviceScroll.setBorder(BorderFactory.createTitledBorder("Normal Services"));
            } else {
                serviceScroll.setBorder(BorderFactory.createTitledBorder("All Services"));
            }
            serviceListPanel.removeAll();
            for (JCheckBox cb : activeChecks.get()) {
                serviceListPanel.add(cb);
            }
            int total = (int) activeChecks.get().stream().filter(JCheckBox::isSelected).count();
            selectionStatusLabel.setText(total + " selected (max " + maxSelection.get() + ")");
            totalStatusLabel.setText("Total selected: " + total + "/" + maxSelection.get());
            serviceListPanel.revalidate();
            serviceListPanel.repaint();
        };

        final Runnable[] updateSummaryRef = new Runnable[1];
        final Runnable[] updateCalendarRef = new Runnable[1];

        updateCalendarRef[0] = () -> {
            calendarGrid.removeAll();
            YearMonth ym = YearMonth.of(displayedMonth[0].getYear(), displayedMonth[0].getMonth());
            monthLabel.setText(ym.getMonth() + " " + ym.getYear());

            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (String d : days) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                calendarGrid.add(lbl);
            }

            LocalDate firstDay = displayedMonth[0];
            int startOffset = firstDay.getDayOfWeek().getValue();
            for (int i = 1; i < startOffset; i++) {
                calendarGrid.add(new JLabel(""));
            }

            int maxDay = ym.lengthOfMonth();
            for (int day = 1; day <= maxDay; day++) {
                LocalDate date = LocalDate.of(ym.getYear(), ym.getMonth(), day);
                JButton btn = new JButton(String.valueOf(day));
                btn.setMargin(new Insets(2, 2, 2, 2));
                btn.setFocusPainted(false);
                if (date.equals(selectedDate[0])) {
                    btn.setBackground(SharedStyles.NAV_ACTIVE_TOP);
                    btn.setForeground(Color.WHITE);
                }
                btn.addActionListener(e -> {
                    selectedDate[0] = date;
                    updateCalendarRef[0].run();
                    refreshSlots.run();
                    if (updateSummaryRef[0] != null) {
                        updateSummaryRef[0].run();
                    }
                });
                calendarGrid.add(btn);
            }

            calendarGrid.revalidate();
            calendarGrid.repaint();
        };

        prevMonth.addActionListener(e -> {
            displayedMonth[0] = displayedMonth[0].minusMonths(1);
            updateCalendarRef[0].run();
        });
        nextMonth.addActionListener(e -> {
            displayedMonth[0] = displayedMonth[0].plusMonths(1);
            updateCalendarRef[0].run();
        });

        JPanel schedulePanel = new JPanel(new BorderLayout(0, 10));
        schedulePanel.setOpaque(false);

        JPanel calendarPanel = new JPanel(new BorderLayout(0, 2));
        calendarPanel.setOpaque(false);
        calendarPanel.add(calendarHeader, BorderLayout.NORTH);
        calendarPanel.add(calendarGrid, BorderLayout.CENTER);

        JPanel slotRow = new JPanel(new BorderLayout(10, 0));
        slotRow.setOpaque(false);
        slotRow.setBorder(new EmptyBorder(6, 0, 0, 0));
        slotRow.add(new JLabel("Time Slot:"), BorderLayout.WEST);
        slotRow.add(slotCombo, BorderLayout.CENTER);

        schedulePanel.add(calendarPanel, BorderLayout.CENTER);
        schedulePanel.add(slotRow, BorderLayout.SOUTH);

        JPanel serviceSelectionPanel = new JPanel(new BorderLayout(0, 6));
        serviceSelectionPanel.setOpaque(false);
        serviceSelectionPanel.add(categoryPanel, BorderLayout.NORTH);
        serviceSelectionPanel.add(serviceScroll, BorderLayout.CENTER);
        serviceSelectionPanel.add(selectionStatusLabel, BorderLayout.SOUTH);

        JTextArea summaryArea = new JTextArea(16, 28);
        summaryArea.setEditable(false);
        summaryArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        summaryArea.setBackground(Color.WHITE);
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Summary"));

        JButton checkoutBtn = SharedStyles.createActionButton("Checkout", SharedStyles.BTN_GREEN);

        Runnable updateSummary = () -> {
            StringBuilder sb = new StringBuilder();
            boolean isMajorCategory = majorCategoryBtn.isSelected();
            List<Service> selectedServices = activeChecks.get().stream()
                    .filter(JCheckBox::isSelected)
                    .map(cb -> (Service) cb.getClientProperty("service"))
                    .collect(Collectors.toList());

            int totalCount = selectedServices.size();
            double totalPrice = 0.0;
            for (Service s : selectedServices) totalPrice += s.getPrice();

            sb.append("Category: ").append(isMajorCategory ? "Major" : "Normal").append("\n");
            sb.append("Selected services (").append(totalCount).append(")\n");
            for (Service s : selectedServices) {
                sb.append("- ").append(s.getServiceName()).append(" (RM ").append(String.format("%.2f", s.getPrice())).append(")\n");
            }
            sb.append("\nTotal selected: ").append(totalCount).append("/")
                    .append(maxSelection.get()).append("\n");
            sb.append("Total: RM ").append(String.format("%.2f", totalPrice)).append("\n");
            String scheduleText = selectedTime[0] == null
                    ? "Not selected"
                    : selectedDate[0].format(AppointmentService.DATE_FORMATTER) + " " + selectedTime[0];
            sb.append("Schedule: ").append(scheduleText).append("\n");
            summaryArea.setText(sb.toString());

            totalStatusLabel.setText("Total selected: " + totalCount + "/" + maxSelection.get());
        };
        updateSummaryRef[0] = updateSummary;

        for (JCheckBox cb : allChecks) {
            cb.addItemListener(ev -> {
                int total = (int) activeChecks.get().stream().filter(JCheckBox::isSelected).count();
                if (total > maxSelection.get()) {
                    cb.setSelected(false);
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    total = (int) activeChecks.get().stream().filter(JCheckBox::isSelected).count();
                }
                selectionStatusLabel.setText(total + " selected (max " + maxSelection.get() + ")");
                totalStatusLabel.setText("Total selected: " + total + "/" + maxSelection.get());
                refreshSlots.run();
                updateSummaryRef[0].run();
            });
        }

        normalCategoryBtn.addActionListener(e -> {
            refreshServiceList.run();
            refreshSlots.run();
            updateSummaryRef[0].run();
        });
        majorCategoryBtn.addActionListener(e -> {
            refreshServiceList.run();
            refreshSlots.run();
            updateSummaryRef[0].run();
        });

        slotCombo.addActionListener(e -> {
            SlotOption selected = (SlotOption) slotCombo.getSelectedItem();
            if (selected == null) return;
            if (!selected.available) {
                java.awt.Toolkit.getDefaultToolkit().beep();
                selectedTime[0] = null;
                updateSummaryRef[0].run();
                return;
            }
            selectedTime[0] = selected.time;
            updateSummaryRef[0].run();
        });

        updateCalendarRef[0].run();
        refreshServiceList.run();
        refreshSlots.run();
        updateSummaryRef[0].run();

        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setOpaque(false);
        JPanel leftContent = new JPanel();
        leftContent.setOpaque(false);
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));
        leftContent.add(serviceSelectionPanel);
        leftContent.add(Box.createVerticalStrut(8));
        JSeparator serviceCalendarSeparator = new JSeparator(SwingConstants.HORIZONTAL);
        serviceCalendarSeparator.setForeground(new Color(220, 220, 220));
        serviceCalendarSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        leftContent.add(serviceCalendarSeparator);
        leftContent.add(Box.createVerticalStrut(8));
        leftContent.add(schedulePanel);
        leftPanel.add(leftContent, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 12));
        rightPanel.setOpaque(false);
        rightPanel.add(summaryScroll, BorderLayout.CENTER);
        rightPanel.add(checkoutBtn, BorderLayout.SOUTH);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(leftPanel);
        contentPanel.add(rightPanel);

        int y = 0;
        SharedStyles.addFormRow(card, gbc, y++, "Select Vehicle:", vehicleCombo);

        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        card.add(contentPanel, gbc);

        gbc.gridwidth = 1;

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.weightx = 1;
        cardGbc.weighty = 1;
        cardGbc.fill = GridBagConstraints.BOTH;
        center.add(card, cardGbc);

        checkoutBtn.addActionListener(e -> {
            if (selectedTime[0] == null) {
                SharedStyles.showWarning(context.getOwner(), "Please select a date and time.");
                return;
            }

            boolean isMajorCategory = majorCategoryBtn.isSelected();
            List<Service> selected = activeChecks.get().stream()
                    .filter(JCheckBox::isSelected)
                    .map(cb -> (Service) cb.getClientProperty("service"))
                    .collect(Collectors.toList());
            int totalCount = selected.size();

            if (totalCount < 1) {
                SharedStyles.showWarning(context.getOwner(), "Please select at least 1 service.");
                return;
            }
            if (totalCount > maxSelection.get()) {
                SharedStyles.showWarning(context.getOwner(), "You can select up to " + maxSelection.get() + " services only.");
                return;
            }

            double total = selected.stream().mapToDouble(Service::getPrice).sum();
            String dateValue = selectedDate[0].format(AppointmentService.DATE_FORMATTER);
            String timeValue = selectedTime[0];
            SlotType requestedType = isMajorCategory ? SlotType.MAJOR : SlotType.NORMAL;
            String scheduleError = appointmentService().validateSchedule(dateValue, timeValue, requestedType);
            if (scheduleError != null) {
                SharedStyles.showWarning(context.getOwner(), scheduleError);
                return;
            }
            StringBuilder summary = new StringBuilder("<html><body style='width: 300px;'>");
            summary.append("<h2>Booking Summary</h2>");
            summary.append("<hr>");
            summary.append("<b>Category</b>: ")
                    .append(isMajorCategory ? "Major" : "Normal")
                    .append("<br><br>");
            summary.append("<b>Selected Services</b>:<br>");
            for (Service s : selected) {
                summary.append("* ").append(s.getServiceName()).append(": RM ")
                        .append(String.format("%.2f", s.getPrice())).append("<br>");
            }
            summary.append("<hr>");
            summary.append("<h3 style='color: #2e7d32;'>Total Amount: RM ").append(String.format("%.2f", total)).append("</h3>");
            summary.append("<br>Proceed with this booking?</body></html>");

            if (SharedStyles.showConfirm(context.getOwner(), summary.toString())) {
                String vId = vehicleCombo.getSelectedItem().toString().split(" - ")[0];
                List<String> sIds = selected.stream().map(Service::getServiceId).collect(Collectors.toList());
                String res = appointmentService().bookAppointment(currentUser().getUserId(), vId, sIds, dateValue, timeValue, "CUSTOMER");
                SharedStyles.showMessage(context.getOwner(), res);
                if (res.startsWith("Success")) {
                    context.getNavigator().navigateTo("My Appointments");
                    context.getRefreshAction().run();
                }
            }
        });

        add(center, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private static final class SlotOption {
        private final String time;
        private final String label;
        private final boolean available;

        private SlotOption(String time, String label, boolean available) {
            this.time = time;
            this.label = label;
            this.available = available;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
