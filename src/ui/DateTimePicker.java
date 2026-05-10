package ui;

import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.swing.*;
import service_layer.AppointmentService;
import service_layer.AppointmentService.SlotCapacity;
import service_layer.AppointmentService.SlotType;

public class DateTimePicker extends JDialog {

    private final JComboBox<SlotOption> slotCombo;
    private final AppointmentService appointmentService;
    private final SlotType requestedType;
    private boolean confirmed = false;
    private int lastValidSlotIndex = -1;
    private String selectedDate;
    private String selectedTime;
    private LocalDate displayedMonth;
    private LocalDate selectedDateObj;
    private JLabel monthLabel;
    private JPanel calendarGrid;

    public DateTimePicker(Frame parent, SlotType requestedType) {
        super(parent, "Select Date & Time", true);
        setLayout(new BorderLayout());
        setResizable(false);
        this.appointmentService = new AppointmentService();
        this.requestedType = requestedType;

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        LocalDate today = LocalDate.now();
        displayedMonth = today.withDayOfMonth(1);
        selectedDateObj = today;
        slotCombo = new JComboBox<>();

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

        JPanel calendarHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        calendarHeader.setOpaque(false);
        JButton prevMonth = SharedStyles.createActionButton("<", SharedStyles.BTN_BLUE);
        JButton nextMonth = SharedStyles.createActionButton(">", SharedStyles.BTN_BLUE);
        monthLabel = new JLabel();
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        calendarHeader.add(prevMonth);
        calendarHeader.add(monthLabel);
        calendarHeader.add(nextMonth);

        calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarGrid.setOpaque(false);

        prevMonth.addActionListener(e -> {
            displayedMonth = displayedMonth.minusMonths(1);
            updateCalendar();
        });
        nextMonth.addActionListener(e -> {
            displayedMonth = displayedMonth.plusMonths(1);
            updateCalendar();
        });
        slotCombo.addActionListener(e -> {
            SlotOption selected = (SlotOption) slotCombo.getSelectedItem();
            if (selected == null) return;
            if (!selected.available) {
                Toolkit.getDefaultToolkit().beep();
                if (lastValidSlotIndex >= 0 && lastValidSlotIndex < slotCombo.getItemCount()) {
                    slotCombo.setSelectedIndex(lastValidSlotIndex);
                }
                return;
            }
            lastValidSlotIndex = slotCombo.getSelectedIndex();
        });

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 6; p.add(calendarHeader, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 6; p.add(calendarGrid, gbc);
        gbc.gridwidth = 1;

        y++;
        gbc.gridy = y; gbc.gridx = 0; p.add(new JLabel("Time Slot:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 5; p.add(slotCombo, gbc);
        gbc.gridwidth = 1;

        add(p, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton ok = SharedStyles.createActionButton("Confirm", SharedStyles.BTN_GREEN);
        JButton cancel = SharedStyles.createActionButton("Cancel", SharedStyles.BTN_RED);
        
        ok.addActionListener(e -> {
            SlotOption selectedSlot = (SlotOption) slotCombo.getSelectedItem();
            if (selectedSlot == null || !selectedSlot.available) {
                SharedStyles.showWarning(this, "Please select an available slot.");
                return;
            }

            String date = getSelectedDate();
            utils.Result<Void> result = appointmentService.validateSchedule(date, selectedSlot.time, requestedType);
            if (result.isFailure()) {
                SharedStyles.showWarning(this, result.getError());
                refreshSlotOptions();
                return;
            }

            selectedDate = date;
            selectedTime = selectedSlot.time;
            confirmed = true;
            dispose();
        });
        cancel.addActionListener(e -> dispose());
        
        btnPanel.add(cancel);
        btnPanel.add(ok);
        add(btnPanel, BorderLayout.SOUTH);

        updateCalendar();
        refreshSlotOptions();

        pack();
        setLocationRelativeTo(parent);
    }

    private String getSelectedDate() {
        LocalDate date = selectedDateObj != null ? selectedDateObj : LocalDate.now();
        return date.format(AppointmentService.DATE_FORMATTER);
    }

    private void updateCalendar() {
        calendarGrid.removeAll();

        YearMonth ym = YearMonth.of(displayedMonth.getYear(), displayedMonth.getMonth());
        monthLabel.setText(ym.getMonth() + " " + ym.getYear());

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            calendarGrid.add(lbl);
        }

        LocalDate firstDay = displayedMonth;
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
            if (date.equals(selectedDateObj)) {
                btn.setBackground(SharedStyles.NAV_ACTIVE_TOP);
                btn.setForeground(Color.WHITE);
            }
            btn.addActionListener(e -> {
                selectedDateObj = date;
                updateCalendar();
                refreshSlotOptions();
            });
            calendarGrid.add(btn);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private void refreshSlotOptions() {
        String dateValue = getSelectedDate();
        slotCombo.removeAllItems();
        lastValidSlotIndex = -1;

        int index = 0;
        for (String slotTime : AppointmentService.getAllowedSlotTimes()) {
            SlotCapacity cap = appointmentService.getSlotCapacity(dateValue, slotTime);
            boolean available = appointmentService.isSlotAvailable(dateValue, slotTime, requestedType);
            int majorLimit = appointmentService.getCapacityLimitForSlotType(SlotType.MAJOR);
            int normalLimit = appointmentService.getCapacityLimitForSlotType(SlotType.NORMAL);
            int totalLimit = appointmentService.getTotalCapacityLimit();
            String label = String.format("%s  [Major: %d/%d | Normal: %d/%d | Total: %d/%d]%s",
                    slotTime,
                    cap.getMajorCount(), majorLimit,
                    cap.getNormalCount(), normalLimit,
                    cap.getTotalCount(), totalLimit,
                    available ? "" : " (FULL)");
            slotCombo.addItem(new SlotOption(slotTime, label, available));
            if (available && lastValidSlotIndex == -1) {
                lastValidSlotIndex = index;
            }
            index++;
        }

        if (lastValidSlotIndex >= 0) {
            slotCombo.setSelectedIndex(lastValidSlotIndex);
        }
    }

    public static String showPicker(Frame parent, SlotType requestedType) {
        DateTimePicker picker = new DateTimePicker(parent, requestedType);
        picker.setVisible(true);
        if (picker.confirmed) {
            return picker.selectedDate + " " + picker.selectedTime;
        }
        return null;
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
