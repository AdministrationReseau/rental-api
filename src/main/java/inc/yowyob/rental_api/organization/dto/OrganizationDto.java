package inc.yowyob.rental_api.organization.dto;

import inc.yowyob.rental_api.core.enums.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour les organisations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDto {

    private UUID id;
    private String name;
    private OrganizationType organizationType;
    private String description;
    private UUID ownerId;

    // Informations légales
    private String registrationNumber;
    private String taxNumber;
    private String businessLicense;

    // Adresse
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String region;

    // Contact
    private String phone;
    private String email;
    private String website;

    // Politique de l'organisation
    private OrganizationPolicyDto policies;

    // Configuration
    private OrganizationSettingsDto settings;

    // Statut
    private Boolean isActive;
    private Boolean isVerified;

    // Limites et utilisation
    private Integer maxAgencies;
    private Integer maxVehicles;
    private Integer maxDrivers;
    private Integer maxUsers;

    private Integer currentAgencies;
    private Integer currentVehicles;
    private Integer currentDrivers;
    private Integer currentUsers;

    // Métadonnées
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // Informations calculées
    private Double agencyUsagePercentage;
    private Double vehicleUsagePercentage;
    private Double driverUsagePercentage;
    private Double userUsagePercentage;
    private String subscriptionPlan;
    private LocalDateTime subscriptionExpiresAt;

    // Statistiques rapides
    private Integer totalActiveAgencies;
    private Integer totalActiveVehicles;
    private Integer totalActiveDrivers;
    private Integer totalRentals;
}
