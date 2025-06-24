package inc.yowyob.rental_api.role.dto;

import inc.yowyob.rental_api.core.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO pour l'affichage des informations de rôle
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDto {

    private UUID id;
    private String name;
    private String description;
    private UUID organizationId;
    private RoleType roleType;
    private Boolean isSystemRole;
    private Boolean isDefaultRole;
    private Boolean isActive;
    private Integer priority;
    private Set<String> permissions;
    private String color;
    private String icon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // Informations calculées
    private Integer permissionCount;
    private Integer userCount; // Nombre d'utilisateurs ayant ce rôle
    private String summary;
}
