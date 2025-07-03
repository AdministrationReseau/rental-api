package inc.yowyob.rental_api.driver.repository;

import inc.yowyob.rental_api.driver.enums.DriverStatus;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import inc.yowyob.rental_api.driver.entities.Driver;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends CassandraRepository<Driver, UUID> {

    /**
     * Récupère tous les chauffeurs associés à un utilisateur donné
     */
    @Query("SELECT * FROM drivers WHERE user_id = ?0 ALLOW FILTERING")
    Optional<Driver> findByUserId(UUID userId);

    /**
     * Récupère les chauffeurs par statut
     */
    @Query("SELECT * FROM drivers WHERE status = ?0 ALLOW FILTERING")
    List<Driver> findByStatus(DriverStatus status);

    /**
     * Récupère les chauffeurs disponibles
     */
    @Query("SELECT * FROM drivers WHERE available = true ALLOW FILTERING")
    List<Driver> findAvailableDrivers();

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

    List<Driver> findByOrganizationId(UUID organizationId);
}
