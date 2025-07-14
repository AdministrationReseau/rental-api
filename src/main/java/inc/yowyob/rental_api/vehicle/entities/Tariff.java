package inc.yowyob.rental_api.vehicle.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("tariffs")
public class Tariff {

    @PrimaryKey
    private TariffKey key;

    private String description;

    @NotNull
    @PositiveOrZero
    private BigDecimal pricePerHour;

    @NotNull
    @PositiveOrZero
    private BigDecimal pricePerDay;

    @PositiveOrZero
    private BigDecimal pricePerWeek; // For future use

    @NotBlank
    private String currency; // e.g., "XAF", "EUR", "USD"
    
    private boolean isDefault = false;

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
    public static class TariffKey implements Serializable {
        @PrimaryKeyColumn(name = "organization_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private UUID organizationId;
        @PrimaryKeyColumn(name = "id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
        private UUID id;
    }
}