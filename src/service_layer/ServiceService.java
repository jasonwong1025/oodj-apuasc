package service_layer;

import model.appointment.Appointment;
import model.service.Service;
import repository.AppointmentRepository;
import repository.ServiceFileRepository;
import utils.ValidationUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ServiceService {
    private final ServiceFileRepository repo;
    private final AppointmentRepository appointmentRepo;

    public ServiceService() {
        this.repo = new ServiceFileRepository();
        this.appointmentRepo = new AppointmentRepository();
    }

    public List<Service> listAll() {
        return repo.getAll();
    }

    public Service findById(String serviceId) {
        for (Service s : listAll()) {
            if (s.getServiceId().equals(serviceId)) return s;
        }
        return null;
    }

    public List<Service> filter(String searchText, String categoryFilter) {
        String q = searchText == null ? "" : searchText.trim().toLowerCase();
        return listAll().stream()
                .filter(s -> categoryFilter == null || "ALL".equals(categoryFilter)
                        || s.getCategoryId().equals(categoryFilter))
                .filter(s -> q.isEmpty()
                        || s.getServiceId().toLowerCase().contains(q)
                        || s.getServiceName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    /**
     * @return null on success, otherwise error message.
     */
    public String addService(String serviceName, String categoryId, String priceText) {
        if (!ValidationUtil.isNotEmpty(serviceName)) return "Service name is required.";
        if (!ValidationUtil.isNotEmpty(categoryId)) return "Category is required.";
        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price < 0) return "Price cannot be negative.";
        } catch (NumberFormatException e) {
            return "Price must be a valid number.";
        }

        List<Service> all = listAll();
        String trimmedName = serviceName.trim();
        if (serviceNameExists(all, trimmedName, null)) return "Service name already exists.";

        String newId = generateNextId(all);
        all.add(new Service(newId, trimmedName, categoryId, price, false));
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
    public String updateService(String serviceId, String serviceName, String categoryId, String priceText) {
        if (!ValidationUtil.isNotEmpty(serviceName)) return "Service name is required.";
        if (!ValidationUtil.isNotEmpty(categoryId)) return "Category is required.";
        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price < 0) return "Price cannot be negative.";
        } catch (NumberFormatException e) {
            return "Price must be a valid number.";
        }

        List<Service> all = listAll();
        Service target = null;
        for (Service s : all) {
            if (s.getServiceId().equals(serviceId)) {
                target = s;
                break;
            }
        }
        if (target == null) return "Service not found.";

        String trimmedName = serviceName.trim();
        if (serviceNameExists(all, trimmedName, serviceId)) return "Service name already exists.";

        target.setServiceName(trimmedName);
        target.setCategoryId(categoryId);
        target.setPrice(price);
        target.setIncludedInNormalService(false);
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
    public String deleteService(String serviceId) {
        List<Service> all = listAll();
        Service target = null;
        for (Service service : all) {
            if (service.getServiceId().equals(serviceId)) {
                target = service;
                break;
            }
        }
        if (target == null) return "Service not found.";

        if (hasOngoingAppointment(serviceId)) {
            return "Cannot delete service: it is used by an ongoing appointment.";
        }

        boolean removed = all.removeIf(s -> s.getServiceId().equals(serviceId));
        if (!removed) return "Service not found.";
        try {
            repo.writeAll(all);
        } catch (IOException e) {
            return "Failed to save: " + e.getMessage();
        }
        return null;
    }

    private String generateNextId(List<Service> all) {
        int max = 0;
        for (Service s : all) {
            if (s.getServiceId() != null && s.getServiceId().startsWith("SV")) {
                try {
                    int n = Integer.parseInt(s.getServiceId().substring(2));
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("SV%03d", max + 1);
    }

    private boolean serviceNameExists(List<Service> services, String serviceName, String excludedServiceId) {
        String normalizedName = normalizeServiceName(serviceName);
        for (Service service : services) {
            if (excludedServiceId != null && excludedServiceId.equals(service.getServiceId())) {
                continue;
            }
            if (normalizeServiceName(service.getServiceName()).equals(normalizedName)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOngoingAppointment(String serviceId) {
        for (Appointment appointment : appointmentRepo.getAllAppointments()) {
            if (isOngoingStatus(appointment.getStatus()) && appointmentIncludesService(appointment, serviceId)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOngoingStatus(String status) {
        return "PENDING".equalsIgnoreCase(status)
                || "CONFIRMED".equalsIgnoreCase(status)
                || "IN PROGRESS".equalsIgnoreCase(status);
    }

    private boolean appointmentIncludesService(Appointment appointment, String serviceId) {
        if (appointment == null || appointment.getServiceId() == null || serviceId == null) return false;
        String[] serviceIds = appointment.getServiceId().split(",");
        for (String appointmentServiceId : serviceIds) {
            if (serviceId.equalsIgnoreCase(appointmentServiceId.trim())) {
                return true;
            }
        }
        return false;
    }

    private String normalizeServiceName(String serviceName) {
        if (serviceName == null) return "";
        return serviceName.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
