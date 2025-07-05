package inc.yowyob.rental_api.organization.repository;

import inc.yowyob.rental_api.core.enums.OrganizationType;
import inc.yowyob.rental_api.organization.entities.Organization;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends CassandraRepository<Organization, UUID> {

    /**
     * Trouve une organisation par propriétaire
     */
    @Query("SELECT * FROM organizations WHERE owner_id = ?0 ALLOW FILTERING")
    Optional<Organization> findByOwnerId(UUID ownerId);

    /**
     * Trouve toutes les organisations d'un propriétaire
     */
    @Query("SELECT * FROM organizations WHERE owner_id = ?0 ALLOW FILTERING")
    List<Organization> findAllByOwnerId(UUID ownerId);

    /**
     * Trouve une organisation par nom
     */
    @Query("SELECT * FROM organizations WHERE name = ?0 ALLOW FILTERING")
    Optional<Organization> findByName(String name);

    /**
     * Trouve les organisations par type
     */
    @Query("SELECT * FROM organizations WHERE organization_type = ?0 ALLOW FILTERING")
    List<Organization> findByOrganizationType(String organizationType);

    /**
     * Trouve les organisations actives
     */
    @Query("SELECT * FROM organizations WHERE is_active = true ALLOW FILTERING")
    List<Organization> findActiveOrganizations();

    /**
     * Trouve les organisations vérifiées
     */
    @Query("SELECT * FROM organizations WHERE is_verified = true ALLOW FILTERING")
    List<Organization> findVerifiedOrganizations();

    /**
     * Trouve les organisations par ville
     */
    @Query("SELECT * FROM organizations WHERE city = ?0 ALLOW FILTERING")
    List<Organization> findByCity(String city);

    /**
     * Trouve les organisations par pays
     */
    @Query("SELECT * FROM organizations WHERE country = ?0 ALLOW FILTERING")
    List<Organization> findByCountry(String country);

    /**
     * Trouve les organisations créées dans une période
     */
    @Query("SELECT * FROM organizations WHERE created_at >= ?0 AND created_at <= ?1 ALLOW FILTERING")
    List<Organization> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouve les organisations par numéro d'enregistrement
     */
    @Query("SELECT * FROM organizations WHERE registration_number = ?0 ALLOW FILTERING")
    Optional<Organization> findByRegistrationNumber(String registrationNumber);

    /**
     * Trouve les organisations par numéro de taxe
     */
    @Query("SELECT * FROM organizations WHERE tax_number = ?0 ALLOW FILTERING")
    Optional<Organization> findByTaxNumber(String taxNumber);

    /**
     * Compte les organisations actives
     */
    @Query("SELECT COUNT(*) FROM organizations WHERE is_active = true ALLOW FILTERING")
    Long countActiveOrganizations();

    /**
     * Compte les organisations par type
     */
    @Query("SELECT COUNT(*) FROM organizations WHERE organization_type = ?0 ALLOW FILTERING")
    Long countByOrganizationType(String organizationType);

    /**
     * Trouve les organisations avec des limites spécifiques
     */
    @Query("SELECT * FROM organizations WHERE max_agencies >= ?0 ALLOW FILTERING")
    List<Organization> findByMaxAgenciesGreaterThanEqual(Integer minAgencies);

    /**
     * Trouve les organisations proches de leurs limites d'agences
     */
    @Query("SELECT * FROM organizations WHERE current_agencies >= max_agencies * 0.8 ALLOW FILTERING")
    List<Organization> findNearAgencyLimit();

    /**
     * Trouve les organisations par email
     */
    @Query("SELECT * FROM organizations WHERE email = ?0 ALLOW FILTERING")
    Optional<Organization> findByEmail(String email);

    /**
     * Trouve les organisations modifiées récemment
     */
    @Query("SELECT * FROM organizations WHERE updated_at >= ?0 ALLOW FILTERING")
    List<Organization> findRecentlyUpdated(LocalDateTime since);

    /**
     * Vérifie si un nom d'organisation existe déjà
     */
    @Query("SELECT COUNT(*) FROM organizations WHERE name = ?0 ALLOW FILTERING")
    Long countByName(String name);

    /**
     * Méthode par défaut pour vérifier l'existence d'un nom
     */
    default boolean existsByName(String name) {
        return countByName(name) > 0;
    }

    /**
     * Vérifie si un numéro d'enregistrement existe déjà
     */
    @Query("SELECT COUNT(*) FROM organizations WHERE registration_number = ?0 ALLOW FILTERING")
    Long countByRegistrationNumber(String registrationNumber);

    /**
     * Méthode par défaut pour vérifier l'existence d'un numéro d'enregistrement
     */
    default boolean existsByRegistrationNumber(String registrationNumber) {
        return countByRegistrationNumber(registrationNumber) > 0;
    }

    /**
     * Trouve les organisations inactives depuis une certaine date
     */
    @Query("SELECT * FROM organizations WHERE is_active = false AND updated_at < ?0 ALLOW FILTERING")
    List<Organization> findInactiveSince(LocalDateTime cutoffDate);

    /**
     * Trouve les organisations par région
     */
    @Query("SELECT * FROM organizations WHERE region = ?0 ALLOW FILTERING")
    List<Organization> findByRegion(String region);

    /**
     * Trouve les organisations avec site web
     */
    @Query("SELECT * FROM organizations WHERE website IS NOT NULL ALLOW FILTERING")
    List<Organization> findWithWebsite();

    /**
     * Trouve les organisations créées par un utilisateur spécifique
     */
    @Query("SELECT * FROM organizations WHERE created_by = ?0 ALLOW FILTERING")
    List<Organization> findByCreatedBy(UUID createdBy);
}
