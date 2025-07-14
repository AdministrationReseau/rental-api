package inc.yowyob.rental_api.vehicle.repository;

import inc.yowyob.rental_api.vehicle.entities.Image;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImageRepository extends CassandraRepository<Image, UUID> {

    @Query("SELECT * FROM images WHERE resourceId = ?0")
    List<Image> findByResourceId(UUID resourceId);
}