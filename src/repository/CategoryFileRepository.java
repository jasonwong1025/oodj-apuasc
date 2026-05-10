package repository;

import model.service.Category;
import utils.FileStorageHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            utils.Logger.error("Repository", "I/O Error", e);
        }
        return categories;
    }

    private Category parseLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 2) return null;
        return new Category(
            FileStorageHelper.unescape(parts[0]),
            FileStorageHelper.unescape(parts[1])
        );
    }

    public void writeAll(List<Category> categories) throws IOException {
        List<String> lines = categories.stream()
                .map(Category::toString)
                .collect(Collectors.toList());
        FileStorageHelper.writeAtomic(FILE_PATH, lines);
    }
}
