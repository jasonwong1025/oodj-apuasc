package model.service;

public class Service {
    private String serviceId;
    private String serviceName;
    private String categoryId;
    private double price;
    private boolean includedInNormalService;

    public Service() {}

    public Service(String serviceId, String serviceName, String categoryId, double price, boolean includedInNormalService) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.categoryId = categoryId;
        this.price = price;
        this.includedInNormalService = includedInNormalService;
    }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    // Backward-compatible aliases used by existing UI code.
    public String getCategory() { return categoryId; }
    public void setCategory(String category) { this.categoryId = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public boolean isIncludedInNormalService() { return includedInNormalService; }
    public void setIncludedInNormalService(boolean includedInNormalService) { this.includedInNormalService = includedInNormalService; }

    @Override
    public String toString() {
        return utils.FileStorageHelper.join(serviceId, serviceName, categoryId, String.format("%.2f", price),
                includedInNormalService ? "YES" : "NO");
    }
}
