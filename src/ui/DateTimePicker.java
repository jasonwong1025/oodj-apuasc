package ui;

import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.stream.IntStream;
import javax.swing.*;
import service_layer.AppointmentService;
import service_layer.AppointmentService.SlotCapacity;
import service_layer.AppointmentService.SlotType;

public class DateTimePicker extends JDialog {

    private final JComboBox<Integer> yearCombo;
    private final JComboBox<Integer> monthCombo;
    private final JComboBox<Integer> dayCombo;
    private final JComboBox<SlotOption> slotCombo;
    private final AppointmentService appointmentService;
    private final SlotType requestedType;
    private boolean confirmed = false;
    private int lastValidSlotIndex = -1;
    private String selectedDate;
    private String selectedTime;

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

        yearCombo = new JComboBox<>(IntStream.rangeClosed(today.getYear(), today.getYear() + 5).boxed().toArray(Integer[]::new));
        monthCombo = new JComboBox<>(IntStream.rangeClosed(1, 12).boxed().toArray(Integer[]::new));
        dayCombo = new JComboBox<>(IntStream.rangeClosed(1, 31).boxed().toArray(Integer[]::new));
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

        yearCombo.setSelectedItem(today.getYear());
        monthCombo.setSelectedItem(today.getMonthValue());
        dayCombo.setSelectedItem(today.getDayOfMonth());

        yearCombo.addActionListener(e -> {
            refreshDayOptions();
            refreshSlotOptions();
        });
        monthCombo.addActionListener(e -> {
            refreshDayOptions();
            refreshSlotOptions();
        });
        dayCombo.addActionListener(e -> refreshSlotOptions());
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
        gbc.gridx = 0; gbc.gridy = y; p.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; p.add(yearCombo, gbc);
        gbc.gridx = 2; p.add(new JLabel("Month:"), gbc);
        gbc.gridx = 3; p.add(monthCombo, gbc);
        gbc.gridx = 4; p.add(new JLabel("Day:"), gbc);
        gbc.gridx = 5; p.add(dayCombo, gbc);

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
            String error = appointmentService.validateSchedule(date, selectedSlot.time, requestedType);
            if (error != null) {
                SharedStyles.showWarning(this, error);
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

        refreshDayOptions();
        refreshSlotOptions();

        pack();
        setLocationRelativeTo(parent);
    }

    private String getSelectedDate() {
        return String.format("%04d-%02d-%02d",
                yearCombo.getSelectedItem(),
                monthCombo.getSelectedItem(),
                dayCombo.getSelectedItem());
    }

    private void refreshDayOptions() {
        Integer year = (Integer) yearCombo.getSelectedItem();
        Integer month = (Integer) monthCombo.getSelectedItem();
        if (year == null || month == null) return;

        int maxDay = YearMonth.of(year, month).lengthOfMonth();
        Integer currentDay = (Integer) dayCombo.getSelectedItem();

        dayCombo.removeAllItems();
        for (int i = 1; i <= maxDay; i++) {
            dayCombo.addItem(i);
        }

        if (currentDay != null) {
            dayCombo.setSelectedItem(Math.min(currentDay, maxDay));
        }
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
