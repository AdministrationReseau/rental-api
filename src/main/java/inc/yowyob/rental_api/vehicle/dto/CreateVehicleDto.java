package inc.yowyob.rental_api.vehicle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class CreateVehicleDto {
    @NotNull
    private UUID agencyId;

    @NotNull
    private UUID tariffId;

    @NotBlank
    @Size(max = 20)
    private String registrationNumber;

    @NotBlank
    private String brand;

    @NotBlank
    private String model;

    @NotNull
    @Min(1980)
    private Integer year;

    @NotBlank
    private String vehicleType;

    // Optional fields
    private String color;
    private Double mileage;
    private String fuelType;
    private String transmission;
    private Integer seats;
    private Set<String> features;
}