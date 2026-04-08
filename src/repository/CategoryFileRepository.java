package repository;

import model.service.Category;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryFileRepository {
    private static final String FILE_PATH = "data/categories.txt";

    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return categories;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Category c = parseLine(line);
                if (c != null) categories.add(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return categories;
    }

    private Category parseLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 2) return null;
        return new Category(parts[0].trim(), parts[1].trim());
    }

    public void writeAll(List<Category> categories) throws IOException {
        File file = new File(FILE_PATH);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (Category c : categories) {
                bw.write(c.toString());
                bw.newLine();
            }
        }
    }
}
