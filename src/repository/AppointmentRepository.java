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
                    parts[0],
                    parts[1],
                    parts[2],
                    parts[3],
                    parts[4],
                    parts[5],
                    parts[6],
                    parts[7],
                    parts.length > 8 ? parts[8] : "NORMAL",
                    parts.length > 9 ? parts[9] : "NONE"
            ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(appointment.toString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(Appointment updatedAppointment) {
        List<Appointment> appointments = getAllAppointments();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Appointment a : appointments) {
                if (a.getAppointmentId().equals(updatedAppointment.getAppointmentId())) {
                    bw.write(updatedAppointment.toString());
                } else {
                    bw.write(a.toString());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
