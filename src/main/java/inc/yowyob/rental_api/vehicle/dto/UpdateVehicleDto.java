package inc.yowyob.rental_api.vehicle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UpdateVehicleDto {

    private UUID tariffId;

    @Size(max = 20)
    private String registrationNumber;

    private String brand;
    private String model;

    @Min(1980)
    private Integer year;

    private String vehicleType;
    private String color;
    private Double mileage;
    private String fuelType;
    private String transmission;
    private Integer seats;
    private Set<String> features;
}