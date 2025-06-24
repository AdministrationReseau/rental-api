package inc.yowyob.rental_api.role.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO pour l'affichage des permissions effectives d'un utilisateur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermissionsDto {

    private UUID userId;
    private UUID organizationId;
    private Set<String> effectivePermissions;
    private List<RoleDto> assignedRoles;
    private List<PermissionGroupDto> permissionGroups;
    private Boolean hasFullAccess;
    private String accessLevel; // ADMIN, MANAGER, USER, LIMITED
}
