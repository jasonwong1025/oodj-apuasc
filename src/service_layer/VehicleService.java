package service_layer;

import java.util.List;
import model.appointment.Appointment;
import model.vehicle.Vehicle;
import repository.AppointmentRepository;
import repository.VehicleRepository;
import utils.IdGenerator;
import utils.Result;
import utils.ValidationUtil;

public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final AppointmentRepository appointmentRepository;

    public VehicleService() {
        this.vehicleRepository = new VehicleRepository();
        this.appointmentRepository = new AppointmentRepository();
    }

    public List<Vehicle> getCustomerVehicles(String customerId) {
        return vehicleRepository.getVehiclesByOwner(customerId);
    }

    public void addVehicle(Vehicle v) {
        vehicleRepository.save(v);
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

    public Result<Vehicle> updateVehicle(String ownerId, String vehicleId, String plateNumber, String brand, String model) {
        Vehicle existing = findById(vehicleId);
        if (existing == null || !existing.getOwnerId().equals(ownerId)) {
            return Result.failure("Vehicle not found.");
        }

        String normalizedPlate = plateNumber == null ? null : plateNumber.trim();
        String normalizedBrand = brand == null ? null : brand.trim();
        String normalizedModel = model == null ? null : model.trim();

        for (Vehicle v : vehicleRepository.getAllVehicles()) {
            if (v.getVehicleId().equals(vehicleId)) continue;
            if (v.getPlateNumber().equalsIgnoreCase(normalizedPlate)) {
                return Result.failure("Plate number " + normalizedPlate + " is already registered.");
            }
        }

        Vehicle updated = new Vehicle(vehicleId, ownerId, normalizedPlate, normalizedBrand, normalizedModel);
        String violations = ValidationUtil.getViolations(updated);
        if (violations != null) {
            return Result.failure(violations);
        }

        vehicleRepository.update(updated);
        return Result.success(updated);
    }

    public void deleteVehicle(String vehicleId) {
        vehicleRepository.delete(vehicleId);
    }

    /**
     * @return null on success, otherwise error message.
     */
    public Result<Void> deleteVehicleForCustomer(String ownerId, String vehicleId) {
        Vehicle existing = findById(vehicleId);
        if (existing == null || !existing.getOwnerId().equals(ownerId)) {
            return Result.failure("Vehicle not found.");
        }

        for (Appointment a : appointmentRepository.getAllAppointments()) {
            if (vehicleId.equals(a.getVehicleId()) && "PENDING".equalsIgnoreCase(a.getStatus())) {
                return Result.failure("Cannot delete this vehicle because it has pending appointment(s).");
            }
        }

        vehicleRepository.delete(vehicleId);
        return Result.success(null);
    }

    public Vehicle findById(String vehicleId) {
        return vehicleRepository.getAllVehicles().stream()
                .filter(v -> v.getVehicleId().equals(vehicleId))
                .findFirst()
                .orElse(null);
    }
}
