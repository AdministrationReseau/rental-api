// PATH: src/test/java/inc/yowyob/rental_api/driver/service/DriverServiceTest.java
package inc.yowyob.rental_api.driver.service;

import inc.yowyob.rental_api.core.enums.UserType;
import inc.yowyob.rental_api.driver.dto.CreateDriverDto;
import inc.yowyob.rental_api.driver.dto.DriverDto;
import inc.yowyob.rental_api.driver.entities.Driver;
import inc.yowyob.rental_api.driver.mapper.DriverMapper;
import inc.yowyob.rental_api.driver.repository.DriverRepository;
import inc.yowyob.rental_api.user.entities.User;
import inc.yowyob.rental_api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock private DriverRepository driverRepository;
    @Mock private UserRepository userRepository;
    @Mock private DriverMapper driverMapper;
    @InjectMocks private DriverService driverService;

    private User testUser;
    private CreateDriverDto createDriverDto;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        // CORRECTION: Utilisation du constructeur correct de l'entité User
        testUser = new User("driver@test.com", "password", "John", "Doe", UserType.STAFF);
        testUser.setId(userId);

        createDriverDto = CreateDriverDto.builder()
                .userId(userId)
                .organizationId(UUID.randomUUID())
                .age(30)
                .licenseNumber("B123456")
                .licenseType("B")
                .build();
    }

    @Test
    void createDriver_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            driverService.createDriver(createDriverDto, UUID.randomUUID());
        });

        verify(driverRepository, never()).save(any(Driver.class));
    }
}