package service_layer;

import model.service.Category;
import model.service.Service;
import repository.CategoryFileRepository;
import repository.ServiceFileRepository;
import utils.ValidationUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryService {
    private final CategoryFileRepository repo;
    private final ServiceFileRepository serviceRepo;

    public CategoryService() {
        this.repo = new CategoryFileRepository();
        this.serviceRepo = new ServiceFileRepository();
    }

    public List<Category> listAll() {
        return repo.getAll();
    }

    public Category findById(String categoryId) {
        for (Category c : listAll()) {
            if (c.getCategoryId().equals(categoryId)) return c;
        }
        return null;
    }

    public List<String> listCategoryNames() {
        return listAll().stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toList());
    }

    public String getCategoryNameById(String categoryId) {
        Category c = findById(categoryId);
        return c == null ? null : c.getCategoryName();
    }

    public List<Category> filter(String searchText) {
        String q = searchText == null ? "" : searchText.trim().toLowerCase();
        return listAll().stream()
                .filter(c -> q.isEmpty()
                        || c.getCategoryId().toLowerCase().contains(q)
                        || c.getCategoryName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    /**
     * @return null on success, otherwise error message.
     */
    public String addCategory(String categoryName) {
        if (!ValidationUtil.isNotEmpty(categoryName)) return "Category name is required.";
        List<Category> all = listAll();
        for (Category c : all) {
            if (c.getCategoryName().equalsIgnoreCase(categoryName.trim())) {
                return "Category already exists.";
            }
        }
        String newId = generateNextId(all);
        all.add(new Category(newId, categoryName.trim()));
        try {
            repo.writeAll(all);
        } catch (IOException e) {
            return "Failed to save: " + e.getMessage();
        }
        return null;
    }

    /**
     * @return null on success, otherwise error message.
     */
    public String updateCategory(String categoryId, String newName) {
        if (!ValidationUtil.isNotEmpty(newName)) return "Category name is required.";
        List<Category> all = listAll();
        Category target = null;
        for (Category c : all) {
            if (c.getCategoryId().equals(categoryId)) {
                target = c;
                break;
            }
        }
        if (target == null) return "Category not found.";
        for (Category c : all) {
            if (!c.getCategoryId().equals(categoryId) && c.getCategoryName().equalsIgnoreCase(newName.trim())) {
                return "Category name already exists.";
            }
        }
        target.setCategoryName(newName.trim());
        try {
            repo.writeAll(all);
        } catch (IOException e) {
            return "Failed to save: " + e.getMessage();
        }
        return null;
    }

    /**
     * @return null on success, otherwise error message.
     */
    public String deleteCategory(String categoryId) {
        List<Category> all = listAll();
        Category target = null;
        for (Category c : all) {
            if (c.getCategoryId().equals(categoryId)) {
                target = c;
                break;
            }
        }
        if (target == null) return "Category not found.";

        for (Service s : serviceRepo.getAll()) {
            if (target.getCategoryId().equalsIgnoreCase(s.getCategoryId())) {
                return "Cannot delete category: it is associated with existing services.";
            }
        }

        boolean removed = all.removeIf(c -> c.getCategoryId().equals(categoryId));
        if (!removed) return "Category not found.";
        try {
            repo.writeAll(all);
        } catch (IOException e) {
            return "Failed to save: " + e.getMessage();
        }
        return null;
    }

    private String generateNextId(List<Category> all) {
        int max = 0;
        for (Category c : all) {
            if (c.getCategoryId() != null && c.getCategoryId().startsWith("CAT")) {
                try {
                    int n = Integer.parseInt(c.getCategoryId().substring(3));
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("CAT%03d", max + 1);
    }
}
