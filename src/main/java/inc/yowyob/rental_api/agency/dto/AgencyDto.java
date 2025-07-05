package inc.yowyob.rental_api.agency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO de réponse pour les agences
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyDto {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;

    // Adresse
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String region;

    // Contact
    private String phone;
    private String email;

    // Géolocalisation
    private GeoLocationDto geoLocation;

    // Gestionnaire
    private UUID managerId;
    private String managerName; // Nom du gestionnaire pour affichage

    // Horaires d'ouverture
    private Map<String, WorkingHoursDto> workingHours;

    // Statut
    private Boolean isActive;
    private Boolean is24Hours;

    // Statistiques
    private Integer currentVehicles;
    private Integer currentDrivers;
    private Integer currentStaff;

    // Zone de géofencing
    private String geofenceZoneId;
    private Double geofenceRadius;

    // Configuration
    private AgencySettingsDto settings;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // Informations calculées
    private String fullAddress;
    private Boolean isOpenNow;
    private Double distanceFromUser; // Distance en km depuis la position de l'utilisateur
    private String organizationName; // Nom de l'organisation pour affichage

    // Statistiques rapides
    private Integer totalRentals;
    private Integer activeRentals;
    private Integer availableVehicles;
    private Integer availableDrivers;
}
