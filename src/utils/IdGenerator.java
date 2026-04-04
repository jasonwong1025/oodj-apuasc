package utils;

import model.users.User;
import repository.UserFileRepository;
import java.util.List;

public class IdGenerator {
    public static String generateNextCustomerId() {
        UserFileRepository repo = new UserFileRepository();
        List<User> users = repo.getAllUsers();
        
        int maxId = 0;
        for (User user : users) {
            if ("Customer".equals(user.getRole()) && user.getUserId().startsWith("C")) {
                try {
                    int currentId = Integer.parseInt(user.getUserId().substring(1));
                    if (currentId > maxId) {
                        maxId = currentId;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("C%03d", maxId + 1);
    }
}
