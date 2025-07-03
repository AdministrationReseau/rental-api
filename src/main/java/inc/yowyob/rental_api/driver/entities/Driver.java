// src/main/java/inc/yowyob/rental_api/driver/entities/Driver.java
// RENOMMEZ le fichier de DriverSession.java à Driver.java

package inc.yowyob.rental_api.driver.entities;

import inc.yowyob.rental_api.driver.enums.DriverStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("drivers")
public class Driver {

    @PrimaryKeyColumn(name = "driver_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID driverId;

    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Driver must be at least 18 years old")
    private Integer age;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "License type is required")
    private String licenseType;
    
    private String location;
    
    private String idCardUrl;
    private String driverLicenseUrl;

    private List<UUID> assignedVehicleIds;

    @NotNull(message = "Availability status is required")
    private Boolean available = true;

    @DecimalMin(value = "0.0") @DecimalMax(value = "5.0")
    private Double rating = 0.0;
    
    private String insuranceProvider;
    private String insurancePolicy;

    private DriverStatus status = DriverStatus.AVAILABLE;
    private LocalDateTime statusUpdatedAt;
    private UUID statusUpdatedBy;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}