package inc.yowyob.rental_api.organization.service;

import inc.yowyob.rental_api.organization.dto.*;
import inc.yowyob.rental_api.organization.entities.Organization;
import inc.yowyob.rental_api.organization.repository.OrganizationRepository;
import inc.yowyob.rental_api.subscription.entities.OrganizationSubscription;
import inc.yowyob.rental_api.subscription.entities.SubscriptionPlan;
import inc.yowyob.rental_api.subscription.service.SubscriptionService;
import inc.yowyob.rental_api.security.util.SecurityUtils;
import inc.yowyob.rental_api.user.entities.User;
import inc.yowyob.rental_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    /**
     * Crée une nouvelle organisation
     */
    @Transactional
    public OrganizationDto createOrganization(CreateOrganizationDto createDto, UUID ownerId) {
        log.info("Creating organization: {} for owner: {}", createDto.getName(), ownerId);

        // Vérifier que le propriétaire existe
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        // Vérifier que le propriétaire n'a pas déjà une organisation
        if (organizationRepository.findByOwnerId(ownerId).isPresent()) {
            throw new IllegalStateException("Owner already has an organization");
        }

        // Vérifier l'unicité du nom
        if (organizationRepository.existsByName(createDto.getName())) {
            throw new IllegalArgumentException("Organization name already exists");
        }

        // Vérifier l'unicité des numéros d'enregistrement
        if (createDto.getRegistrationNumber() != null &&
            organizationRepository.existsByRegistrationNumber(createDto.getRegistrationNumber())) {
            throw new IllegalArgumentException("Registration number already exists");
        }

        // Créer l'organisation
        Organization organization = mapToEntity(createDto, ownerId);
        organization.setCreatedBy(ownerId);
        organization.setUpdatedBy(ownerId);

        Organization savedOrganization = organizationRepository.save(organization);

        // Mettre à jour l'utilisateur propriétaire avec l'ID de l'organisation
        owner.setOrganizationId(savedOrganization.getId());
        userRepository.save(owner);

        log.info("Organization created successfully with ID: {}", savedOrganization.getId());
        return mapToDto(savedOrganization);
    }

    /**
     * Met à jour une organisation
     */
    @Transactional
    public OrganizationDto updateOrganization(UUID organizationId, UpdateOrganizationDto updateDto) {
        log.info("Updating organization: {}", organizationId);

        Organization organization = getOrganizationOrThrow(organizationId);

        // Vérifier les permissions
        validateOrganizationAccess(organization);

        // Vérifier l'unicité du nom si changé
        if (updateDto.getName() != null && !updateDto.getName().equals(organization.getName())) {
            if (organizationRepository.existsByName(updateDto.getName())) {
                throw new IllegalArgumentException("Organization name already exists");
            }
        }

        // Vérifier l'unicité du numéro d'enregistrement si changé
        if (updateDto.getRegistrationNumber() != null &&
            !updateDto.getRegistrationNumber().equals(organization.getRegistrationNumber())) {
            if (organizationRepository.existsByRegistrationNumber(updateDto.getRegistrationNumber())) {
                throw new IllegalArgumentException("Registration number already exists");
            }
        }

        // Appliquer les mises à jour
        updateEntityFromDto(organization, updateDto);
        organization.setUpdatedBy(SecurityUtils.getCurrentUserId());
        organization.setUpdatedAt(LocalDateTime.now());

        Organization savedOrganization = organizationRepository.save(organization);
        log.info("Organization updated successfully: {}", organizationId);

        return mapToDto(savedOrganization);
    }

    /**
     * Récupère une organisation par ID
     */
    public OrganizationDto getOrganizationById(UUID organizationId) {
        log.debug("Fetching organization: {}", organizationId);

        Organization organization = getOrganizationOrThrow(organizationId);
        validateOrganizationAccess(organization);

        return mapToDto(organization);
    }

    /**
     * Récupère l'organisation de l'utilisateur connecté
     */
    public Optional<OrganizationDto> getCurrentUserOrganization() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Optional.empty();
        }

        log.debug("Fetching organization for current user: {}", currentUserId);

        return organizationRepository.findByOwnerId(currentUserId)
            .map(this::mapToDto);
    }

    /**
     * Récupère l'organisation par propriétaire
     */
    public Optional<OrganizationDto> getOrganizationByOwnerId(UUID ownerId) {
        log.debug("Fetching organization for owner: {}", ownerId);

        // Vérifier que l'utilisateur peut accéder à ces informations
        if (!SecurityUtils.isCurrentUserSuperAdmin() &&
            !ownerId.equals(SecurityUtils.getCurrentUserId())) {
            throw new SecurityException("Access denied to organization information");
        }

        return organizationRepository.findByOwnerId(ownerId)
            .map(this::mapToDto);
    }

    /**
     * Active une organisation
     */
    @Transactional
    public OrganizationDto activateOrganization(UUID organizationId) {
        log.info("Activating organization: {}", organizationId);

        Organization organization = getOrganizationOrThrow(organizationId);
        validateOrganizationAccess(organization);

        organization.activate();
        organization.setUpdatedBy(SecurityUtils.getCurrentUserId());

        Organization savedOrganization = organizationRepository.save(organization);
        log.info("Organization activated successfully: {}", organizationId);

        return mapToDto(savedOrganization);
    }

    /**
     * Désactive une organisation
     */
    @Transactional
    public OrganizationDto deactivateOrganization(UUID organizationId) {
        log.info("Deactivating organization: {}", organizationId);

        Organization organization = getOrganizationOrThrow(organizationId);
        validateOrganizationAccess(organization);

        organization.deactivate();
        organization.setUpdatedBy(SecurityUtils.getCurrentUserId());

        Organization savedOrganization = organizationRepository.save(organization);
        log.info("Organization deactivated successfully: {}", organizationId);

        return mapToDto(savedOrganization);
    }

    /**
     * Met à jour les limites de l'organisation basées sur l'abonnement
     */
    @Transactional
    public void updateOrganizationLimits(UUID organizationId, SubscriptionPlan plan) {
        log.info("Updating organization limits for: {} with plan: {}", organizationId, plan.getName());

        Organization organization = getOrganizationOrThrow(organizationId);

        // Calculer les nouvelles limites basées sur le plan
        Integer maxAgencies = plan.getMaxAgencies();
        Integer maxVehicles = plan.getMaxVehicles();
        Integer maxDrivers = plan.getMaxDrivers();
        Integer maxUsers = calculateMaxUsers(plan);

        organization.updateLimits(maxAgencies, maxVehicles, maxDrivers, maxUsers);
        organization.setUpdatedBy(SecurityUtils.getCurrentUserId());

        organizationRepository.save(organization);
        log.info("Organization limits updated successfully");
    }

    /**
     * Vérifie si l'organisation peut créer une nouvelle agence
     */
    public boolean canCreateAgency(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        return organization.canCreateAgency();
    }

    /**
     * Vérifie si l'organisation peut créer un nouveau véhicule
     */
    public boolean canCreateVehicle(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        return organization.canCreateVehicle();
    }

    /**
     * Vérifie si l'organisation peut créer un nouveau chauffeur
     */
    public boolean canCreateDriver(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        return organization.canCreateDriver();
    }

    /**
     * Vérifie si l'organisation peut créer un nouvel utilisateur
     */
    public boolean canCreateUser(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        return organization.canCreateUser();
    }

    /**
     * Incrémente le compteur d'agences
     */
    @Transactional
    public void incrementAgencyCount(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        organization.incrementAgencyCount();
        organizationRepository.save(organization);
    }

    /**
     * Décrémente le compteur d'agences
     */
    @Transactional
    public void decrementAgencyCount(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        organization.decrementAgencyCount();
        organizationRepository.save(organization);
    }

    /**
     * Incrémente le compteur de véhicules
     */
    @Transactional
    public void incrementVehicleCount(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        organization.incrementVehicleCount();
        organizationRepository.save(organization);
    }

    /**
     * Décrémente le compteur de véhicules
     */
    @Transactional
    public void decrementVehicleCount(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        organization.decrementVehicleCount();
        organizationRepository.save(organization);
    }

    /**
     * Incrémente le compteur de chauffeurs
     */
    @Transactional
    public void incrementDriverCount(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        organization.incrementDriverCount();
        organizationRepository.save(organization);
    }

    /**
     * Décrémente le compteur de chauffeurs
     */
    @Transactional
    public void decrementDriverCount(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        organization.decrementDriverCount();
        organizationRepository.save(organization);
    }

    /**
     * Incrémente le compteur d'utilisateurs
     */
    @Transactional
    public void incrementUserCount(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        organization.incrementUserCount();
        organizationRepository.save(organization);
    }

    /**
     * Décrémente le compteur d'utilisateurs
     */
    @Transactional
    public void decrementUserCount(UUID organizationId) {
        Organization organization = getOrganizationOrThrow(organizationId);
        organization.decrementUserCount();
        organizationRepository.save(organization);
    }

    /**
     * Obtient les statistiques de l'organisation
     */
    public OrganizationStatsDto getOrganizationStats(UUID organizationId) {
        log.debug("Fetching organization stats: {}", organizationId);

        Organization organization = getOrganizationOrThrow(organizationId);
        validateOrganizationAccess(organization);

        return OrganizationStatsDto.builder()
            .organizationId(organizationId)
            .organizationName(organization.getName())
            .totalAgencies(organization.getCurrentAgencies())
            .totalVehicles(organization.getCurrentVehicles())
            .totalDrivers(organization.getCurrentDrivers())
            .totalUsers(organization.getCurrentUsers())
            .maxAgencies(organization.getMaxAgencies())
            .maxVehicles(organization.getMaxVehicles())
            .maxDrivers(organization.getMaxDrivers())
            .maxUsers(organization.getMaxUsers())
            .agencyUsagePercentage(organization.getAgencyUsagePercentage())
            .vehicleUsagePercentage(organization.getVehicleUsagePercentage())
            .driverUsagePercentage(organization.getDriverUsagePercentage())
            .userUsagePercentage(organization.getUserUsagePercentage())
            .isActive(organization.getIsActive())
            .isVerified(organization.getIsVerified())
            .createdAt(organization.getCreatedAt())
            .build();
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private Organization getOrganizationOrThrow(UUID organizationId) {
        return organizationRepository.findById(organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));
    }

    private void validateOrganizationAccess(Organization organization) {
        if (!SecurityUtils.canAccessOrganization(organization.getId())) {
            throw new SecurityException("Access denied to organization");
        }
    }

    private Organization mapToEntity(CreateOrganizationDto createDto, UUID ownerId) {
        Organization organization = new Organization(
            createDto.getName(),
            createDto.getOrganizationType(),
            ownerId
        );

        organization.setDescription(createDto.getDescription());
        organization.setRegistrationNumber(createDto.getRegistrationNumber());
        organization.setTaxNumber(createDto.getTaxNumber());
        organization.setBusinessLicense(createDto.getBusinessLicense());
        organization.setAddress(createDto.getAddress());
        organization.setCity(createDto.getCity());
        organization.setCountry(createDto.getCountry());
        organization.setPostalCode(createDto.getPostalCode());
        organization.setRegion(createDto.getRegion());
        organization.setPhone(createDto.getPhone());
        organization.setEmail(createDto.getEmail());
        organization.setWebsite(createDto.getWebsite());
        organization.setLogoUrl(createDto.getLogoUrl());
        organization.setPrimaryColor(createDto.getPrimaryColor());
        organization.setSecondaryColor(createDto.getSecondaryColor());

        // Mapper les politiques
        if (createDto.getPolicies() != null) {
            organization.setPolicies(mapPolicyToEntity(createDto.getPolicies()));
        }

        // Mapper les paramètres
        if (createDto.getSettings() != null) {
            organization.setSettings(mapSettingsToEntity(createDto.getSettings()));
        }

        return organization;
    }

    private void updateEntityFromDto(Organization organization, UpdateOrganizationDto updateDto) {
        if (updateDto.getName() != null) {
            organization.setName(updateDto.getName());
        }
        if (updateDto.getOrganizationType() != null) {
            organization.setOrganizationType(updateDto.getOrganizationType());
        }
        if (updateDto.getDescription() != null) {
            organization.setDescription(updateDto.getDescription());
        }
        if (updateDto.getRegistrationNumber() != null) {
            organization.setRegistrationNumber(updateDto.getRegistrationNumber());
        }
        if (updateDto.getTaxNumber() != null) {
            organization.setTaxNumber(updateDto.getTaxNumber());
        }
        if (updateDto.getBusinessLicense() != null) {
            organization.setBusinessLicense(updateDto.getBusinessLicense());
        }
        if (updateDto.getAddress() != null) {
            organization.setAddress(updateDto.getAddress());
        }
        if (updateDto.getCity() != null) {
            organization.setCity(updateDto.getCity());
        }
        if (updateDto.getCountry() != null) {
            organization.setCountry(updateDto.getCountry());
        }
        if (updateDto.getPostalCode() != null) {
            organization.setPostalCode(updateDto.getPostalCode());
        }
        if (updateDto.getRegion() != null) {
            organization.setRegion(updateDto.getRegion());
        }
        if (updateDto.getPhone() != null) {
            organization.setPhone(updateDto.getPhone());
        }
        if (updateDto.getEmail() != null) {
            organization.setEmail(updateDto.getEmail());
        }
        if (updateDto.getWebsite() != null) {
            organization.setWebsite(updateDto.getWebsite());
        }
        if (updateDto.getLogoUrl() != null) {
            organization.setLogoUrl(updateDto.getLogoUrl());
        }
        if (updateDto.getPrimaryColor() != null) {
            organization.setPrimaryColor(updateDto.getPrimaryColor());
        }
        if (updateDto.getSecondaryColor() != null) {
            organization.setSecondaryColor(updateDto.getSecondaryColor());
        }
        if (updateDto.getIsActive() != null) {
            organization.setIsActive(updateDto.getIsActive());
        }

        // Mettre à jour les politiques
        if (updateDto.getPolicies() != null) {
            organization.setPolicies(mapPolicyToEntity(updateDto.getPolicies()));
        }

        // Mettre à jour les paramètres
        if (updateDto.getSettings() != null) {
            organization.setSettings(mapSettingsToEntity(updateDto.getSettings()));
        }
    }

    private OrganizationDto mapToDto(Organization organization) {
        OrganizationDto.OrganizationDtoBuilder builder = OrganizationDto.builder()
            .id(organization.getId())
            .name(organization.getName())
            .organizationType(organization.getOrganizationType())
            .description(organization.getDescription())
            .ownerId(organization.getOwnerId())
            .registrationNumber(organization.getRegistrationNumber())
            .taxNumber(organization.getTaxNumber())
            .businessLicense(organization.getBusinessLicense())
            .address(organization.getAddress())
            .city(organization.getCity())
            .country(organization.getCountry())
            .postalCode(organization.getPostalCode())
            .region(organization.getRegion())
            .phone(organization.getPhone())
            .email(organization.getEmail())
            .website(organization.getWebsite())
            .isActive(organization.getIsActive())
            .isVerified(organization.getIsVerified())
            .maxAgencies(organization.getMaxAgencies())
            .maxVehicles(organization.getMaxVehicles())
            .maxDrivers(organization.getMaxDrivers())
            .maxUsers(organization.getMaxUsers())
            .currentAgencies(organization.getCurrentAgencies())
            .currentVehicles(organization.getCurrentVehicles())
            .currentDrivers(organization.getCurrentDrivers())
            .currentUsers(organization.getCurrentUsers())
            .logoUrl(organization.getLogoUrl())
            .primaryColor(organization.getPrimaryColor())
            .secondaryColor(organization.getSecondaryColor())
            .createdAt(organization.getCreatedAt())
            .updatedAt(organization.getUpdatedAt())
            .createdBy(organization.getCreatedBy())
            .updatedBy(organization.getUpdatedBy())
            .agencyUsagePercentage(organization.getAgencyUsagePercentage())
            .vehicleUsagePercentage(organization.getVehicleUsagePercentage())
            .driverUsagePercentage(organization.getDriverUsagePercentage())
            .userUsagePercentage(organization.getUserUsagePercentage());

        // Mapper les politiques
        if (organization.getPolicies() != null) {
            builder.policies(mapPolicyToDto(organization.getPolicies()));
        }

        // Mapper les paramètres
        if (organization.getSettings() != null) {
            builder.settings(mapSettingsToDto(organization.getSettings()));
        }

        // Ajouter les informations d'abonnement
        try {
            Optional<OrganizationSubscription> subscription =
                subscriptionService.getActiveSubscription(organization.getId());
            if (subscription.isPresent()) {
                OrganizationSubscription sub = subscription.get();
                Optional<SubscriptionPlan> plan =
                    subscriptionService.getPlanById(sub.getSubscriptionPlanId());
                if (plan.isPresent()) {
                    builder.subscriptionPlan(plan.get().getName());
                }
                builder.subscriptionExpiresAt(sub.getEndDate());
            }
        } catch (Exception e) {
            log.warn("Error loading subscription info for organization: {}", organization.getId(), e);
        }

        return builder.build();
    }

    private Organization.OrganizationPolicy mapPolicyToEntity(OrganizationPolicyDto dto) {
        Organization.OrganizationPolicy policy = new Organization.OrganizationPolicy();
        policy.setWithDriverOption(dto.getWithDriverOption());
        policy.setWithoutDriverOption(dto.getWithoutDriverOption());
        policy.setDriverMandatory(dto.getDriverMandatory());
        policy.setMinRentalHours(dto.getMinRentalHours());
        policy.setMaxRentalDays(dto.getMaxRentalDays());
        policy.setSecurityDeposit(dto.getSecurityDeposit());
        policy.setLateReturnPenalty(dto.getLateReturnPenalty());
        policy.setMinDriverAge(dto.getMinDriverAge());
        policy.setMaxDriverAge(dto.getMaxDriverAge());
        policy.setRequireDriverLicense(dto.getRequireDriverLicense());
        policy.setRequireCreditCard(dto.getRequireCreditCard());
        policy.setAllowWeekendRental(dto.getAllowWeekendRental());
        policy.setAllowHolidayRental(dto.getAllowHolidayRental());
        policy.setCancellationPolicy(dto.getCancellationPolicy());
        policy.setFreeCancellationHours(dto.getFreeCancellationHours());
        policy.setCancellationFeePercentage(dto.getCancellationFeePercentage());
        policy.setRefundPolicy(dto.getRefundPolicy());
        policy.setRefundProcessingDays(dto.getRefundProcessingDays());
        return policy;
    }

    private OrganizationPolicyDto mapPolicyToDto(Organization.OrganizationPolicy policy) {
        OrganizationPolicyDto dto = new OrganizationPolicyDto();
        dto.setWithDriverOption(policy.getWithDriverOption());
        dto.setWithoutDriverOption(policy.getWithoutDriverOption());
        dto.setDriverMandatory(policy.getDriverMandatory());
        dto.setMinRentalHours(policy.getMinRentalHours());
        dto.setMaxRentalDays(policy.getMaxRentalDays());
        dto.setSecurityDeposit(policy.getSecurityDeposit());
        dto.setLateReturnPenalty(policy.getLateReturnPenalty());
        dto.setMinDriverAge(policy.getMinDriverAge());
        dto.setMaxDriverAge(policy.getMaxDriverAge());
        dto.setRequireDriverLicense(policy.getRequireDriverLicense());
        dto.setRequireCreditCard(policy.getRequireCreditCard());
        dto.setAllowWeekendRental(policy.getAllowWeekendRental());
        dto.setAllowHolidayRental(policy.getAllowHolidayRental());
        dto.setCancellationPolicy(policy.getCancellationPolicy());
        dto.setFreeCancellationHours(policy.getFreeCancellationHours());
        dto.setCancellationFeePercentage(policy.getCancellationFeePercentage());
        dto.setRefundPolicy(policy.getRefundPolicy());
        dto.setRefundProcessingDays(policy.getRefundProcessingDays());
        return dto;
    }

    private Organization.OrganizationSettings mapSettingsToEntity(OrganizationSettingsDto dto) {
        Organization.OrganizationSettings settings = new Organization.OrganizationSettings();
        settings.setTimezone(dto.getTimezone());
        settings.setCurrency(dto.getCurrency());
        settings.setLanguage(dto.getLanguage());
        settings.setDateFormat(dto.getDateFormat());
        settings.setEmailNotifications(dto.getEmailNotifications());
        settings.setSmsNotifications(dto.getSmsNotifications());
        settings.setPushNotifications(dto.getPushNotifications());
        settings.setEnableGeofencing(dto.getEnableGeofencing());
        settings.setEnableChat(dto.getEnableChat());
        settings.setEnableAdvancedReports(dto.getEnableAdvancedReports());
        settings.setEnableApiAccess(dto.getEnableApiAccess());
        settings.setRequireTwoFactorAuth(dto.getRequireTwoFactorAuth());
        settings.setPasswordExpirationDays(dto.getPasswordExpirationDays());
        settings.setAuditLogging(dto.getAuditLogging());
        settings.setEnableMobileMoneyPayments(dto.getEnableMobileMoneyPayments());
        settings.setEnableCardPayments(dto.getEnableCardPayments());
        settings.setEnableBankTransfers(dto.getEnableBankTransfers());
        return settings;
    }

    private OrganizationSettingsDto mapSettingsToDto(Organization.OrganizationSettings settings) {
        OrganizationSettingsDto dto = new OrganizationSettingsDto();
        dto.setTimezone(settings.getTimezone());
        dto.setCurrency(settings.getCurrency());
        dto.setLanguage(settings.getLanguage());
        dto.setDateFormat(settings.getDateFormat());
        dto.setEmailNotifications(settings.getEmailNotifications());
        dto.setSmsNotifications(settings.getSmsNotifications());
        dto.setPushNotifications(settings.getPushNotifications());
        dto.setEnableGeofencing(settings.getEnableGeofencing());
        dto.setEnableChat(settings.getEnableChat());
        dto.setEnableAdvancedReports(settings.getEnableAdvancedReports());
        dto.setEnableApiAccess(settings.getEnableApiAccess());
        dto.setRequireTwoFactorAuth(settings.getRequireTwoFactorAuth());
        dto.setPasswordExpirationDays(settings.getPasswordExpirationDays());
        dto.setAuditLogging(settings.getAuditLogging());
        dto.setEnableMobileMoneyPayments(settings.getEnableMobileMoneyPayments());
        dto.setEnableCardPayments(settings.getEnableCardPayments());
        dto.setEnableBankTransfers(settings.getEnableBankTransfers());
        return dto;
    }

    private Integer calculateMaxUsers(SubscriptionPlan plan) {
        // Calculer le nombre max d'utilisateurs basé sur le plan
        // Par exemple agences * 10 + véhicules * 0.5 + chauffeurs
        int baseUsers = plan.getMaxAgencies() * 10; // 10 utilisateurs par agence
        int vehicleUsers = (int) (plan.getMaxVehicles() * 0.5); // 0.5 utilisateur par véhicule
        int driverUsers = plan.getMaxDrivers(); // 1 utilisateur par chauffeur

        return baseUsers + vehicleUsers + driverUsers;
    }
}

/**
 * DTO pour les statistiques d'organisation
 */
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@lombok.Builder
class OrganizationStatsDto {
    private UUID organizationId;
    private String organizationName;
    private Integer totalAgencies;
    private Integer totalVehicles;
    private Integer totalDrivers;
    private Integer totalUsers;
    private Integer maxAgencies;
    private Integer maxVehicles;
    private Integer maxDrivers;
    private Integer maxUsers;
    private Double agencyUsagePercentage;
    private Double vehicleUsagePercentage;
    private Double driverUsagePercentage;
    private Double userUsagePercentage;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}
