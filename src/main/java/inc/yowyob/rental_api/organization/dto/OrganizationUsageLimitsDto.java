package inc.yowyob.rental_api.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO pour les limites d'utilisation d'une organisation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationUsageLimitsDto {

    private UUID organizationId;

    // Limites maximales définies par l'abonnement
    private Integer maxAgencies;
    private Integer maxVehicles;
    private Integer maxDrivers;
    private Integer maxUsers;

    // Utilisation actuelle
    private Integer currentAgencies;
    private Integer currentVehicles;
    private Integer currentDrivers;
    private Integer currentUsers;

    // Pourcentages d'utilisation
    private Double agencyUsagePercentage;
    private Double vehicleUsagePercentage;
    private Double driverUsagePercentage;
    private Double userUsagePercentage;

    // Indicateurs de capacité
    private Boolean canCreateAgency;
    private Boolean canCreateVehicle;
    private Boolean canCreateDriver;
    private Boolean canCreateUser;

    // Informations calculées
    private Integer remainingAgencies;
    private Integer remainingVehicles;
    private Integer remainingDrivers;
    private Integer remainingUsers;

    // Getters calculés
    public Integer getRemainingAgencies() {
        if (maxAgencies == null || currentAgencies == null) return 0;
        return Math.max(0, maxAgencies - currentAgencies);
    }

    public Integer getRemainingVehicles() {
        if (maxVehicles == null || currentVehicles == null) return 0;
        return Math.max(0, maxVehicles - currentVehicles);
    }

    public Integer getRemainingDrivers() {
        if (maxDrivers == null || currentDrivers == null) return 0;
        return Math.max(0, maxDrivers - currentDrivers);
    }

    public Integer getRemainingUsers() {
        if (maxUsers == null || currentUsers == null) return 0;
        return Math.max(0, maxUsers - currentUsers);
    }
}
