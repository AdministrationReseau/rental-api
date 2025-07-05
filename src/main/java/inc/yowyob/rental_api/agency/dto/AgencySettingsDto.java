package inc.yowyob.rental_api.agency.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les paramètres d'agence
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgencySettingsDto {

    // Paramètres opérationnels
    @NotNull(message = "Allow self service setting is required")
    private Boolean allowSelfService = false;

    @NotNull(message = "Require inspection setting is required")
    private Boolean requireInspection = true;

    @NotNull(message = "Enable key box setting is required")
    private Boolean enableKeyBox = false;

    // Notifications
    @NotNull(message = "Notify on reservation setting is required")
    private Boolean notifyOnReservation = true;

    @NotNull(message = "Notify on return setting is required")
    private Boolean notifyOnReturn = true;

    @NotNull(message = "Notify on late return setting is required")
    private Boolean notifyOnLateReturn = true;

    // Géofencing (si activé au niveau organisation)
    @NotNull(message = "Enable geofence alerts setting is required")
    private Boolean enableGeofenceAlerts = false;

    @NotNull(message = "Track vehicles real time setting is required")
    private Boolean trackVehiclesRealTime = false;

    // Politiques spécifiques à l'agence
    @NotNull(message = "Allow instant booking setting is required")
    private Boolean allowInstantBooking = true;

    @NotNull(message = "Advance booking days is required")
    @Min(value = 1, message = "Advance booking days must be at least 1")
    @Max(value = 365, message = "Advance booking days cannot exceed 365")
    private Integer advanceBookingDays = 30;

    @NotNull(message = "Minimum booking notice hours is required")
    @Min(value = 0, message = "Minimum booking notice hours must be positive or zero")
    @Max(value = 168, message = "Minimum booking notice hours cannot exceed 168 (1 week)")
    private Integer minBookingNoticeHours = 2;
}
