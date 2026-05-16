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
            utils.Logger.error("Repository", "I/O Error", e);
        }
        return list;
    }

    public void addOrUpdate(Feedback feedback) {
        List<Feedback> all = getAll();
        boolean found = false;
        for (Feedback f : all) {
            if (f.getAppointmentId().equals(feedback.getAppointmentId())) {
                f.setDescription(feedback.getDescription());
                f.setDateTime(feedback.getDateTime());
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
        List<String> lines = new ArrayList<>();
        for (Feedback f : list) {
            lines.add(f.toString());
        }
        try {
            utils.FileStorageHelper.writeAtomic(FILE_PATH, lines);
        } catch (IOException e) {
            utils.Logger.error("Repository", "I/O Error", e);
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


        private Feedback parseLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 4) return null;
        return new Feedback(
            utils.FileStorageHelper.unescape(parts[0].trim()),
            utils.FileStorageHelper.unescape(parts[1].trim()),
            utils.FileStorageHelper.unescape(parts[2].trim()),
            utils.FileStorageHelper.unescape(parts[3].trim())
        );
    }
}
