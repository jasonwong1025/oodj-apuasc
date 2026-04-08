package service_layer;

import model.vehicle.Vehicle;
import repository.VehicleRepository;
import utils.IdGenerator;
import java.util.List;

public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleService() {
        this.vehicleRepository = new VehicleRepository();
    }

    public List<Vehicle> getCustomerVehicles(String customerId) {
        return vehicleRepository.getVehiclesByOwner(customerId);
    }

    public void addVehicle(String ownerId, String plateNumber, String brand, String model) {
        String vehicleId = IdGenerator.generateId("VEH", "data/vehicles.txt");
        Vehicle vehicle = new Vehicle(vehicleId, ownerId, plateNumber, brand, model);
        vehicleRepository.save(vehicle);
    }

    public void updateVehicle(Vehicle vehicle) {
        vehicleRepository.update(vehicle);
    }

    public void deleteVehicle(String vehicleId) {
        vehicleRepository.delete(vehicleId);
    }
}
