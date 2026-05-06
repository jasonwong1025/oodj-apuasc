package repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import model.feedback.Review;

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
                String[] parts = line.split("\\|", -1);
                if (parts.length == 5) {
                    reviews.add(new Review(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public void save(Review review) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(review.toString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
