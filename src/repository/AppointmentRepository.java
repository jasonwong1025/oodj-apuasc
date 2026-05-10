package repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import model.appointment.Appointment;

public class AppointmentRepository {
    private static final String FILE_PATH = "data/appointments.txt";

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return appointments;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8) {
                    appointments.add(new Appointment(
                    utils.FileStorageHelper.unescape(parts[0]),
                    utils.FileStorageHelper.unescape(parts[1]),
                    utils.FileStorageHelper.unescape(parts[2]),
                    utils.FileStorageHelper.unescape(parts[3]),
                    utils.FileStorageHelper.unescape(parts[4]),
                    utils.FileStorageHelper.unescape(parts[5]),
                    utils.FileStorageHelper.unescape(parts[6]),
                    utils.FileStorageHelper.unescape(parts[7]),
                    parts.length > 8 ? utils.FileStorageHelper.unescape(parts[8]) : "NORMAL",
                    parts.length > 9 ? utils.FileStorageHelper.unescape(parts[9]) : "NONE"
            ));
                }
            }
        } catch (IOException e) {
            utils.Logger.error("AppointmentRepository", "Failed to load appointments", e);
        }
        return appointments;
    }

    public List<Appointment> getAppointmentsByCustomer(String customerId) {
        List<Appointment> all = getAllAppointments();
        List<Appointment> filtered = new ArrayList<>();
        for (Appointment a : all) {
            if (a.getCustomerId().equals(customerId)) {
                filtered.add(a);
            }
        }
        return filtered;
    }

    public void save(Appointment appointment) {
        try {
            utils.FileStorageHelper.appendLine(FILE_PATH, appointment.toString());
        } catch (IOException e) {
            utils.Logger.error("AppointmentRepository", "Failed to load appointments", e);
        }
    }

    public void update(Appointment updatedAppointment) {
        List<Appointment> appointments = getAllAppointments();
        List<String> lines = new ArrayList<>();
        for (Appointment a : appointments) {
            if (a.getAppointmentId().equals(updatedAppointment.getAppointmentId())) {
                lines.add(updatedAppointment.toString());
            } else {
                lines.add(a.toString());
            }
        }
        try {
            utils.FileStorageHelper.writeAtomic(FILE_PATH, lines);
        } catch (IOException e) {
            utils.Logger.error("AppointmentRepository", "Failed to load appointments", e);
        }
    }
}
