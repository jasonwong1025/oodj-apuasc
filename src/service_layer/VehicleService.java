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

    public String addVehicle(String ownerId, String plateNumber, String brand, String model) {
        if (vehicleRepository.getVehiclesByPlate(plateNumber).isPresent()) {
            return "Plate number " + plateNumber + " is already registered.";
        }
        String vehicleId = IdGenerator.generateId("VEH", "data/vehicles.txt");
        Vehicle vehicle = new Vehicle(vehicleId, ownerId, plateNumber, brand, model);
        vehicleRepository.save(vehicle);
        return null; // Success
    }

    public void updateVehicle(Vehicle vehicle) {
        vehicleRepository.update(vehicle);
    }

    public void deleteVehicle(String vehicleId) {
        vehicleRepository.delete(vehicleId);
    }
}
