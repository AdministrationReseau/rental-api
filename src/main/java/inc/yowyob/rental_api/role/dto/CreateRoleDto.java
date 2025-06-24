package inc.yowyob.rental_api.role.dto;

import inc.yowyob.rental_api.core.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.UUID;

/**
 * DTO pour la création d'un nouveau rôle
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleDto {

    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    private RoleType roleType;

    @Min(value = 0, message = "Priority must be positive")
    @Max(value = 100, message = "Priority must not exceed 100")
    private Integer priority = 0;

    private Set<String> permissions;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color")
    private String color;

    private String icon;

    private Boolean isDefaultRole = false;
}
