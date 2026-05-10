package repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import model.feedback.Review;
import utils.FileStorageHelper;

public class ReviewRepository {
    private static final String FILE_PATH = "data/reviews.txt";

    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return reviews;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length >= 5) {
                    reviews.add(new Review(
                        FileStorageHelper.unescape(p[0]),
                        FileStorageHelper.unescape(p[1]),
                        Integer.parseInt(FileStorageHelper.unescape(p[2])),
                        FileStorageHelper.unescape(p[3]),
                        FileStorageHelper.unescape(p[4])
                    ));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public void save(Review review) {
        try {
            FileStorageHelper.appendLine(FILE_PATH, review.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
