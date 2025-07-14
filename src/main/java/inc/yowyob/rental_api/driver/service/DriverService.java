package inc.yowyob.rental_api.driver.service;

import java.util.Collections;
import inc.yowyob.rental_api.driver.dto.CreateDriverDto;
import inc.yowyob.rental_api.driver.dto.DriverDto;
import inc.yowyob.rental_api.driver.dto.UpdateDriverDto;
import inc.yowyob.rental_api.driver.entities.Driver;
import inc.yowyob.rental_api.driver.mapper.DriverMapper;
import inc.yowyob.rental_api.driver.repository.DriverRepository;
// import inc.yowyob.rental_api.security.util.SecurityUtils;
import inc.yowyob.rental_api.user.entities.User;
import inc.yowyob.rental_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
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
                .agencyId(createDto.getAgencyId())
                .dateOfBirth(createDto.getDateOfBirth())
                .licenseNumber(createDto.getLicenseNumber())
                .licenseType(createDto.getLicenseType())
                .licenseExpiry(createDto.getLicenseExpiry())
                .experience(createDto.getExperience())
                .registrationId(createDto.getRegistrationId())
                .cni(createDto.getCni())
                .position(createDto.getPosition())
                .department(createDto.getDepartment())
                .staffStatus(createDto.getStaffStatus())
                .hourlyRate(createDto.getHourlyRate())
                .workingHours(createDto.getWorkingHours())
                .hireDate(createDto.getHireDate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        
        Driver savedDriver = driverRepository.save(driver);
        log.info("Driver created successfully with ID {}", savedDriver.getDriverId());

        return buildFullDriverDto(savedDriver, user);
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
        
        return buildFullDriverDto(driver, user);
    }
    /**
     * Récupère tous les chauffeurs d'une organisation de manière performante.
     * @param organizationId L'ID de l'organisation.
     * @return Une liste de DTOs de chauffeurs.
     */
     public Slice<DriverDto> getAllDriversByOrganization(UUID organizationId, Pageable pageable) {
        log.info("Fetching a paginated list of drivers for organization {}", organizationId);

        // 1. Récupérer la PAGE de Driver depuis le repository.
        //    C'est ici que l'appel est corrigé : on passe le Pageable.
        Slice<Driver> driverSlice = driverRepository.findByOrganizationId(organizationId, pageable);

        // 2. Extraire la liste des chauffeurs pour la page ACTUELLE.
        List<Driver> driversOnSlice = driverSlice.getContent();

        if (driversOnSlice.isEmpty()) {
            // On retourne une nouvelle implémentation de Slice avec une liste vide.
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        // 3. Collecter les IDs des utilisateurs (uniquement pour la page actuelle).
        List<UUID> userIds = driversOnSlice.stream()
                .map(Driver::getUserId)
                .distinct()
                .toList();

        // 4. Récupérer tous les utilisateurs nécessaires en UNE SEULE requête.
        Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 5. Utiliser la méthode `map` de l'objet Page pour transformer Page<Driver> en Page<DriverDto>.
        //    Cette méthode préserve toutes les informations de pagination.
        return driverSlice.map(driver -> {
            User user = userMap.get(driver.getUserId());
            if (user == null) {
                log.warn("Data integrity issue: User not found for driver {}, skipping.", driver.getDriverId());
                return null; // Ce cas devrait être rare si les données sont cohérentes.
            }
            return buildFullDriverDto(driver, user);
        });
    }

     /**
     * Récupère une page de chauffeurs pour une AGENCE spécifique.
     */
    public Slice<DriverDto> getAllDriversByAgency(UUID agencyId, Pageable pageable) {
        log.info("Fetching a paginated list of drivers for agency {}", agencyId);
        Slice<Driver> driverSlice = driverRepository.findByAgencyId(agencyId, pageable);
        // On réutilise la même logique de mapping
        return mapDriverSliceToDtoSlice(driverSlice, pageable);
    }

    private Slice<DriverDto> mapDriverSliceToDtoSlice(Slice<Driver> driverSlice, Pageable pageable) {
        List<Driver> driversOnSlice = driverSlice.getContent();

        if (driversOnSlice.isEmpty()) {
            // On retourne une nouvelle implémentation de Slice avec une liste vide.
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        List<UUID> userIds = driversOnSlice.stream().map(Driver::getUserId).distinct().toList();
        Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // On mappe la liste de DTOs
        List<DriverDto> dtoList = driversOnSlice.stream().map(driver -> {
            User user = userMap.get(driver.getUserId());
            if (user == null) return null;
            return buildFullDriverDto(driver, user);
        }).collect(Collectors.toList());

        // On reconstruit un Slice<DriverDto> avec la liste mappée et les infos du Slice original
        return new SliceImpl<>(dtoList, pageable, driverSlice.hasNext());
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

        // 1. Récupérer l'entité Driver existante
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new NoSuchElementException("Driver not found with ID: " + driverId));

        // 2. Appliquer les mises à jour partielles
        if (updateDto.getDateOfBirth() != null) {
            driver.setDateOfBirth(updateDto.getDateOfBirth());
        }
        if (updateDto.getLicenseNumber() != null) {
            driver.setLicenseNumber(updateDto.getLicenseNumber());
        }
        if (updateDto.getLicenseType() != null) {
            driver.setLicenseType(updateDto.getLicenseType());
        }
        if (updateDto.getLicenseExpiry() != null) {
            driver.setLicenseExpiry(updateDto.getLicenseExpiry());
        }
        if (updateDto.getExperience() != null) {
            driver.setExperience(updateDto.getExperience());
        }
        if (updateDto.getPosition() != null) {
            driver.setPosition(updateDto.getPosition());
        }
        if (updateDto.getDepartment() != null) {
            driver.setDepartment(updateDto.getDepartment());
        }
        if (updateDto.getStaffStatus() != null) {
            driver.setStaffStatus(updateDto.getStaffStatus());
        }
        if (updateDto.getHourlyRate() != null) {
            driver.setHourlyRate(updateDto.getHourlyRate());
        }
        if (updateDto.getWorkingHours() != null) {
            driver.setWorkingHours(updateDto.getWorkingHours());
        }
        if (updateDto.getStatus() != null) {
            driver.setStatus(updateDto.getStatus());
        }
        if (updateDto.getRating() != null) {
            driver.setRating(updateDto.getRating());
        }

        // 3. Mettre à jour la date de dernière modification
        driver.setUpdatedAt(LocalDateTime.now());
        // Vous pourriez aussi ajouter un champ `updatedBy` à l'entité Driver si nécessaire

        // 4. Sauvegarder l'entité mise à jour
        Driver updatedDriver = driverRepository.save(driver);
        log.info("Driver {} updated successfully.", driverId);

        // 5. Récupérer l'utilisateur associé pour construire le DTO complet
        User user = userRepository.findById(driver.getUserId())
                .orElseThrow(() -> new IllegalStateException("Associated user not found for an existing driver. Data integrity issue for driver ID: " + driverId));
        
        return buildFullDriverDto(updatedDriver, user);
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

    public Slice<DriverDto> getAllDrivers(Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllDrivers'");
    }

    // Méthode privée pour centraliser la construction du DTO complet
    private DriverDto buildFullDriverDto(Driver driver, User user) {
        DriverDto dto = driverMapper.toDto(driver, user);
        
        // Calculer et ajouter l'âge
        if (driver.getDateOfBirth() != null) {
            dto.setAge(Period.between(driver.getDateOfBirth(), LocalDate.now()).getYears());
        }
        
        return dto;
    }
}