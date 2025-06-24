package inc.yowyob.rental_api.role.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

/**
 * DTO pour la mise à jour des permissions d'un rôle
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionsDto {

    @NotNull(message = "Role ID is required")
    private UUID roleId;

    @NotNull(message = "Permissions are required")
    private Set<String> permissions;

    private String updateReason;
}
