package inc.yowyob.rental_api.vehicle.repository;

import inc.yowyob.rental_api.vehicle.entities.Vehicle;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends CassandraRepository<Vehicle, Vehicle.VehicleKey> {

    // Efficient query due to Primary Key design
    List<Vehicle> findByKeyOrganizationIdAndKeyAgencyId(UUID organizationId, UUID agencyId);

    List<Vehicle> findByKeyOrganizationId(UUID organizationId);

    // This still requires a secondary index or a different table structure for high performance.
    // For now, we accept ALLOW FILTERING as it's a specific search by a unique property within an organization.
    @Query("SELECT * FROM vehicles WHERE key.organization_id = ?0 AND registration_number = ?1 ALLOW FILTERING")
    Optional<Vehicle> findByOrganizationIdAndRegistrationNumber(UUID organizationId, String registrationNumber);

    Long countByKeyOrganizationId(UUID organizationId);

    // This query is inefficient. We will address this in the service layer.
    @Query("SELECT * FROM vehicles WHERE tariff_id = ?0 ALLOW FILTERING")
    List<Vehicle> findByTariffId(UUID tariffId);

}