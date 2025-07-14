package inc.yowyob.rental_api.vehicle.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("vehicles")
public class Vehicle {

    @PrimaryKey
    private VehicleKey key;

    @NotNull
    private UUID tariffId;

    @NotBlank
    private String registrationNumber;

    @NotBlank
    private String brand;

    @NotBlank
    private String model;

    @NotNull
    private Integer year;

    private String color;
    private Double mileage;

    @NotNull
    private VehicleStatus status;

    // Properties
    private String vehicleType;
    private String fuelType;
    private String transmission;
    private Integer seats;
    private Set<String> features;

    // --- Images & Documents ---
    private Set<UUID> imageIds;
    private Set<UUID> documentIds;

    // --- Audit ---
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @PrimaryKeyClass
    public static class VehicleKey implements Serializable {
        @PrimaryKeyColumn(name = "organization_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private UUID organizationId;
        @PrimaryKeyColumn(name = "agency_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
        private UUID agencyId;
        @PrimaryKeyColumn(name = "id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
        private UUID id;
    }
}