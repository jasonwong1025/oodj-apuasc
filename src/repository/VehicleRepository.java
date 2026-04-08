package repository;

import model.vehicle.Vehicle;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleRepository {
    private static final String FILE_PATH = "data/vehicles.txt";

    public Optional<Vehicle> getVehiclesByPlate(String plate) {
        return getAllVehicles().stream()
                .filter(v -> v.getPlateNumber().equalsIgnoreCase(plate.trim()))
                .findFirst();
    }

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return vehicles;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length == 5) {
                    vehicles.add(new Vehicle(parts[0], parts[1], parts[2], parts[3], parts[4]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    public List<Vehicle> getVehiclesByOwner(String ownerId) {
        List<Vehicle> all = getAllVehicles();
        List<Vehicle> filtered = new ArrayList<>();
        for (Vehicle v : all) {
            if (v.getOwnerId().equals(ownerId)) {
                filtered.add(v);
            }
        }
        return filtered;
    }

    public void save(Vehicle vehicle) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(vehicle.toString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(Vehicle updatedVehicle) {
        List<Vehicle> vehicles = getAllVehicles();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Vehicle v : vehicles) {
                if (v.getVehicleId().equals(updatedVehicle.getVehicleId())) {
                    bw.write(updatedVehicle.toString());
                } else {
                    bw.write(v.toString());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete(String vehicleId) {
        List<Vehicle> vehicles = getAllVehicles();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Vehicle v : vehicles) {
                if (!v.getVehicleId().equals(vehicleId)) {
                    bw.write(v.toString());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
