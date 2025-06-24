package inc.yowyob.rental_api.role.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité d'association entre utilisateur et rôle
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table("user_roles")
public class UserRole {

    @Id
    @PrimaryKey
    private UUID id;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Role ID is required")
    private UUID roleId;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    // Agence spécifique (optionnel, pour les rôles limités à une agence)
    private UUID agencyId;

    // Date d'assignation du rôle
    private LocalDateTime assignedAt;

    // Date d'expiration du rôle (optionnel)
    private LocalDateTime expiresAt;

    // Indique si l'assignation est active
    private Boolean isActive = true;

    // Raison de l'assignation
    private String assignmentReason;

    // Audit
    private UUID assignedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public UserRole(UUID userId, UUID roleId, UUID organizationId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.roleId = roleId;
        this.organizationId = organizationId;
        this.assignedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UserRole(UUID userId, UUID roleId, UUID organizationId, UUID agencyId) {
        this(userId, roleId, organizationId);
        this.agencyId = agencyId;
    }

    /**
     * Vérifie si l'assignation est encore valide
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return Boolean.TRUE.equals(this.isActive) &&
            (this.expiresAt == null || now.isBefore(this.expiresAt));
    }

    /**
     * Vérifie si l'assignation a expiré
     */
    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Définit une date d'expiration
     */
    public void setExpiration(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Prolonge l'assignation
     */
    public void extend(int days) {
        if (this.expiresAt != null) {
            this.expiresAt = this.expiresAt.plusDays(days);
        } else {
            this.expiresAt = LocalDateTime.now().plusDays(days);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Révoque l'assignation du rôle
     */
    public void revoke() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Réactive l'assignation du rôle
     */
    public void reactivate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Vérifie si l'assignation est limitée à une agence
     */
    public boolean isAgencySpecific() {
        return this.agencyId != null;
    }

    /**
     * Obtient le nombre de jours restants avant expiration
     */
    public long getDaysUntilExpiration() {
        if (this.expiresAt == null) {
            return Long.MAX_VALUE; // Pas d'expiration
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), this.expiresAt);
    }

    /**
     * Vérifie si l'assignation expire bientôt
     */
    public boolean isExpiringSoon(int days) {
        long daysUntilExpiration = getDaysUntilExpiration();
        return daysUntilExpiration <= days && daysUntilExpiration > 0;
    }
}
