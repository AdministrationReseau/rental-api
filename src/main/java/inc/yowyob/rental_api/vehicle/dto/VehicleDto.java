package inc.yowyob.rental_api.vehicle.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class VehicleDto {
    private UUID id;
    private UUID organizationId;
    private UUID agencyId;
    private String registrationNumber;
    private String brand;
    private String model;
    private Integer year;
    private String color;
    private Double mileage;
    private String status;
    private String vehicleType;
    private String fuelType;
    private String transmission;
    private Integer seats;
    private Set<String> features;
    private TariffDto tariff;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}