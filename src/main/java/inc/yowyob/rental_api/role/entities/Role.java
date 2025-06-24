package inc.yowyob.rental_api.role.entities;

import inc.yowyob.rental_api.core.enums.RoleType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entité représentant un rôle dans le système
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table("roles")
public class Role {

    @Id
    @PrimaryKey
    private UUID id;

    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    // Type de rôle (prédéfini ou personnalisé)
    private RoleType roleType;

    // Indique si c'est un rôle système (non modifiable)
    private Boolean isSystemRole = false;

    // Indique si c'est un rôle par défaut pour les nouveaux utilisateurs
    private Boolean isDefaultRole = false;

    // Indique si le rôle est actif
    private Boolean isActive = true;

    // Priorité du rôle (plus le nombre est élevé, plus le rôle est prioritaire)
    private Integer priority = 0;

    // Permissions associées au rôle (stockées comme Set de codes de permission)
    private Set<String> permissions = new HashSet<>();

    // Métadonnées du rôle
    private String color; // Couleur pour l'affichage dans l'interface
    private String icon;  // Icône pour l'affichage

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // Constructors
    public Role(String name, String description, UUID organizationId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.organizationId = organizationId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Role(String name, String description, UUID organizationId, RoleType roleType) {
        this(name, description, organizationId);
        this.roleType = roleType;
        this.isSystemRole = roleType != null && roleType.isSystemRole();
    }

    /**
     * Ajoute une permission au rôle
     */
    public void addPermission(String permissionCode) {
        if (permissionCode != null && !permissionCode.trim().isEmpty()) {
            if (this.permissions == null) {
                this.permissions = new HashSet<>();
            }
            this.permissions.add(permissionCode.trim());
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Retire une permission du rôle
     */
    public void removePermission(String permissionCode) {
        if (this.permissions != null && permissionCode != null) {
            this.permissions.remove(permissionCode.trim());
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Vérifie si le rôle a une permission spécifique
     */
    public boolean hasPermission(String permissionCode) {
        return this.permissions != null && this.permissions.contains(permissionCode);
    }

    /**
     * Ajoute plusieurs permissions au rôle
     */
    public void addPermissions(Set<String> permissionCodes) {
        if (permissionCodes != null && !permissionCodes.isEmpty()) {
            if (this.permissions == null) {
                this.permissions = new HashSet<>();
            }
            this.permissions.addAll(permissionCodes);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Remplace toutes les permissions par une nouvelle liste
     */
    public void setPermissions(Set<String> permissionCodes) {
        this.permissions = permissionCodes != null ? new HashSet<>(permissionCodes) : new HashSet<>();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Obtient le nombre de permissions
     */
    public int getPermissionCount() {
        return this.permissions != null ? this.permissions.size() : 0;
    }

    /**
     * Vérifie si le rôle peut être modifié
     */
    public boolean canBeModified() {
        return !Boolean.TRUE.equals(this.isSystemRole);
    }

    /**
     * Vérifie si le rôle peut être supprimé
     */
    public boolean canBeDeleted() {
        return !Boolean.TRUE.equals(this.isSystemRole) && !Boolean.TRUE.equals(this.isDefaultRole);
    }

    /**
     * Active le rôle
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Désactive le rôle
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marque le rôle comme rôle par défaut
     */
    public void setAsDefault() {
        this.isDefaultRole = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Retire le statut de rôle par défaut
     */
    public void unsetAsDefault() {
        this.isDefaultRole = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Met à jour les métadonnées du rôle
     */
    public void updateMetadata(String color, String icon) {
        this.color = color;
        this.icon = icon;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Clone le rôle avec un nouveau nom
     */
    public Role cloneWithNewName(String newName, UUID newOrganizationId) {
        Role clonedRole = new Role(newName, this.description, newOrganizationId);
        clonedRole.setPermissions(new HashSet<>(this.permissions));
        clonedRole.setPriority(this.priority);
        clonedRole.setColor(this.color);
        clonedRole.setIcon(this.icon);
        return clonedRole;
    }

    /**
     * Vérifie si le rôle est équivalent à un autre (mêmes permissions)
     */
    public boolean isEquivalentTo(Role otherRole) {
        if (otherRole == null) return false;
        Set<String> thisPermissions = this.permissions != null ? this.permissions : new HashSet<>();
        Set<String> otherPermissions = otherRole.permissions != null ? otherRole.permissions : new HashSet<>();
        return thisPermissions.equals(otherPermissions);
    }

    /**
     * Obtient un résumé du rôle
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Rôle: ").append(this.name);
        if (this.roleType != null) {
            summary.append(" (").append(this.roleType.getDescription()).append(")");
        }
        summary.append(" - ").append(getPermissionCount()).append(" permission(s)");
        if (Boolean.TRUE.equals(this.isSystemRole)) {
            summary.append(" [SYSTÈME]");
        }
        if (Boolean.TRUE.equals(this.isDefaultRole)) {
            summary.append(" [DÉFAUT]");
        }
        if (!Boolean.TRUE.equals(this.isActive)) {
            summary.append(" [INACTIF]");
        }
        return summary.toString();
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name.trim();
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPriority(Integer priority) {
        this.priority = priority != null ? Math.max(0, priority) : 0;
        this.updatedAt = LocalDateTime.now();
    }
}
