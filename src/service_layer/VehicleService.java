package service_layer;

import model.vehicle.Vehicle;
import repository.VehicleRepository;
import utils.IdGenerator;
import utils.Result;
import utils.ValidationUtil;
import java.util.List;

public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleService() {
        this.vehicleRepository = new VehicleRepository();
    }

    public List<Vehicle> getCustomerVehicles(String customerId) {
        return vehicleRepository.getVehiclesByOwner(customerId);
    }

    public Result<Vehicle> addVehicle(String ownerId, String plateNumber, String brand, String model) {
        if (vehicleRepository.getVehiclesByPlate(plateNumber).isPresent()) {
            return Result.failure("Plate number " + plateNumber + " is already registered.");
        }
        
        Vehicle vehicle = new Vehicle(null, ownerId, plateNumber, brand, model);
        String violations = ValidationUtil.getViolations(vehicle);
        if (violations != null) {
            return Result.failure(violations);
        }

        vehicle.setVehicleId(IdGenerator.generateId("VEH", "data/vehicles.txt"));
        vehicleRepository.save(vehicle);
        return Result.success(vehicle);
    }

    public void updateVehicle(Vehicle vehicle) {
        vehicleRepository.update(vehicle);
    }

    public void deleteVehicle(String vehicleId) {
        vehicleRepository.delete(vehicleId);
    }
}
