package inc.yowyob.rental_api.role.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour grouper les permissions par ressource
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionGroupDto {

    private String resource;
    private String resourceLabel;
    private String description;
    private List<PermissionDto> permissions;
    private Integer totalPermissions;
    private Integer assignedPermissions;
}
