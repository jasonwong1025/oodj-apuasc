package repository;

import model.review.Review;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
                if (parts.length == 6) {
                    reviews.add(new Review(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]), parts[4], parts[5]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public List<Review> getReviewsByCustomer(String customerId) {
        List<Review> all = getAllReviews();
        List<Review> filtered = new ArrayList<>();
        for (Review r : all) {
            if (r.getCustomerId().equals(customerId)) {
                filtered.add(r);
            }
        }
        return filtered;
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
