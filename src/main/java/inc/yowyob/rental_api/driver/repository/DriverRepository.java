package inc.yowyob.rental_api.driver.repository;

import inc.yowyob.rental_api.driver.enums.DriverStatus;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import inc.yowyob.rental_api.driver.entities.Driver;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends CassandraRepository<Driver, UUID> {

    /**
     * Trouve un chauffeur par l'ID de l'utilisateur associé.
     * S'appuie sur un index secondaire sur la colonne 'user_id'.
     */
    // @Query("SELECT * FROM drivers WHERE user_id = ?0 ALLOW FILTERING")
    Optional<Driver> findByUserId(UUID userId);

    /**
     * Récupère les chauffeurs par statut
     */
    // @Query("SELECT * FROM drivers WHERE status = ?0 ALLOW FILTERING")
    List<Driver> findByStatus(DriverStatus status);

   /**
     * Récupère les chauffeurs par localisation
     */
    @Query("SELECT * FROM drivers WHERE location = ?0 ALLOW FILTERING")
    List<Driver> findByLocation(String location);

    /**
     * Récupère les chauffeurs ayant une note minimale
     */
    @Query("SELECT * FROM drivers WHERE rating >= ?0 ALLOW FILTERING")
    List<Driver> findByMinimumRating(double rating);

    /**
     * Récupère les chauffeurs assignés à un véhicule spécifique
     */
    @Query("SELECT * FROM drivers WHERE vehicle_assigned CONTAINS ?0 ALLOW FILTERING")
    List<Driver> findByVehicleAssigned(UUID vehicleId);

    Slice<Driver> findByOrganizationId(UUID organizationId, Pageable pageable);
    // List<Driver> findByOrganizationId(UUID organizationId, Pageable pageable);
    
    /**
     * Récupère les chauffeurs d'une organisation par leur localisation.
     * S'appuie sur un index secondaire sur la colonne 'location'.
     */
    List<Driver> findByOrganizationIdAndLocation(UUID organizationId, String location);

    List<Driver> findByOrganizationIdAndRatingGreaterThanEqual(UUID organizationId, Double rating);

    /**
     * Récupère les chauffeurs d'une organisation assignés à un véhicule spécifique.
     * S'appuie sur un index secondaire sur la collection 'assigned_vehicle_ids'.
     */
    Slice<Driver> findByOrganizationIdAndAssignedVehicleIdsContains(UUID organizationId, UUID vehicleId);

    /**
     * Trouve tous les chauffeurs appartenant à une agence spécifique,
     * avec support de la pagination et du tri.
     * S'appuie sur l'index 'idx_driver_agency_id'.
     *
     * @param agencyId L'identifiant unique de l'agence.
     * @param pageable L'objet de pagination.
     * @return Une Page d'entités Driver.
     */
    Slice<Driver> findByAgencyId(UUID agencyId, Pageable pageable);
}
