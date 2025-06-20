package inc.yowyob.rental_api.subscription.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Getter
@Setter
@Table("subscription_plans")
public class SubscriptionPlan {

    @Id
    @PrimaryKey
    private UUID id;

    @NotBlank(message = "Plan name is required")
    private String name; // GRATUIT, BASIC, PREMIUM, ENTERPRISE

    private String description;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be positive or zero")
    private BigDecimal price;

    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration must be positive")
    private Integer durationDays;

    @NotNull(message = "Max agencies is required")
    @PositiveOrZero(message = "Max agencies must be positive")
    private Integer maxAgencies;

    @NotNull(message = "Max vehicles is required")
    @PositiveOrZero(message = "Max vehicles must be positive")
    private Integer maxVehicles;

    @NotNull(message = "Max drivers is required")
    @PositiveOrZero(message = "Max drivers must be positive")
    private Integer maxDrivers;

    private Boolean hasGeofencing = false;
    private Boolean hasChat = false;
    private Boolean hasAdvancedReports = false;
    private Boolean hasApiAccess = false;
    private Boolean hasPrioritySupport = false;

    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public SubscriptionPlan() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }

    public SubscriptionPlan(String name, String description, BigDecimal price,
                            Integer durationDays, Integer maxAgencies, Integer maxVehicles,
                            Integer maxDrivers) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.price = price;
        this.durationDays = durationDays;
        this.maxAgencies = maxAgencies;
        this.maxVehicles = maxVehicles;
        this.maxDrivers = maxDrivers;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public boolean isTrialPlan() {
        return "GRATUIT".equalsIgnoreCase(this.name);
    }

    public boolean hasFeature(String feature) {
        return switch (feature.toLowerCase()) {
            case "geofencing" -> Boolean.TRUE.equals(this.hasGeofencing);
            case "chat" -> Boolean.TRUE.equals(this.hasChat);
            case "advanced_reports" -> Boolean.TRUE.equals(this.hasAdvancedReports);
            case "api_access" -> Boolean.TRUE.equals(this.hasApiAccess);
            case "priority_support" -> Boolean.TRUE.equals(this.hasPrioritySupport);
            default -> false;
        };
    }

    public BigDecimal calculateMonthlyPrice() {
        if (durationDays == null || durationDays == 0) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(30))
            .divide(BigDecimal.valueOf(durationDays), 2, java.math.RoundingMode.HALF_UP);
    }
}
