package inc.yowyob.rental_api.driver.dto;

import inc.yowyob.rental_api.driver.enums.DriverStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO pour la mise à jour des informations d'un chauffeur.
 * Tous les champs sont optionnels pour permettre des mises à jour partielles (PATCH).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDriverDto {

    @Min(value = 18, message = "L'âge doit être d'au moins 18 ans.")
    private Integer age;

    private String licenseNumber;

    private String licenseType;

    /**
     * La localisation actuelle du chauffeur (ex: "Douala, Bonapriso").
     */
    private String location;

    private String idCardUrl;

    private String driverLicenseUrl;

    /**
     * Liste des IDs de véhicules actuellement assignés.
     */
    private List<UUID> assignedVehicleIds;

    /**
     * Disponibilité du chauffeur pour de nouvelles courses.
     */
    private Boolean available;

    /**
     * La note du chauffeur. Note: Il est généralement déconseillé de permettre
     * une mise à jour directe. Elle devrait être calculée.
     * Laisser ici pour flexibilité si un admin doit l'ajuster manuellement.
     */
    @DecimalMin(value = "0.0", message = "La note doit être positive.")
    @DecimalMax(value = "5.0", message = "La note ne peut pas dépasser 5.0.")
    private Double rating;

    private String insuranceProvider;

    private String insurancePolicy;

    /**
     * Statut opérationnel du chauffeur (disponible, en course, hors service...).
     */
    private DriverStatus status;
}