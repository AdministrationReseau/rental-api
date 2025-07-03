package inc.yowyob.rental_api.driver.service;

import inc.yowyob.rental_api.driver.dto.CreateDriverDto;
import inc.yowyob.rental_api.driver.dto.DriverDto;
import inc.yowyob.rental_api.driver.dto.UpdateDriverDto;
import inc.yowyob.rental_api.driver.entities.Driver;
import inc.yowyob.rental_api.driver.mapper.DriverMapper;
import inc.yowyob.rental_api.driver.repository.DriverRepository;
import inc.yowyob.rental_api.security.util.SecurityUtils;
import inc.yowyob.rental_api.user.entities.User;
import inc.yowyob.rental_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final DriverMapper driverMapper;

    /**
     * Crée un nouveau profil de chauffeur pour un utilisateur existant.
     * @param createDto DTO contenant les informations de création.
     * @param createdBy ID de l'utilisateur effectuant la création.
     * @return Le DTO du chauffeur nouvellement créé.
     */
    @Transactional
    public DriverDto createDriver(CreateDriverDto createDto, UUID createdBy) {
        log.info("Creating driver for user ID {} by user {}", createDto.getUserId(), createdBy);
        
        // 1. Vérifier que l'utilisateur existe
        User user = userRepository.findById(createDto.getUserId())
            .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + createDto.getUserId()));

        // 2. Vérifier si cet utilisateur est déjà un chauffeur
        if (driverRepository.findByUserId(createDto.getUserId()).isPresent()) {
            throw new IllegalStateException("This user is already registered as a driver.");
        }

        // 3. Créer l'entité Driver
        Driver driver = Driver.builder()
                .driverId(UUID.randomUUID())
                .userId(createDto.getUserId())
                .organizationId(createDto.getOrganizationId())
                .age(createDto.getAge())
                .licenseNumber(createDto.getLicenseNumber())
                .licenseType(createDto.getLicenseType())
                .idCardUrl(createDto.getIdCardUrl())
                .driverLicenseUrl(createDto.getDriverLicenseUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .statusUpdatedBy(createdBy) // L'initiateur du statut
                .build();
        
        Driver savedDriver = driverRepository.save(driver);
        log.info("Driver created successfully with ID {}", savedDriver.getDriverId());

        return driverMapper.toDto(savedDriver, user);
    }

    /**
     * Récupère un chauffeur par son ID.
     * @param driverId L'ID du chauffeur.
     * @return Le DTO complet du chauffeur.
     */
    public DriverDto getDriverById(UUID driverId) {
        log.info("Fetching driver with ID {}", driverId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new NoSuchElementException("Driver not found with ID: " + driverId));
        
        User user = userRepository.findById(driver.getUserId())
                .orElseThrow(() -> new NoSuchElementException("Associated user not found for driver ID: " + driverId));
        
        return driverMapper.toDto(driver, user);
    }

    /**
     * Récupère tous les chauffeurs d'une organisation de manière performante.
     * @param organizationId L'ID de l'organisation.
     * @return Une liste de DTOs de chauffeurs.
     */
    public List<DriverDto> getAllDriversByOrganization(UUID organizationId) {
        log.info("Fetching all drivers for organization {}", organizationId);

        List<Driver> drivers = driverRepository.findByOrganizationId(organizationId);
        if (drivers.isEmpty()) {
            return List.of();
        }

        List<UUID> userIds = drivers.stream().map(Driver::getUserId).distinct().toList();

        Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return drivers.stream()
                .map(driver -> {
                    User user = userMap.get(driver.getUserId());
                    if (user == null) {
                        log.warn("User not found for driver {}, skipping.", driver.getDriverId());
                        return null;
                    }
                    return driverMapper.toDto(driver, user);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Met à jour les informations d'un chauffeur existant.
     * @param driverId L'ID du chauffeur à mettre à jour.
     * @param updateDto DTO contenant les champs à modifier.
     * @param updatedBy ID de l'utilisateur qui effectue la mise à jour.
     * @return Le DTO du chauffeur mis à jour.
     */
    @Transactional
    public DriverDto updateDriver(UUID driverId, UpdateDriverDto updateDto, UUID updatedBy) {
        log.info("Updating driver {} by user {}", driverId, updatedBy);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new NoSuchElementException("Driver not found with ID: " + driverId));

        // Appliquer les mises à jour partielles
        if (updateDto.getAge() != null) driver.setAge(updateDto.getAge());
        if (updateDto.getLicenseNumber() != null) driver.setLicenseNumber(updateDto.getLicenseNumber());
        if (updateDto.getLicenseType() != null) driver.setLicenseType(updateDto.getLicenseType());
        if (updateDto.getLocation() != null) driver.setLocation(updateDto.getLocation());
        if (updateDto.getIdCardUrl() != null) driver.setIdCardUrl(updateDto.getIdCardUrl());
        if (updateDto.getDriverLicenseUrl() != null) driver.setDriverLicenseUrl(updateDto.getDriverLicenseUrl());
        if (updateDto.getAssignedVehicleIds() != null) driver.setAssignedVehicleIds(updateDto.getAssignedVehicleIds());
        if (updateDto.getAvailable() != null) driver.setAvailable(updateDto.getAvailable());
        if (updateDto.getRating() != null) driver.setRating(updateDto.getRating());
        if (updateDto.getInsuranceProvider() != null) driver.setInsuranceProvider(updateDto.getInsuranceProvider());
        if (updateDto.getInsurancePolicy() != null) driver.setInsurancePolicy(updateDto.getInsurancePolicy());
        
        if (updateDto.getStatus() != null && updateDto.getStatus() != driver.getStatus()) {
            driver.setStatus(updateDto.getStatus());
            driver.setStatusUpdatedAt(LocalDateTime.now());
            driver.setStatusUpdatedBy(updatedBy);
        }

        driver.setUpdatedAt(LocalDateTime.now());

        Driver updatedDriver = driverRepository.save(driver);
        log.info("Driver {} updated successfully.", driverId);

        User user = userRepository.findById(driver.getUserId())
            .orElseThrow(() -> new NoSuchElementException("Associated user not found for driver ID: " + driverId));

        return driverMapper.toDto(updatedDriver, user);
    }
    
    /**
     * Supprime un chauffeur.
     * @param driverId L'ID du chauffeur à supprimer.
     */
    @Transactional
    public void deleteDriver(UUID driverId) {
        log.info("Deleting driver with ID {}", driverId);
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new NoSuchElementException("Driver not found with ID: " + driverId));
        
        driverRepository.delete(driver);
        log.info("Driver with ID {} deleted successfully", driverId);
    }

    public List<DriverDto> getAllDrivers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllDrivers'");
    }
}