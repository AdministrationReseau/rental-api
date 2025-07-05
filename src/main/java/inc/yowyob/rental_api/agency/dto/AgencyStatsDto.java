package inc.yowyob.rental_api.agency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO pour les statistiques d'agence
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgencyStatsDto {

    private UUID agencyId;
    private String agencyName;

    // Statistiques des ressources
    private Integer totalVehicles;
    private Integer availableVehicles;
    private Integer totalDrivers;
    private Integer availableDrivers;
    private Integer totalStaff;

    // Statistiques des locations
    private Integer totalRentals;
    private Integer activeRentals;
    private Integer completedRentals;
    private Integer cancelledRentals;

    // Taux d'occupation
    private Double vehicleOccupancyRate;
    private Double driverOccupancyRate;

    // Revenus (optionnel selon les permissions)
    private java.math.BigDecimal monthlyRevenue;
    private java.math.BigDecimal yearlyRevenue;

    // Période des statistiques
    private java.time.LocalDateTime periodStart;
    private java.time.LocalDateTime periodEnd;
}
