package ui.CustomerPortal;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import service_layer.AppointmentService;
import ui.shared.SharedStyles;

public class DateRangePickerDialog extends JDialog {
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate displayedMonth;
    private JLabel monthLabel;
    private JPanel calendarGrid;
    private boolean confirmed = false;
    private JButton confirmBtn;

    public DateRangePickerDialog(java.awt.Frame parent, String startValue, String endValue) {
        super(parent, "Select Date Range", true);
        setLayout(new BorderLayout());
        setResizable(false);

        LocalDate today = LocalDate.now();
        displayedMonth = today.withDayOfMonth(1);
        startDate = parseDateSafe(startValue);
        endDate = parseDateSafe(endValue);
        if (startDate == null && endDate != null) {
            startDate = endDate;
            endDate = null;
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel calendarHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        calendarHeader.setOpaque(false);
        JButton prevMonth = SharedStyles.createActionButton("<", SharedStyles.BTN_BLUE);
        JButton nextMonth = SharedStyles.createActionButton(">", SharedStyles.BTN_BLUE);
        monthLabel = new JLabel();
        monthLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
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

        JLabel hint = new JLabel("Select a start date, then an end date.");
        hint.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        hint.setForeground(java.awt.Color.GRAY);

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 6; p.add(calendarHeader, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 6; p.add(calendarGrid, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 6; p.add(hint, gbc);
        gbc.gridwidth = 1;

        add(p, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(java.awt.Color.WHITE);
        confirmBtn = SharedStyles.createActionButton("Confirm", SharedStyles.BTN_GREEN);
        JButton cancel = SharedStyles.createActionButton("Cancel", SharedStyles.BTN_RED);

        confirmBtn.addActionListener(e -> {
            if (startDate == null || endDate == null) {
                SharedStyles.showWarning(this, "Please select a date range.");
                return;
            }
            confirmed = true;
            dispose();
        });
        cancel.addActionListener(e -> dispose());

        btnPanel.add(cancel);
        btnPanel.add(confirmBtn);
        add(btnPanel, BorderLayout.SOUTH);

        updateCalendar();
        pack();
        setLocationRelativeTo(parent);
    }

    private void updateCalendar() {
        calendarGrid.removeAll();

        YearMonth ym = YearMonth.of(displayedMonth.getYear(), displayedMonth.getMonth());
        monthLabel.setText(ym.getMonth() + " " + ym.getYear());

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
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
            styleRangeButton(btn, date);
            btn.addActionListener(e -> {
                if (startDate == null || endDate != null) {
                    startDate = date;
                    endDate = null;
                } else {
                    if (date.isBefore(startDate)) {
                        endDate = startDate;
                        startDate = date;
                    } else {
                        endDate = date;
                    }
                }
                updateCalendar();
            });
            calendarGrid.add(btn);
        }

        confirmBtn.setEnabled(startDate != null && endDate != null);
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private void styleRangeButton(JButton btn, LocalDate date) {
        if (startDate != null && date.equals(startDate)) {
            btn.setBackground(SharedStyles.NAV_ACTIVE_TOP);
            btn.setForeground(java.awt.Color.WHITE);
            return;
        }
        if (endDate != null && date.equals(endDate)) {
            btn.setBackground(SharedStyles.NAV_ACTIVE_TOP);
            btn.setForeground(java.awt.Color.WHITE);
            return;
        }
        if (startDate != null && endDate != null && !date.isBefore(startDate) && !date.isAfter(endDate)) {
            btn.setBackground(new java.awt.Color(220, 234, 255));
            btn.setForeground(java.awt.Color.BLACK);
            return;
        }
        btn.setBackground(null);
        btn.setForeground(java.awt.Color.BLACK);
    }

    private LocalDate parseDateSafe(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw, AppointmentService.DATE_FORMATTER);
        } catch (java.time.format.DateTimeParseException ex) {
            return null;
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getStartDate() {
        return startDate == null ? "" : startDate.format(AppointmentService.DATE_FORMATTER);
    }

    public String getEndDate() {
        return endDate == null ? "" : endDate.format(AppointmentService.DATE_FORMATTER);
    }
}
