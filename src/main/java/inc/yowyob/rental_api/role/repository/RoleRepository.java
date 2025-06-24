package inc.yowyob.rental_api.role.repository;

import inc.yowyob.rental_api.core.enums.RoleType;
import inc.yowyob.rental_api.role.entities.Role;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends CassandraRepository<Role, UUID> {

    /**
     * Trouve tous les rôles d'une organisation
     */
    @Query("SELECT * FROM roles WHERE organization_id = ?0 ALLOW FILTERING")
    List<Role> findByOrganizationId(UUID organizationId);

    /**
     * Trouve les rôles actifs d'une organisation
     */
    @Query("SELECT * FROM roles WHERE organization_id = ?0 AND is_active = true ALLOW FILTERING")
    List<Role> findActiveByOrganizationId(UUID organizationId);

    /**
     * Trouve un rôle par nom dans une organisation
     */
    @Query("SELECT * FROM roles WHERE organization_id = ?0 AND name = ?1 ALLOW FILTERING")
    Optional<Role> findByOrganizationIdAndName(UUID organizationId, String name);

    /**
     * Trouve les rôles par type
     */
    @Query("SELECT * FROM roles WHERE role_type = ?0 ALLOW FILTERING")
    List<Role> findByRoleType(String roleType);

    /**
     * Trouve les rôles système
     */
    @Query("SELECT * FROM roles WHERE issystemrole = true ALLOW FILTERING")
    List<Role> findSystemRoles();

    /**
     * Trouve les rôles par défaut d'une organisation
     */
    @Query("SELECT * FROM roles WHERE organization_id = ?0 AND is_default_role = true ALLOW FILTERING")
    List<Role> findDefaultRolesByOrganizationId(UUID organizationId);

    /**
     * Trouve les rôles personnalisés (non-système) d'une organisation
     */
    @Query("SELECT * FROM roles WHERE organization_id = ?0 AND is_system_role = false ALLOW FILTERING")
    List<Role> findCustomRolesByOrganizationId(UUID organizationId);

    /**
     * Trouve les rôles créés par un utilisateur spécifique
     */
    @Query("SELECT * FROM roles WHERE created_by = ?0 ALLOW FILTERING")
    List<Role> findByCreatedBy(UUID createdBy);

    /**
     * Trouve les rôles créés dans une période donnée
     */
    @Query("SELECT * FROM roles WHERE created_at >= ?0 AND created_at <= ?1 ALLOW FILTERING")
    List<Role> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouve les rôles avec une priorité supérieure ou égale
     */
    @Query("SELECT * FROM roles WHERE organization_id = ?0 AND priority >= ?1 ALLOW FILTERING")
    List<Role> findByOrganizationIdAndPriorityGreaterThanEqual(UUID organizationId, Integer priority);

    /**
     * Trouve les rôles contenant une permission spécifique
     */
    @Query("SELECT * FROM roles WHERE permissions CONTAINS ?0 ALLOW FILTERING")
    List<Role> findByPermission(String permission);

    /**
     * Trouve les rôles d'une organisation contenant une permission spécifique
     */
    @Query("SELECT * FROM roles WHERE organization_id = ?0 AND permissions CONTAINS ?1 ALLOW FILTERING")
    List<Role> findByOrganizationIdAndPermission(UUID organizationId, String permission);

    /**
     * Compte les rôles actifs d'une organisation
     */
    @Query("SELECT COUNT(*) FROM roles WHERE organization_id = ?0 AND is_active = true ALLOW FILTERING")
    Long countActiveByOrganizationId(UUID organizationId);

    /**
     * Comte les rôles par type
     */
    @Query("SELECT COUNT(*) FROM roles WHERE role_type = ?0 ALLOW FILTERING")
    Long countByRoleType(String roleType);

    /**
     * Trouve les rôles avec des couleurs spécifiques
     */
    @Query("SELECT * FROM roles WHERE organization_id = ?0 AND color = ?1 ALLOW FILTERING")
    List<Role> findByOrganizationIdAndColor(UUID organizationId, String color);

    /**
     * Vérifie si un nom de rôle existe déjà dans une organisation
     */
    @Query("SELECT COUNT(*) FROM roles WHERE organization_id = ?0 AND name = ?1 ALLOW FILTERING")
    Long countByOrganizationIdAndName(UUID organizationId, String name);

    /**
     * Méthode par défaut pour vérifier l'existence d'un nom de rôle
     */
    default boolean existsByOrganizationIdAndName(UUID organizationId, String name) {
        return countByOrganizationIdAndName(organizationId, name) > 0;
    }

    /**
     * Trouve les rôles modifiés récemment
     */
    @Query("SELECT * FROM roles WHERE organization_id = ?0 AND updated_at >= ?1 ALLOW FILTERING")
    List<Role> findRecentlyUpdatedByOrganizationId(UUID organizationId, LocalDateTime since);

    /**
     * Trouve les rôles inactifs d'une organisation
     */
    @Query("SELECT * FROM roles WHERE organization_id = ?0 AND is_active = false ALLOW FILTERING")
    List<Role> findInactiveByOrganizationId(UUID organizationId);
}
