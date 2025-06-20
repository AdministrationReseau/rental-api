package inc.yowyob.rental_api.onboarding.dto;

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

    @NotNull(message = "With driver option is required")
    private Boolean withDriverOption = true;

    @NotNull(message = "Without driver option is required")
    private Boolean withoutDriverOption = true;

    @NotNull(message = "Driver mandatory setting is required")
    private Boolean driverMandatory = false;

    @NotNull(message = "Minimum rental hours is required")
    @Min(value = 1, message = "Minimum rental hours must be at least 1")
    @Max(value = 168, message = "Minimum rental hours cannot exceed 168 (1 week)")
    private Integer minRentalHours = 1;

    @NotNull(message = "Security deposit is required")
    @PositiveOrZero(message = "Security deposit must be positive or zero")
    private BigDecimal securityDeposit = BigDecimal.ZERO;

    @NotNull(message = "Late return penalty is required")
    @PositiveOrZero(message = "Late return penalty must be positive or zero")
    private BigDecimal lateReturnPenalty = BigDecimal.ZERO;

    @NotNull(message = "Cancellation policy is required")
    @Size(max = 1000, message = "Cancellation policy must not exceed 1000 characters")
    private String cancellationPolicy = "Annulation gratuite jusqu'à 24h avant la location";

    private Boolean allowWeekendRental = true;
    private Boolean allowHolidayRental = true;
    private Boolean requireDriverLicense = true;
    private Integer minDriverAge = 21;
    private Integer maxDriverAge = 75;
}
