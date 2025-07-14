package inc.yowyob.rental_api.vehicle.entities;

import lombok.Getter;

@Getter
public enum VehicleStatus {
    AVAILABLE("Available", "The vehicle is available for rent."),
    RENTED("Rented", "The vehicle is currently rented."),
    MAINTENANCE("In Maintenance", "The vehicle is undergoing maintenance."),
    OUT_OF_SERVICE("Out of Service", "The vehicle is temporarily out of service."),
    RETIRED("Retired", "The vehicle has been removed from the fleet.");

    private final String description;
    private final String details;

    VehicleStatus(String description, String details) {
        this.description = description;
        this.details = details;
    }
}