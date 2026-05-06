package repository;

import model.service.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFileRepository {
    private static final String FILE_PATH = "data/services.txt";

    public List<Service> getAll() {
        List<Service> services = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return services;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Service s = parseLine(line);
                if (s != null) services.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return services;
    }

    private Service parseLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 4) return null;
        try {
            double price = Double.parseDouble(parts[3].trim());
            return new Service(parts[0].trim(), parts[1].trim(), parts[2].trim(), price, false);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void writeAll(List<Service> services) throws IOException {
        File file = new File(FILE_PATH);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (Service s : services) {
                bw.write(s.toString());
                bw.newLine();
            }
        }
    }
}
