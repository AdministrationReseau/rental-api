package inc.yowyob.rental_api.role.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les statistiques des rôles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleStatsDto {

    private Long totalRoles;
    private Long activeRoles;
    private Long inactiveRoles;
    private Long systemRoles;
    private Long customRoles;
    private Long defaultRoles;

    private Long totalUserRoles;
    private Long activeUserRoles;
    private Long expiredUserRoles;
    private Long expiringSoonUserRoles;

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // Statistiques par organisation
    private Long rolesCreatedThisPeriod;
    private Long rolesModifiedThisPeriod;
    private Long assignmentsThisPeriod;

    // Moyennes
    private Double averagePermissionsPerRole;
    private Double averageUsersPerRole;
}
