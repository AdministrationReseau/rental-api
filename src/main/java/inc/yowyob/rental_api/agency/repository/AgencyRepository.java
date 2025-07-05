package inc.yowyob.rental_api.agency.repository;

import inc.yowyob.rental_api.agency.entities.Agency;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencyRepository extends CassandraRepository<Agency, UUID> {

    /**
     * Trouve toutes les agences d'une organisation
     */
    @Query("SELECT * FROM agencies WHERE organization_id = ?0 ALLOW FILTERING")
    List<Agency> findByOrganizationId(UUID organizationId);

    /**
     * Trouve les agences actives d'une organisation
     */
    @Query("SELECT * FROM agencies WHERE organization_id = ?0 AND is_active = true ALLOW FILTERING")
    List<Agency> findActiveByOrganizationId(UUID organizationId);

    /**
     * Trouve une agence par nom dans une organisation
     */
    @Query("SELECT * FROM agencies WHERE organization_id = ?0 AND name = ?1 ALLOW FILTERING")
    Optional<Agency> findByOrganizationIdAndName(UUID organizationId, String name);

    /**
     * Trouve les agences par gestionnaire
     */
    @Query("SELECT * FROM agencies WHERE manager_id = ?0 ALLOW FILTERING")
    List<Agency> findByManagerId(UUID managerId);

    /**
     * Trouve les agences par ville
     */
    @Query("SELECT * FROM agencies WHERE city = ?0 ALLOW FILTERING")
    List<Agency> findByCity(String city);

    /**
     * Trouve les agences par pays
     */
    @Query("SELECT * FROM agencies WHERE country = ?0 ALLOW FILTERING")
    List<Agency> findByCountry(String country);

    /**
     * Trouve les agences par région
     */
    @Query("SELECT * FROM agencies WHERE region = ?0 ALLOW FILTERING")
    List<Agency> findByRegion(String region);

    /**
     * Trouve les agences ouvertes 24h/24
     */
    @Query("SELECT * FROM agencies WHERE is_24hours = true ALLOW FILTERING")
    List<Agency> find24HourAgencies();

    /**
     * Trouve les agences avec géofencing activé
     */
    @Query("SELECT * FROM agencies WHERE geofence_zone_id IS NOT NULL ALLOW FILTERING")
    List<Agency> findWithGeofencing();

    /**
     * Trouve les agences par email
     */
    @Query("SELECT * FROM agencies WHERE email = ?0 ALLOW FILTERING")
    Optional<Agency> findByEmail(String email);

    /**
     * Trouve les agences par téléphone
     */
    @Query("SELECT * FROM agencies WHERE phone = ?0 ALLOW FILTERING")
    Optional<Agency> findByPhone(String phone);

    /**
     * Compte les agences actives par organisation
     */
    @Query("SELECT COUNT(*) FROM agencies WHERE organization_id = ?0 AND is_active = true ALLOW FILTERING")
    Long countActiveByOrganizationId(UUID organizationId);

    /**
     * Comte toutes les agences par organisation
     */
    @Query("SELECT COUNT(*) FROM agencies WHERE organization_id = ?0 ALLOW FILTERING")
    Long countByOrganizationId(UUID organizationId);

    /**
     * Trouve les agences créées dans une période
     */
    @Query("SELECT * FROM agencies WHERE created_at >= ?0 AND created_at <= ?1 ALLOW FILTERING")
    List<Agency> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouve les agences créées par un utilisateur
     */
    @Query("SELECT * FROM agencies WHERE created_by = ?0 ALLOW FILTERING")
    List<Agency> findByCreatedBy(UUID createdBy);

    /**
     * Trouve les agences modifiées récemment
     */
    @Query("SELECT * FROM agencies WHERE updated_at >= ?0 ALLOW FILTERING")
    List<Agency> findRecentlyUpdated(LocalDateTime since);

    /**
     * Trouve les agences sans gestionnaire
     */
    @Query("SELECT * FROM agencies WHERE manager_id IS NULL ALLOW FILTERING")
    List<Agency> findWithoutManager();

    /**
     * Trouve les agences par zone géographique (approximative par ville et région)
     */
    @Query("SELECT * FROM agencies WHERE city = ?0 AND region = ?1 ALLOW FILTERING")
    List<Agency> findByCityAndRegion(String city, String region);

    /**
     * Vérifie si un nom d'agence existe dans une organisation
     */
    @Query("SELECT COUNT(*) FROM agencies WHERE organization_id = ?0 AND name = ?1 ALLOW FILTERING")
    Long countByOrganizationIdAndName(UUID organizationId, String name);

    /**
     * Méthode par défaut pour vérifier l'existence d'un nom d'agence
     */
    default boolean existsByOrganizationIdAndName(UUID organizationId, String name) {
        return countByOrganizationIdAndName(organizationId, name) > 0;
    }

    /**
     * Trouve les agences avec un nombre minimum de véhicules
     */
    @Query("SELECT * FROM agencies WHERE current_vehicles >= ?0 ALLOW FILTERING")
    List<Agency> findWithMinimumVehicles(Integer minVehicles);

    /**
     * Trouve les agences avec un nombre minimum de chauffeurs
     */
    @Query("SELECT * FROM agencies WHERE current_drivers >= ?0 ALLOW FILTERING")
    List<Agency> findWithMinimumDrivers(Integer minDrivers);

    /**
     * Trouve les agences avec un nombre minimum de personnel
     */
    @Query("SELECT * FROM agencies WHERE current_staff >= ?0 ALLOW FILTERING")
    List<Agency> findWithMinimumStaff(Integer minStaff);

    /**
     * Trouve les agences inactives
     */
    @Query("SELECT * FROM agencies WHERE is_active = false ALLOW FILTERING")
    List<Agency> findInactiveAgencies();

    /**
     * Trouve les agences inactives d'une organisation
     */
    @Query("SELECT * FROM agencies WHERE organization_id = ?0 AND is_active = false ALLOW FILTERING")
    List<Agency> findInactiveByOrganizationId(UUID organizationId);

    /**
     * Trouve les agences par code postal
     */
    @Query("SELECT * FROM agencies WHERE postal_code = ?0 ALLOW FILTERING")
    List<Agency> findByPostalCode(String postalCode);

    /**
     * Trouve les agences dans une organisation par ville
     */
    @Query("SELECT * FROM agencies WHERE organization_id = ?0 AND city = ?1 ALLOW FILTERING")
    List<Agency> findByOrganizationIdAndCity(UUID organizationId, String city);

    /**
     * Trouve les agences d'une organisation dans un pays
     */
    @Query("SELECT * FROM agencies WHERE organization_id = ?0 AND country = ?1 ALLOW FILTERING")
    List<Agency> findByOrganizationIdAndCountry(UUID organizationId, String country);
}
