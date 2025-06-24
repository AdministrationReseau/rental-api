package inc.yowyob.rental_api.role.dto;

import inc.yowyob.rental_api.core.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO pour les templates de rôles prédéfinis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleTemplateDto {

    private RoleType roleType;
    private String name;
    private String description;
    private Set<String> defaultPermissions;
    private Integer defaultPriority;
    private String defaultColor;
    private String defaultIcon;
    private Boolean isRecommended;
}
