package ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

public class DateTimePicker extends JDialog {

    private final JComboBox<Integer> yearCombo;
    private final JComboBox<Integer> monthCombo;
    private final JComboBox<Integer> dayCombo;
    private final JComboBox<String> hourCombo;
    private final JComboBox<String> minuteCombo;
    private boolean confirmed = false;

    public DateTimePicker(Frame parent) {
        super(parent, "Select Date & Time", true);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        LocalDateTime now = LocalDateTime.now();

        yearCombo = new JComboBox<>(IntStream.rangeClosed(now.getYear(), now.getYear() + 5).boxed().toArray(Integer[]::new));
        monthCombo = new JComboBox<>(IntStream.rangeClosed(1, 12).boxed().toArray(Integer[]::new));
        dayCombo = new JComboBox<>(IntStream.rangeClosed(1, 31).boxed().toArray(Integer[]::new));
        
        String[] hours = IntStream.range(0, 24).mapToObj(i -> String.format("%02d", i)).toArray(String[]::new);
        String[] mins = IntStream.range(0, 60).mapToObj(i -> String.format("%02d", i)).toArray(String[]::new);
        
        hourCombo = new JComboBox<>(hours);
        minuteCombo = new JComboBox<>(mins);

        yearCombo.setSelectedItem(now.getYear());
        monthCombo.setSelectedItem(now.getMonthValue());
        dayCombo.setSelectedItem(now.getDayOfMonth());
        hourCombo.setSelectedItem(String.format("%02d", now.getHour()));
        minuteCombo.setSelectedItem(String.format("%02d", now.getMinute()));

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; p.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; p.add(yearCombo, gbc);
        gbc.gridx = 2; p.add(new JLabel("Month:"), gbc);
        gbc.gridx = 3; p.add(monthCombo, gbc);
        gbc.gridx = 4; p.add(new JLabel("Day:"), gbc);
        gbc.gridx = 5; p.add(dayCombo, gbc);

        y++;
        gbc.gridy = y; gbc.gridx = 0; p.add(new JLabel("Hour:"), gbc);
        gbc.gridx = 1; p.add(hourCombo, gbc);
        gbc.gridx = 2; p.add(new JLabel("Minute:"), gbc);
        gbc.gridx = 3; p.add(minuteCombo, gbc);

        add(p, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton ok = SharedStyles.createActionButton("Confirm", SharedStyles.BTN_GREEN);
        JButton cancel = SharedStyles.createActionButton("Cancel", SharedStyles.BTN_RED);
        
        ok.addActionListener(e -> { confirmed = true; dispose(); });
        cancel.addActionListener(e -> dispose());
        
        btnPanel.add(cancel);
        btnPanel.add(ok);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public static String showPicker(Frame parent) {
        DateTimePicker picker = new DateTimePicker(parent);
        picker.setVisible(true);
        if (picker.confirmed) {
            return String.format("%04d-%02d-%02d %s:%s",
                    picker.yearCombo.getSelectedItem(),
                    picker.monthCombo.getSelectedItem(),
                    picker.dayCombo.getSelectedItem(),
                    picker.hourCombo.getSelectedItem(),
                    picker.minuteCombo.getSelectedItem());
        }
        return null;
    }
}
