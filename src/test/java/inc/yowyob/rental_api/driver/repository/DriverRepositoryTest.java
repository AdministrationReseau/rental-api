// PATH: src/test/java/inc/yowyob/rental_api/driver/repository/DriverRepositoryTest.java

package inc.yowyob.rental_api.driver.repository;

import inc.yowyob.rental_api.driver.entities.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName; // Importez ceci

import java.time.Duration; // Importez ceci
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class DriverRepositoryTest {

    private static final int CASSANDRA_PORT = 9042;
    
    // ======================= MODIFICATION CLÉ =======================
    // 1. Utiliser une image spécifique et réputée (évite les bugs de 'latest')
    // 2. Augmenter le timeout de démarrage à 2 minutes.
    @Container
    static final CassandraContainer<?> cassandra = 
        new CassandraContainer<>(DockerImageName.parse("cassandra:4.1.3"))
            .withExposedPorts(CASSANDRA_PORT)
            .withStartupTimeout(Duration.ofMinutes(2));
    // ================================================================

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cassandra.contact-points", cassandra::getContactPoint);
        registry.add("spring.cassandra.port", () -> cassandra.getMappedPort(CASSANDRA_PORT));
        registry.add("spring.cassandra.local-datacenter", cassandra::getLocalDatacenter);
        registry.add("spring.cassandra.keyspace-name", () -> "rental");
        registry.add("spring.cassandra.schema-action", () -> "CREATE_IF_NOT_EXISTS");
        registry.add("spring.cassandra.username", () -> null);
        registry.add("spring.cassandra.password", () -> null);
    }

    @Autowired
    private DriverRepository driverRepository;

    @BeforeEach
    void setUp() {
        driverRepository.deleteAll();
    }

    // ... vos tests restent les mêmes ...
    @Test
    void shouldSaveAndFindDriverById() {
        // GIVEN
        UUID driverId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();

        Driver driver = Driver.builder()
                .driverId(driverId)
                .userId(userId)
                .organizationId(organizationId)
                .age(30)
                .licenseNumber("XYZ123")
                .licenseType("B")
                .available(true)
                .createdAt(LocalDateTime.now())
                .build();

        // WHEN
        driverRepository.save(driver);

        // THEN
        Optional<Driver> foundDriverOpt = driverRepository.findById(driverId);
        assertTrue(foundDriverOpt.isPresent(), "Le chauffeur devrait être trouvé dans la base de données");

        Driver foundDriver = foundDriverOpt.get();
        assertEquals(driverId, foundDriver.getDriverId());
        assertEquals(userId, foundDriver.getUserId());
        assertEquals("XYZ123", foundDriver.getLicenseNumber());
    }

    @Test
    void shouldReturnEmptyWhenDriverNotFound() {
        // GIVEN
        UUID nonExistentId = UUID.randomUUID();

        // WHEN
        Optional<Driver> foundDriverOpt = driverRepository.findById(nonExistentId);

        // THEN
        assertFalse(foundDriverOpt.isPresent(), "Aucun chauffeur ne devrait être trouvé avec un ID inexistant");
    }
}