package inc.yowyob.rental_api.agency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.Map;
import java.util.UUID;

/**
 * DTO pour la mise à jour d'une agence
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAgencyDto {

    @Size(min = 2, max = 100, message = "Agency name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    // Adresse
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Size(max = 100, message = "Region must not exceed 100 characters")
    private String region;

    // Contact
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    private String phone;

    @Email(message = "Email should be valid")
    private String email;

    // Géolocalisation
    private GeoLocationDto geoLocation;

    // Gestionnaire de l'agence
    private UUID managerId;

    // Horaires d'ouverture
    private Map<String, WorkingHoursDto> workingHours;

    // Configuration
    private Boolean is24Hours;
    private Boolean isActive;

    // Zone de géofencing
    private String geofenceZoneId;

    @PositiveOrZero(message = "Geofence radius must be positive or zero")
    private Double geofenceRadius;

    // Configuration
    private AgencySettingsDto settings;
}
