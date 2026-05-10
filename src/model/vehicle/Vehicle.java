package model.vehicle;

import java.io.Serializable;
import utils.validation.NotBlank;
import utils.validation.Pattern;

public class Vehicle implements Serializable {
    private String vehicleId;
    private String ownerId;

    @NotBlank(message = "is required")
    @Pattern(regexp = "^[A-Z0-9 ]{3,10}$", message = "must be valid (3-10 chars, uppercase and numbers)")
    private String plateNumber;

    @NotBlank(message = "is required")
    private String brand;

    @NotBlank(message = "is required")
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
        return utils.FileStorageHelper.join(vehicleId, ownerId, plateNumber, brand, model);
    }
}
