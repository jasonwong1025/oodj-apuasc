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
            utils.Logger.error("Repository", "I/O Error", e);
        }
        return services;
    }

    private Service parseLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 4) return null;
        try {
            double price = Double.parseDouble(parts[3].trim());
            return new Service(
                utils.FileStorageHelper.unescape(parts[0].trim()),
                utils.FileStorageHelper.unescape(parts[1].trim()),
                utils.FileStorageHelper.unescape(parts[2].trim()),
                price,
                false
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void writeAll(List<Service> services) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Service s : services) {
            lines.add(s.toString());
        }
        utils.FileStorageHelper.writeAtomic(FILE_PATH, lines);
    }
}
