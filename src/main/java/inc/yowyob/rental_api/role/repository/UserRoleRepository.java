package inc.yowyob.rental_api.role.repository;

import inc.yowyob.rental_api.role.entities.UserRole;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends CassandraRepository<UserRole, UUID> {

    /**
     * Trouve tous les rôles d'un utilisateur
     */
    @Query("SELECT * FROM user_roles WHERE user_id = ?0 ALLOW FILTERING")
    List<UserRole> findByUserId(UUID userId);

    /**
     * Trouve les rôles actifs d'un utilisateur
     */
    @Query("SELECT * FROM user_roles WHERE user_id = ?0 AND is_active = true ALLOW FILTERING")
    List<UserRole> findActiveByUserId(UUID userId);

    /**
     * Trouve les rôles valides d'un utilisateur (actifs et non expirés)
     */
    @Query("SELECT * FROM user_roles WHERE user_id = ?0 AND is_active = true AND (expires_at IS NULL OR expires_at > ?1) ALLOW FILTERING")
    List<UserRole> findValidByUserId(UUID userId, LocalDateTime now);

    /**
     * Trouve les utilisateurs ayant un rôle spécifique
     */
    @Query("SELECT * FROM user_roles WHERE role_id = ?0 AND is_active = true ALLOW FILTERING")
    List<UserRole> findActiveByRoleId(UUID roleId);

    /**
     * Trouve les rôles d'un utilisateur dans une organisation
     */
    @Query("SELECT * FROM user_roles WHERE user_id = ?0 AND organization_id = ?1 ALLOW FILTERING")
    List<UserRole> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    /**
     * Trouve les rôles actifs d'un utilisateur dans une organisation
     */
    @Query("SELECT * FROM user_roles WHERE user_id = ?0 AND organization_id = ?1 AND is_active = true ALLOW FILTERING")
    List<UserRole> findActiveByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    /**
     * Trouve une assignation spécifique utilisateur-rôle
     */
    @Query("SELECT * FROM user_roles WHERE user_id = ?0 AND role_id = ?1 ALLOW FILTERING")
    Optional<UserRole> findByUserIdAndRoleId(UUID userId, UUID roleId);

    /**
     * Trouve une assignation active spécifique utilisateur-rôle
     */
    @Query("SELECT * FROM user_roles WHERE user_id = ?0 AND role_id = ?1 AND is_active = true ALLOW FILTERING")
    Optional<UserRole> findActiveByUserIdAndRoleId(UUID userId, UUID roleId);

    /**
     * Trouve les rôles d'un utilisateur dans une agence spécifique
     */
    @Query("SELECT * FROM user_roles WHERE user_id = ?0 AND agency_id = ?1 AND is_active = true ALLOW FILTERING")
    List<UserRole> findActiveByUserIdAndAgencyId(UUID userId, UUID agencyId);

    /**
     * Trouve tous les utilisateurs d'une organisation avec leurs rôles
     */
    @Query("SELECT * FROM user_roles WHERE organization_id = ?0 AND is_active = true ALLOW FILTERING")
    List<UserRole> findActiveByOrganizationId(UUID organizationId);

    /**
     * Trouve tous les utilisateurs d'une agence avec leurs rôles
     */
    @Query("SELECT * FROM user_roles WHERE agency_id = ?0 AND is_active = true ALLOW FILTERING")
    List<UserRole> findActiveByAgencyId(UUID agencyId);

    /**
     * Trouve les assignations qui expirent bientôt
     */
    @Query("SELECT * FROM user_roles WHERE is_active = true AND expires_at IS NOT NULL AND expires_at <= ?0 AND expires_at > ?1 ALLOW FILTERING")
    List<UserRole> findExpiringSoon(LocalDateTime expirationThreshold, LocalDateTime now);

    /**
     * Trouve les assignations expirées
     */
    @Query("SELECT * FROM user_roles WHERE is_active = true AND expires_at IS NOT NULL AND expires_at < ?0 ALLOW FILTERING")
    List<UserRole> findExpired(LocalDateTime now);

    /**
     * Trouve les assignations créées par un utilisateur spécifique
     */
    @Query("SELECT * FROM user_roles WHERE assigned_by = ?0 ALLOW FILTERING")
    List<UserRole> findByAssignedBy(UUID assignedBy);

    /**
     * Trouve les assignations dans une période donnée
     */
    @Query("SELECT * FROM user_roles WHERE assigned_at >= ?0 AND assigned_at <= ?1 ALLOW FILTERING")
    List<UserRole> findByAssignedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Compte les utilisateurs ayant un rôle spécifique
     */
    @Query("SELECT COUNT(*) FROM user_roles WHERE role_id = ?0 AND is_active = true ALLOW FILTERING")
    Long countActiveByRoleId(UUID roleId);

    /**
     * Compte les rôles actifs d'un utilisateur
     */
    @Query("SELECT COUNT(*) FROM user_roles WHERE user_id = ?0 AND is_active = true ALLOW FILTERING")
    Long countActiveByUserId(UUID userId);

    /**
     * Compte les utilisateurs actifs dans une organisation
     */
    @Query("SELECT COUNT(DISTINCT user_id) FROM user_roles WHERE organization_id = ?0 AND is_active = true ALLOW FILTERING")
    Long countDistinctActiveUsersByOrganizationId(UUID organizationId);

    /**
     * Trouve les assignations temporaires (avec date d'expiration)
     */
    @Query("SELECT * FROM user_roles WHERE expires_at IS NOT NULL AND is_active = true ALLOW FILTERING")
    List<UserRole> findTemporaryAssignments();

    /**
     * Trouve les assignations permanentes (sans date d'expiration)
     */
    @Query("SELECT * FROM user_roles WHERE expires_at IS NULL AND is_active = true ALLOW FILTERING")
    List<UserRole> findPermanentAssignments();

    /**
     * Trouve les rôles révoqués (inactifs)
     */
    @Query("SELECT * FROM user_roles WHERE is_active = false ALLOW FILTERING")
    List<UserRole> findRevokedAssignments();

    /**
     * Trouve les assignations par organisation et rôle
     */
    @Query("SELECT * FROM user_roles WHERE organization_id = ?0 AND role_id = ?1 AND is_active = true ALLOW FILTERING")
    List<UserRole> findActiveByOrganizationIdAndRoleId(UUID organizationId, UUID roleId);

    /**
     * Vérifie si un utilisateur a un rôle spécifique actif
     */
    @Query("SELECT COUNT(*) FROM user_roles WHERE user_id = ?0 AND role_id = ?1 AND is_active = true ALLOW FILTERING")
    Long countActiveByUserIdAndRoleId(UUID userId, UUID roleId);

    /**
     * Méthode par défaut pour vérifier si un utilisateur a un rôle actif
     */
    default boolean hasActiveRole(UUID userId, UUID roleId) {
        return countActiveByUserIdAndRoleId(userId, roleId) > 0;
    }

    /**
     * Trouve les assignations récentes
     */
    @Query("SELECT * FROM user_roles WHERE assigned_at >= ?0 ALLOW FILTERING")
    List<UserRole> findRecentAssignments(LocalDateTime since);

    /**
     * Trouve les assignations par raison d'assignation
     */
    @Query("SELECT * FROM user_roles WHERE assignment_reason = ?0 ALLOW FILTERING")
    List<UserRole> findByAssignmentReason(String reason);
}
