package inc.yowyob.rental_api.role.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour l'affichage des assignations utilisateur-rôle
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleDto {

    private UUID id;
    private UUID userId;
    private UUID roleId;
    private UUID organizationId;
    private UUID agencyId;
    private LocalDateTime assignedAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private String assignmentReason;
    private UUID assignedBy;

    // Informations du rôle (dénormalisées pour l'affichage)
    private String roleName;
    private String roleDescription;
    private String roleColor;
    private String roleIcon;

    // Informations de l'utilisateur (dénormalisées pour l'affichage)
    private String userFullName;
    private String userEmail;

    // Informations calculées
    private Boolean isExpired;
    private Long daysUntilExpiration;
    private Boolean isExpiringSoon;
}
