package repository;

import model.feedback.Feedback;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackRepository {
    private static final String FILE_PATH = "data/feedbacks.txt";

    public List<Feedback> getAll() {
        List<Feedback> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Feedback f = parseLine(line);
                if (f != null) list.add(f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Feedback parseLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 4) return null;
        return new Feedback(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
    }

    public void addOrUpdate(Feedback feedback) {
        List<Feedback> all = getAll();
        boolean found = false;
        for (Feedback f : all) {
            if (f.getAppointmentId().equals(feedback.getAppointmentId())) {
                f.setDescription(feedback.getDescription());
                f.setType(feedback.getType());
                found = true;
                break;
            }
        }
        if (!found) {
            all.add(feedback);
        }
        writeAll(all);
    }

    public Feedback findByAppointmentId(String apptId) {
        for (Feedback f : getAll()) {
            if (f.getAppointmentId().equals(apptId)) return f;
        }
        return null;
    }

    private void writeAll(List<Feedback> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Feedback f : list) {
                bw.write(f.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String generateNextId() {
        List<Feedback> all = getAll();
        int max = 0;
        for (Feedback f : all) {
            if (f.getFeedbackId() != null && f.getFeedbackId().startsWith("FB")) {
                try {
                    int n = Integer.parseInt(f.getFeedbackId().substring(2));
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("FB%03d", max + 1);
    }
}
