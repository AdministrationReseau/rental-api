package inc.yowyob.rental_api.vehicle.repository;

import inc.yowyob.rental_api.vehicle.entities.Tariff;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TariffRepository extends CassandraRepository<Tariff, Tariff.TariffKey> {

    List<Tariff> findByKeyOrganizationId(UUID organizationId);

    // This query is now efficient because of the composite primary key
    @Query("SELECT * FROM tariffs WHERE organization_id = ?0 and name = ?1")
    Optional<Tariff> findByOrganizationIdAndName(UUID organizationId, String name);

    // Note: Cassandra does not support COUNT on non-primary key columns without secondary indexes.
    // A separate query or application-side logic is needed for counting tariff usage.
    // We will handle this in the service layer.
}