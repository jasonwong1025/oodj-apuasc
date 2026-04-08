package model.vehicle;

public class Vehicle {
    private String vehicleId;
    private String ownerId;
    private String plateNumber;
    private String brand;
    private String model;

    public Vehicle() {}

    public Vehicle(String vehicleId, String ownerId, String plateNumber, String brand, String model) {
        this.vehicleId = vehicleId;
        this.ownerId = ownerId;
        this.plateNumber = plateNumber;
        this.brand = brand;
        this.model = model;
    }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    @Override
    public String toString() {
        return String.join("|", vehicleId, ownerId, plateNumber, brand, model);
    }
}
