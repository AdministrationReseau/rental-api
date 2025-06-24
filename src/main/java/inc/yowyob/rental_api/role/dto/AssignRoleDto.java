package inc.yowyob.rental_api.role.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour l'assignation d'un rôle à un utilisateur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleDto {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Role ID is required")
    private UUID roleId;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    private UUID agencyId; // Optionnel, pour les rôles limités à une agence

    private LocalDateTime expiresAt; // Optionnel, pour les assignations temporaires

    private String assignmentReason;
}
