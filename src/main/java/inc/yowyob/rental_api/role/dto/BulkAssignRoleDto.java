package inc.yowyob.rental_api.role.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO pour l'assignation en masse de rôles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkAssignRoleDto {

    @NotEmpty(message = "User IDs list cannot be empty")
    private List<UUID> userIds;

    @NotNull(message = "Role ID is required")
    private UUID roleId;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    private UUID agencyId;
    private LocalDateTime expiresAt;
    private String assignmentReason;
}
