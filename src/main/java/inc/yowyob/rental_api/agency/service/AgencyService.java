package inc.yowyob.rental_api.agency.service;

import inc.yowyob.rental_api.agency.dto.*;
import inc.yowyob.rental_api.agency.entities.Agency;
import inc.yowyob.rental_api.agency.repository.AgencyRepository;
import inc.yowyob.rental_api.organization.service.OrganizationService;
import inc.yowyob.rental_api.security.util.SecurityUtils;
import inc.yowyob.rental_api.user.entities.User;
import inc.yowyob.rental_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final OrganizationService organizationService;
    private final UserRepository userRepository;

    /**
     * Crée une nouvelle agence
     */
    @Transactional
    public AgencyDto createAgency(CreateAgencyDto createDto) {
        log.info("Creating agency: {} for organization: {}", createDto.getName(), createDto.getOrganizationId());

        // Vérifier les permissions d'accès à l'organisation
        if (!SecurityUtils.canAccessOrganization(createDto.getOrganizationId())) {
            throw new SecurityException("Access denied to organization");
        }

        // Vérifier que l'organisation peut créer une nouvelle agence
        if (!organizationService.canCreateAgency(createDto.getOrganizationId())) {
            throw new IllegalStateException("Organization has reached the maximum number of agencies allowed by subscription");
        }

        // Vérifier l'unicité du nom dans l'organisation
        if (agencyRepository.existsByOrganizationIdAndName(createDto.getOrganizationId(), createDto.getName())) {
            throw new IllegalArgumentException("Agency name already exists in this organization");
        }

        // Vérifier le gestionnaire s'il est spécifié
        if (createDto.getManagerId() != null) {
            validateManager(createDto.getManagerId(), createDto.getOrganizationId());
        }

        // Créer l'agence
        Agency agency = mapToEntity(createDto);
        agency.setCreatedBy(SecurityUtils.getCurrentUserId());
        agency.setUpdatedBy(SecurityUtils.getCurrentUserId());

        Agency savedAgency = agencyRepository.save(agency);

        // Incrémenter le compteur d'agences de l'organisation
        organizationService.incrementAgencyCount(createDto.getOrganizationId());

        log.info("Agency created successfully with ID: {}", savedAgency.getId());
        return mapToDto(savedAgency);
    }

    /**
     * Met à jour une agence
     */
    @Transactional
    public AgencyDto updateAgency(UUID agencyId, UpdateAgencyDto updateDto) {
        log.info("Updating agency: {}", agencyId);

        Agency agency = getAgencyOrThrow(agencyId);

        // Vérifier les permissions
        validateAgencyAccess(agency);

        // Vérifier l'unicité du nom si changé
        if (updateDto.getName() != null && !updateDto.getName().equals(agency.getName())) {
            if (agencyRepository.existsByOrganizationIdAndName(agency.getOrganizationId(), updateDto.getName())) {
                throw new IllegalArgumentException("Agency name already exists in this organization");
            }
        }

        // Vérifier le gestionnaire s'il est modifié
        if (updateDto.getManagerId() != null && !updateDto.getManagerId().equals(agency.getManagerId())) {
            validateManager(updateDto.getManagerId(), agency.getOrganizationId());
        }

        // Appliquer les mises à jour
        updateEntityFromDto(agency, updateDto);
        agency.setUpdatedBy(SecurityUtils.getCurrentUserId());
        agency.setUpdatedAt(LocalDateTime.now());

        Agency savedAgency = agencyRepository.save(agency);
        log.info("Agency updated successfully: {}", agencyId);

        return mapToDto(savedAgency);
    }

    /**
     * Récupère une agence par ID
     */
    public AgencyDto getAgencyById(UUID agencyId) {
        log.debug("Fetching agency: {}", agencyId);

        Agency agency = getAgencyOrThrow(agencyId);
        validateAgencyAccess(agency);

        return mapToDto(agency);
    }

    /**
     * Récupère toutes les agences d'une organisation
     */
    public List<AgencyDto> getAgenciesByOrganizationId(UUID organizationId) {
        log.debug("Fetching agencies for organization: {}", organizationId);

        // Vérifier les permissions
        if (!SecurityUtils.canAccessOrganization(organizationId)) {
            throw new SecurityException("Access denied to organization");
        }

        List<Agency> agencies = agencyRepository.findByOrganizationId(organizationId);

        return agencies.stream()
            .filter(this::canAccessAgency) // Filtrer selon les permissions utilisateur
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    /**
     * Récupère les agences actives d'une organisation
     */
    public List<AgencyDto> getActiveAgenciesByOrganizationId(UUID organizationId) {
        log.debug("Fetching active agencies for organization: {}", organizationId);

        if (!SecurityUtils.canAccessOrganization(organizationId)) {
            throw new SecurityException("Access denied to organization");
        }

        List<Agency> agencies = agencyRepository.findActiveByOrganizationId(organizationId);

        return agencies.stream()
            .filter(this::canAccessAgency)
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    /**
     * Récupère les agences accessibles à l'utilisateur connecté
     */
    public List<AgencyDto> getCurrentUserAgencies() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UUID currentUserAgencyId = SecurityUtils.getCurrentUserAgencyId();

        if (currentUserId == null) {
            return List.of();
        }

        // Si l'utilisateur est lié à une agence spécifique, retourner uniquement celle-ci
        if (SecurityUtils.isCurrentUserStaff() && currentUserAgencyId != null) {
            Optional<Agency> agency = agencyRepository.findById(currentUserAgencyId);
            return agency.map(a -> List.of(mapToDto(a))).orElse(List.of());
        }

        // Sinon, retourner toutes les agences de l'organisation
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        if (organizationId != null) {
            return getActiveAgenciesByOrganizationId(organizationId);
        }

        return List.of();
    }

    /**
     * Active une agence
     */
    @Transactional
    public AgencyDto activateAgency(UUID agencyId) {
        log.info("Activating agency: {}", agencyId);

        Agency agency = getAgencyOrThrow(agencyId);
        validateAgencyAccess(agency);

        agency.activate();
        agency.setUpdatedBy(SecurityUtils.getCurrentUserId());

        Agency savedAgency = agencyRepository.save(agency);
        log.info("Agency activated successfully: {}", agencyId);

        return mapToDto(savedAgency);
    }

    /**
     * Désactive une agence
     */
    @Transactional
    public AgencyDto deactivateAgency(UUID agencyId) {
        log.info("Deactivating agency: {}", agencyId);

        Agency agency = getAgencyOrThrow(agencyId);
        validateAgencyAccess(agency);

        agency.deactivate();
        agency.setUpdatedBy(SecurityUtils.getCurrentUserId());

        Agency savedAgency = agencyRepository.save(agency);
        log.info("Agency deactivated successfully: {}", agencyId);

        return mapToDto(savedAgency);
    }

    /**
     * Supprime une agence
     */
    @Transactional
    public void deleteAgency(UUID agencyId) {
        log.info("Deleting agency: {}", agencyId);

        Agency agency = getAgencyOrThrow(agencyId);
        validateAgencyAccess(agency);

        // Vérifier qu'aucun véhicule ou chauffeur n'est associé
        if (agency.getCurrentVehicles() > 0) {
            throw new IllegalStateException("Cannot delete agency with vehicles. Move or delete vehicles first.");
        }

        if (agency.getCurrentDrivers() > 0) {
            throw new IllegalStateException("Cannot delete agency with drivers. Move or delete drivers first.");
        }

        if (agency.getCurrentStaff() > 0) {
            throw new IllegalStateException("Cannot delete agency with staff. Reassign staff first.");
        }

        // Supprimer l'agence
        agencyRepository.delete(agency);

        // Décrémenter le compteur d'agences de l'organisation
        organizationService.decrementAgencyCount(agency.getOrganizationId());

        log.info("Agency deleted successfully: {}", agencyId);
    }

    /**
     * Assigne un gestionnaire à une agence
     */
    @Transactional
    public AgencyDto assignManager(UUID agencyId, UUID managerId) {
        log.info("Assigning manager {} to agency {}", managerId, agencyId);

        Agency agency = getAgencyOrThrow(agencyId);
        validateAgencyAccess(agency);

        // Valider le gestionnaire
        validateManager(managerId, agency.getOrganizationId());

        agency.setManager(managerId);
        agency.setUpdatedBy(SecurityUtils.getCurrentUserId());

        Agency savedAgency = agencyRepository.save(agency);
        log.info("Manager assigned successfully");

        return mapToDto(savedAgency);
    }

    /**
     * Retire le gestionnaire d'une agence
     */
    @Transactional
    public AgencyDto removeManager(UUID agencyId) {
        log.info("Removing manager from agency {}", agencyId);

        Agency agency = getAgencyOrThrow(agencyId);
        validateAgencyAccess(agency);

        agency.setManager(null);
        agency.setUpdatedBy(SecurityUtils.getCurrentUserId());

        Agency savedAgency = agencyRepository.save(agency);
        log.info("Manager removed successfully");

        return mapToDto(savedAgency);
    }

    /**
     * Assigne un membre du personnel à une agence
     */
    @Transactional
    public void assignStaffToAgency(AssignStaffToAgencyDto assignDto) {
        log.info("Assigning staff {} to agency {}", assignDto.getUserId(), assignDto.getAgencyId());

        Agency agency = getAgencyOrThrow(assignDto.getAgencyId());
        validateAgencyAccess(agency);

        User user = userRepository.findById(assignDto.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Vérifier que l'utilisateur appartient à la même organisation
        if (!agency.getOrganizationId().equals(user.getOrganizationId())) {
            throw new IllegalArgumentException("User does not belong to the same organization as the agency");
        }

        // Vérifier que l'utilisateur est du type STAFF
        if (!user.isStaff()) {
            throw new IllegalArgumentException("Only staff users can be assigned to agencies");
        }

        // Assigner l'utilisateur à l'agence
        user.assignToAgency(assignDto.getAgencyId());

        // Mettre à jour les informations employé
        if (assignDto.getEmployeeId() != null || assignDto.getDepartment() != null ||
            assignDto.getPosition() != null || assignDto.getSupervisorId() != null) {
            user.setEmployeeInfo(
                assignDto.getEmployeeId(),
                assignDto.getDepartment(),
                assignDto.getPosition(),
                assignDto.getSupervisorId()
            );
        }

        userRepository.save(user);

        // Incrémenter le compteur de personnel de l'agence
        agency.incrementStaffCount();
        agencyRepository.save(agency);

        log.info("Staff assigned to agency successfully");
    }

    /**
     * Retire un membre du personnel d'une agence
     */
    @Transactional
    public void removeStaffFromAgency(UUID agencyId, UUID userId) {
        log.info("Removing staff {} from agency {}", userId, agencyId);

        Agency agency = getAgencyOrThrow(agencyId);
        validateAgencyAccess(agency);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Vérifier que l'utilisateur est bien assigné à cette agence
        if (!agency.getId().equals(user.getAgencyId())) {
            throw new IllegalArgumentException("User is not assigned to this agency");
        }

        // Retirer l'utilisateur de l'agence
        user.removeFromAgency();
        userRepository.save(user);

        // Décrémenter le compteur de personnel de l'agence
        agency.decrementStaffCount();
        agencyRepository.save(agency);

        log.info("Staff removed from agency successfully");
    }

    /**
     * Récupère les statistiques d'une agence
     */
    public AgencyStatsDto getAgencyStats(UUID agencyId) {
        log.debug("Fetching agency stats: {}", agencyId);

        Agency agency = getAgencyOrThrow(agencyId);
        validateAgencyAccess(agency);

        return AgencyStatsDto.builder()
            .agencyId(agencyId)
            .agencyName(agency.getName())
            .totalVehicles(agency.getCurrentVehicles())
            .totalDrivers(agency.getCurrentDrivers())
            .totalStaff(agency.getCurrentStaff())
            .build();
    }

    /**
     * Recherche d'agences par critères
     */
    public List<AgencyDto> searchAgencies(UUID organizationId, String city, String region, boolean activeOnly) {
        log.debug("Searching agencies for organization: {} in city: {}, region: {}", organizationId, city, region);

        if (!SecurityUtils.canAccessOrganization(organizationId)) {
            throw new SecurityException("Access denied to organization");
        }

        List<Agency> agencies;

        if (city != null && region != null) {
            agencies = agencyRepository.findByCityAndRegion(city, region);
        } else if (city != null) {
            agencies = agencyRepository.findByOrganizationIdAndCity(organizationId, city);
        } else {
            agencies = activeOnly
                ? agencyRepository.findActiveByOrganizationId(organizationId)
                : agencyRepository.findByOrganizationId(organizationId);
        }

        return agencies.stream()
            .filter(agency -> agency.getOrganizationId().equals(organizationId))
            .filter(this::canAccessAgency)
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private Agency getAgencyOrThrow(UUID agencyId) {
        return agencyRepository.findById(agencyId)
            .orElseThrow(() -> new IllegalArgumentException("Agency not found: " + agencyId));
    }

    private void validateAgencyAccess(Agency agency) {
        if (!canAccessAgency(agency)) {
            throw new SecurityException("Access denied to agency");
        }
    }

    private boolean canAccessAgency(Agency agency) {
        // Super admin peut tout voir
        if (SecurityUtils.isCurrentUserSuperAdmin()) {
            return true;
        }

        // Propriétaire peut voir toutes les agences de son organisation
        if (SecurityUtils.isCurrentUserOwner() &&
            SecurityUtils.canAccessOrganization(agency.getOrganizationId())) {
            return true;
        }

        // Staff ne peut voir que son agence
        if (SecurityUtils.isCurrentUserStaff()) {
            UUID userAgencyId = SecurityUtils.getCurrentUserAgencyId();
            return userAgencyId != null && userAgencyId.equals(agency.getId());
        }

        return false;
    }

    private void validateManager(UUID managerId, UUID organizationId) {
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new IllegalArgumentException("Manager not found"));

        // Vérifier que le gestionnaire appartient à la même organisation
        if (!organizationId.equals(manager.getOrganizationId())) {
            throw new IllegalArgumentException("Manager must belong to the same organization");
        }

        // Vérifier que le gestionnaire a les permissions appropriées (optionnel)
        // Cette vérification pourrait être faite via le système de rôles
    }

    private Agency mapToEntity(CreateAgencyDto createDto) {
        Agency agency = new Agency(
            createDto.getName(),
            createDto.getOrganizationId(),
            createDto.getAddress(),
            createDto.getCity(),
            createDto.getCountry()
        );

        agency.setDescription(createDto.getDescription());
        agency.setPostalCode(createDto.getPostalCode());
        agency.setRegion(createDto.getRegion());
        agency.setPhone(createDto.getPhone());
        agency.setEmail(createDto.getEmail());
        agency.setManagerId(createDto.getManagerId());
        agency.setIs24Hours(createDto.getIs24Hours());
        agency.setGeofenceZoneId(createDto.getGeofenceZoneId());
        agency.setGeofenceRadius(createDto.getGeofenceRadius());

        // Mapper la géolocalisation
        if (createDto.getGeoLocation() != null) {
            agency.setGeoLocation(mapGeoLocationToEntity(createDto.getGeoLocation()));
        }

        // Mapper les horaires de travail
        if (createDto.getWorkingHours() != null) {
            agency.setWorkingHours(mapWorkingHoursToEntity(createDto.getWorkingHours()));
        }

        // Mapper les paramètres
        if (createDto.getSettings() != null) {
            agency.setSettings(mapSettingsToEntity(createDto.getSettings()));
        }

        return agency;
    }

    private void updateEntityFromDto(Agency agency, UpdateAgencyDto updateDto) {
        if (updateDto.getName() != null) {
            agency.setName(updateDto.getName());
        }
        if (updateDto.getDescription() != null) {
            agency.setDescription(updateDto.getDescription());
        }
        if (updateDto.getAddress() != null) {
            agency.setAddress(updateDto.getAddress());
        }
        if (updateDto.getCity() != null) {
            agency.setCity(updateDto.getCity());
        }
        if (updateDto.getCountry() != null) {
            agency.setCountry(updateDto.getCountry());
        }
        if (updateDto.getPostalCode() != null) {
            agency.setPostalCode(updateDto.getPostalCode());
        }
        if (updateDto.getRegion() != null) {
            agency.setRegion(updateDto.getRegion());
        }
        if (updateDto.getPhone() != null) {
            agency.setPhone(updateDto.getPhone());
        }
        if (updateDto.getEmail() != null) {
            agency.setEmail(updateDto.getEmail());
        }
        if (updateDto.getManagerId() != null) {
            agency.setManagerId(updateDto.getManagerId());
        }
        if (updateDto.getIs24Hours() != null) {
            agency.setIs24Hours(updateDto.getIs24Hours());
        }
        if (updateDto.getIsActive() != null) {
            agency.setIsActive(updateDto.getIsActive());
        }
        if (updateDto.getGeofenceZoneId() != null) {
            agency.setGeofenceZoneId(updateDto.getGeofenceZoneId());
        }
        if (updateDto.getGeofenceRadius() != null) {
            agency.setGeofenceRadius(updateDto.getGeofenceRadius());
        }

        // Mettre à jour la géolocalisation
        if (updateDto.getGeoLocation() != null) {
            agency.setGeoLocation(mapGeoLocationToEntity(updateDto.getGeoLocation()));
        }

        // Mettre à jour les horaires
        if (updateDto.getWorkingHours() != null) {
            agency.setWorkingHours(mapWorkingHoursToEntity(updateDto.getWorkingHours()));
        }

        // Mettre à jour les paramètres
        if (updateDto.getSettings() != null) {
            agency.setSettings(mapSettingsToEntity(updateDto.getSettings()));
        }
    }

    private AgencyDto mapToDto(Agency agency) {
        AgencyDto.AgencyDtoBuilder builder = AgencyDto.builder()
            .id(agency.getId())
            .organizationId(agency.getOrganizationId())
            .name(agency.getName())
            .description(agency.getDescription())
            .address(agency.getAddress())
            .city(agency.getCity())
            .country(agency.getCountry())
            .postalCode(agency.getPostalCode())
            .region(agency.getRegion())
            .phone(agency.getPhone())
            .email(agency.getEmail())
            .managerId(agency.getManagerId())
            .isActive(agency.getIsActive())
            .is24Hours(agency.getIs24Hours())
            .currentVehicles(agency.getCurrentVehicles())
            .currentDrivers(agency.getCurrentDrivers())
            .currentStaff(agency.getCurrentStaff())
            .geofenceZoneId(agency.getGeofenceZoneId())
            .geofenceRadius(agency.getGeofenceRadius())
            .createdAt(agency.getCreatedAt())
            .updatedAt(agency.getUpdatedAt())
            .createdBy(agency.getCreatedBy())
            .updatedBy(agency.getUpdatedBy())
            .fullAddress(agency.getFullAddress())
            .isOpenNow(agency.isOpenNow());

        // Mapper la géolocalisation
        if (agency.getGeoLocation() != null) {
            builder.geoLocation(mapGeoLocationToDto(agency.getGeoLocation()));
        }

        // Mapper les horaires
        if (agency.getWorkingHours() != null) {
            builder.workingHours(mapWorkingHoursToDto(agency.getWorkingHours()));
        }

        // Mapper les paramètres
        if (agency.getSettings() != null) {
            builder.settings(mapSettingsToDto(agency.getSettings()));
        }

        // Ajouter les informations du gestionnaire
        if (agency.getManagerId() != null) {
            try {
                Optional<User> manager = userRepository.findById(agency.getManagerId());
                manager.ifPresent(m -> builder.managerName(m.getFullName()));
            } catch (Exception e) {
                log.warn("Error loading manager info for agency: {}", agency.getId(), e);
            }
        }

        return builder.build();
    }

    private Agency.GeoLocation mapGeoLocationToEntity(GeoLocationDto dto) {
        return new Agency.GeoLocation(
            dto.getLatitude(),
            dto.getLongitude(),
            dto.getGooglePlaceId(),
            dto.getTimezone()
        );
    }

    private GeoLocationDto mapGeoLocationToDto(Agency.GeoLocation geoLocation) {
        return new GeoLocationDto(
            geoLocation.getLatitude(),
            geoLocation.getLongitude(),
            geoLocation.getGooglePlaceId(),
            geoLocation.getTimezone()
        );
    }

    private java.util.Map<String, Agency.WorkingHours> mapWorkingHoursToEntity(java.util.Map<String, WorkingHoursDto> dtoMap) {
        return dtoMap.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                java.util.Map.Entry::getKey,
                entry -> new Agency.WorkingHours(
                    entry.getValue().getIsOpen(),
                    entry.getValue().getOpenTime(),
                    entry.getValue().getCloseTime(),
                    entry.getValue().getBreakStartTime(),
                    entry.getValue().getBreakEndTime()
                )
            ));
    }

    private java.util.Map<String, WorkingHoursDto> mapWorkingHoursToDto(java.util.Map<String, Agency.WorkingHours> entityMap) {
        return entityMap.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                java.util.Map.Entry::getKey,
                entry -> new WorkingHoursDto(
                    entry.getValue().getIsOpen(),
                    entry.getValue().getOpenTime(),
                    entry.getValue().getCloseTime(),
                    entry.getValue().getBreakStartTime(),
                    entry.getValue().getBreakEndTime()
                )
            ));
    }

    private Agency.AgencySettings mapSettingsToEntity(AgencySettingsDto dto) {
        Agency.AgencySettings settings = new Agency.AgencySettings();
        settings.setAllowSelfService(dto.getAllowSelfService());
        settings.setRequireInspection(dto.getRequireInspection());
        settings.setEnableKeyBox(dto.getEnableKeyBox());
        settings.setNotifyOnReservation(dto.getNotifyOnReservation());
        settings.setNotifyOnReturn(dto.getNotifyOnReturn());
        settings.setNotifyOnLateReturn(dto.getNotifyOnLateReturn());
        settings.setEnableGeofenceAlerts(dto.getEnableGeofenceAlerts());
        settings.setTrackVehiclesRealTime(dto.getTrackVehiclesRealTime());
        settings.setAllowInstantBooking(dto.getAllowInstantBooking());
        settings.setAdvanceBookingDays(dto.getAdvanceBookingDays());
        settings.setMinBookingNoticeHours(dto.getMinBookingNoticeHours());
        return settings;
    }

    private AgencySettingsDto mapSettingsToDto(Agency.AgencySettings settings) {
        AgencySettingsDto dto = new AgencySettingsDto();
        dto.setAllowSelfService(settings.getAllowSelfService());
        dto.setRequireInspection(settings.getRequireInspection());
        dto.setEnableKeyBox(settings.getEnableKeyBox());
        dto.setNotifyOnReservation(settings.getNotifyOnReservation());
        dto.setNotifyOnReturn(settings.getNotifyOnReturn());
        dto.setNotifyOnLateReturn(settings.getNotifyOnLateReturn());
        dto.setEnableGeofenceAlerts(settings.getEnableGeofenceAlerts());
        dto.setTrackVehiclesRealTime(settings.getTrackVehiclesRealTime());
        dto.setAllowInstantBooking(settings.getAllowInstantBooking());
        dto.setAdvanceBookingDays(settings.getAdvanceBookingDays());
        dto.setMinBookingNoticeHours(settings.getMinBookingNoticeHours());
        return dto;
    }
}
