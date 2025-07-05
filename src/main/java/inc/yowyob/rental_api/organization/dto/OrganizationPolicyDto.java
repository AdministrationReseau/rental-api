package inc.yowyob.rental_api.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO pour les politiques d'organisation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationPolicyDto {

    // Politiques de location
    @NotNull(message = "With driver option is required")
    private Boolean withDriverOption = true;

    @NotNull(message = "Without driver option is required")
    private Boolean withoutDriverOption = true;

    @NotNull(message = "Driver mandatory setting is required")
    private Boolean driverMandatory = false;

    // Paramètres de location
    @NotNull(message = "Minimum rental hours is required")
    @Min(value = 1, message = "Minimum rental hours must be at least 1")
    @Max(value = 168, message = "Minimum rental hours cannot exceed 168 (1 week)")
    private Integer minRentalHours = 1;

    @NotNull(message = "Maximum rental days is required")
    @Min(value = 1, message = "Maximum rental days must be at least 1")
    @Max(value = 365, message = "Maximum rental days cannot exceed 365 (1 year)")
    private Integer maxRentalDays = 30;

    @NotNull(message = "Security deposit is required")
    @PositiveOrZero(message = "Security deposit must be positive or zero")
    private BigDecimal securityDeposit = BigDecimal.ZERO;

    @NotNull(message = "Late return penalty is required")
    @PositiveOrZero(message = "Late return penalty must be positive or zero")
    private BigDecimal lateReturnPenalty = BigDecimal.ZERO;

    // Politiques client
    @NotNull(message = "Minimum driver age is required")
    @Min(value = 18, message = "Minimum driver age must be at least 18")
    @Max(value = 100, message = "Minimum driver age cannot exceed 100")
    private Integer minDriverAge = 21;

    @NotNull(message = "Maximum driver age is required")
    @Min(value = 18, message = "Maximum driver age must be at least 18")
    @Max(value = 100, message = "Maximum driver age cannot exceed 100")
    private Integer maxDriverAge = 75;

    @NotNull(message = "Require driver license setting is required")
    private Boolean requireDriverLicense = true;

    @NotNull(message = "Require credit card setting is required")
    private Boolean requireCreditCard = false;

    // Disponibilités
    @NotNull(message = "Allow weekend rental setting is required")
    private Boolean allowWeekendRental = true;

    @NotNull(message = "Allow holiday rental setting is required")
    private Boolean allowHolidayRental = true;

    // Politiques d'annulation
    @NotNull(message = "Cancellation policy is required")
    @Size(max = 1000, message = "Cancellation policy must not exceed 1000 characters")
    private String cancellationPolicy = "Annulation gratuite jusqu'à 24h avant la location";

    @NotNull(message = "Free cancellation hours is required")
    @Min(value = 0, message = "Free cancellation hours must be positive or zero")
    @Max(value = 168, message = "Free cancellation hours cannot exceed 168 (1 week)")
    private Integer freeCancellationHours = 24;

    @NotNull(message = "Cancellation fee percentage is required")
    @DecimalMin(value = "0.0", message = "Cancellation fee percentage must be positive or zero")
    @DecimalMax(value = "100.0", message = "Cancellation fee percentage cannot exceed 100")
    private BigDecimal cancellationFeePercentage = BigDecimal.ZERO;

    // Politiques de remboursement
    @NotNull(message = "Refund policy is required")
    @Size(max = 1000, message = "Refund policy must not exceed 1000 characters")
    private String refundPolicy = "Remboursement intégral selon les conditions d'annulation";

    @NotNull(message = "Refund processing days is required")
    @Min(value = 1, message = "Refund processing days must be at least 1")
    @Max(value = 30, message = "Refund processing days cannot exceed 30")
    private Integer refundProcessingDays = 7;

    // Validation personnalisée
    @AssertTrue(message = "At least one rental option (with or without driver) must be enabled")
    private boolean isValidRentalOptions() {
        return Boolean.TRUE.equals(withDriverOption) || Boolean.TRUE.equals(withoutDriverOption);
    }

    @AssertTrue(message = "Maximum driver age must be greater than minimum driver age")
    private boolean isValidDriverAgeRange() {
        return minDriverAge == null || maxDriverAge == null || maxDriverAge > minDriverAge;
    }

    @AssertTrue(message = "Maximum rental days must be greater than minimum rental hours converted to days")
    private boolean isValidRentalPeriod() {
        if (minRentalHours == null || maxRentalDays == null) {
            return true;
        }
        int minRentalDays = (int) Math.ceil(minRentalHours / 24.0);
        return maxRentalDays >= minRentalDays;
    }
}
