package inc.yowyob.rental_api.organization.service;

import inc.yowyob.rental_api.organization.dto.*;
import inc.yowyob.rental_api.organization.entities.Organization;
import inc.yowyob.rental_api.organization.repository.OrganizationRepository;
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

        // Récupérer l'utilisateur pour obtenir son organizationId
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser == null || currentUser.getOrganizationId() == null) {
            return Optional.empty();
        }

        Organization organization = organizationRepository.findById(currentUser.getOrganizationId()).orElse(null);
        if (organization == null) {
            return Optional.empty();
        }

        return Optional.of(mapToDto(organization));
    }

    /**
     * Incrémente le compteur d'agences de l'organisation
     */
    @Transactional
    public void incrementAgencyCount(UUID organizationId) {
        log.info("Incrementing agency count for organization: {}", organizationId);

        Organization organization = getOrganizationOrThrow(organizationId);

        // Incrémenter le compteur
        Integer currentCount = organization.getCurrentAgencies() != null ? organization.getCurrentAgencies() : 0;
        organization.setCurrentAgencies(currentCount + 1);
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setUpdatedBy(SecurityUtils.getCurrentUserId());

        organizationRepository.save(organization);

        log.debug("Agency count incremented for organization: {} (new count: {})", organizationId, currentCount + 1);
    }

    /**
     * Décrémente le compteur d'agences de l'organisation
     */
    @Transactional
    public void decrementAgencyCount(UUID organizationId) {
        log.info("Decrementing agency count for organization: {}", organizationId);

        Organization organization = getOrganizationOrThrow(organizationId);

        // Décrémenter le compteur (minimum 0)
        Integer currentCount = organization.getCurrentAgencies() != null ? organization.getCurrentAgencies() : 0;
        organization.setCurrentAgencies(Math.max(0, currentCount - 1));
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setUpdatedBy(SecurityUtils.getCurrentUserId());

        organizationRepository.save(organization);

        log.debug("Agency count decremented for organization: {} (new count: {})", organizationId, Math.max(0, currentCount - 1));
    }

    /**
     * Vérifie si l'organisation peut créer une nouvelle agence
     */
    public boolean canCreateAgency(UUID organizationId) {
        log.debug("Checking if organization {} can create new agency", organizationId);

        Organization organization = getOrganizationOrThrow(organizationId);

        // Vérifier les limites d'abonnement
        Integer currentAgencies = organization.getCurrentAgencies() != null ? organization.getCurrentAgencies() : 0;
        Integer maxAgencies = organization.getMaxAgencies() != null ? organization.getMaxAgencies() : 0;

        if (currentAgencies >= maxAgencies) {
            log.warn("Organization {} has reached maximum agencies limit: {}/{}",
                organizationId, currentAgencies, maxAgencies);
            return false;
        }

        return true;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Récupère l'organisation ou lance une exception
     */
    private Organization getOrganizationOrThrow(UUID organizationId) {
        return organizationRepository.findById(organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));
    }

    /**
     * Valide l'accès à une organisation
     */
    private void validateOrganizationAccess(Organization organization) {
        if (!canAccessOrganization(organization)) {
            throw new SecurityException("Access denied to organization");
        }
    }

    /**
     * Vérifie si l'utilisateur connecté peut accéder à l'organisation
     */
    private boolean canAccessOrganization(Organization organization) {
        // Super admin peut tout voir
        if (SecurityUtils.isCurrentUserSuperAdmin()) {
            return true;
        }

        // Propriétaire peut voir son organisation
        if (SecurityUtils.isCurrentUserOwner()) {
            UUID currentUserOrgId = SecurityUtils.getCurrentUserOrganizationId();
            return organization.getId().equals(currentUserOrgId);
        }

        // Staff peut voir son organisation
        if (SecurityUtils.isCurrentUserStaff()) {
            UUID currentUserOrgId = SecurityUtils.getCurrentUserOrganizationId();
            return organization.getId().equals(currentUserOrgId);
        }

        return false;
    }

    /**
     * Mappe CreateOrganizationDto vers Organization
     */
    private Organization mapToEntity(CreateOrganizationDto createDto, UUID ownerId) {
        Organization organization = new Organization();
        organization.setName(createDto.getName());
        organization.setOrganizationType(createDto.getOrganizationType());
        organization.setDescription(createDto.getDescription());
        organization.setOwnerId(ownerId);
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
        organization.setIsActive(true);
        organization.setIsVerified(false);

        // Initialiser les compteurs
        organization.setCurrentAgencies(0);
        organization.setCurrentVehicles(0);
        organization.setCurrentDrivers(0);
        organization.setCurrentUsers(0);

        // Mapper les politiques si fournies
        if (createDto.getPolicies() != null) {
            organization.setPolicies(mapPolicyToEntity(createDto.getPolicies()));
        }

        // Mapper les paramètres si fournis
        if (createDto.getSettings() != null) {
            organization.setSettings(mapSettingsToEntity(createDto.getSettings()));
        }

        organization.setCreatedAt(LocalDateTime.now());
        organization.setUpdatedAt(LocalDateTime.now());

        return organization;
    }

    /**
     * Met à jour l'entité à partir du DTO
     */
    private void updateEntityFromDto(Organization organization, UpdateOrganizationDto updateDto) {
        if (updateDto.getName() != null) {
            organization.setName(updateDto.getName());
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

        // Mettre à jour les politiques
        if (updateDto.getPolicies() != null) {
            organization.setPolicies(mapPolicyToEntity(updateDto.getPolicies()));
        }

        // Mettre à jour les paramètres
        if (updateDto.getSettings() != null) {
            organization.setSettings(mapSettingsToEntity(updateDto.getSettings()));
        }
    }

    /**
     * Mappe OrganizationPolicyDto vers Organization.OrganizationPolicy
     */
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

    /**
     * Mappe OrganizationSettingsDto vers Organization.OrganizationSettings
     */
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

    /**
     * Mappe Organization vers OrganizationDto
     */
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
            .updatedBy(organization.getUpdatedBy());

        // Mapper les politiques
        if (organization.getPolicies() != null) {
            builder.policies(mapPolicyToDto(organization.getPolicies()));
        }

        // Mapper les paramètres
        if (organization.getSettings() != null) {
            builder.settings(mapSettingsToDto(organization.getSettings()));
        }

        // Calculer les pourcentages d'utilisation
        if (organization.getMaxAgencies() != null && organization.getMaxAgencies() > 0) {
            double agencyUsage = (organization.getCurrentAgencies() != null ? organization.getCurrentAgencies() : 0) * 100.0 / organization.getMaxAgencies();
            builder.agencyUsagePercentage(agencyUsage);
        }

        if (organization.getMaxVehicles() != null && organization.getMaxVehicles() > 0) {
            double vehicleUsage = (organization.getCurrentVehicles() != null ? organization.getCurrentVehicles() : 0) * 100.0 / organization.getMaxVehicles();
            builder.vehicleUsagePercentage(vehicleUsage);
        }

        if (organization.getMaxDrivers() != null && organization.getMaxDrivers() > 0) {
            double driverUsage = (organization.getCurrentDrivers() != null ? organization.getCurrentDrivers() : 0) * 100.0 / organization.getMaxDrivers();
            builder.driverUsagePercentage(driverUsage);
        }

        if (organization.getMaxUsers() != null && organization.getMaxUsers() > 0) {
            double userUsage = (organization.getCurrentUsers() != null ? organization.getCurrentUsers() : 0) * 100.0 / organization.getMaxUsers();
            builder.userUsagePercentage(userUsage);
        }

        return builder.build();
    }

    /**
     * Mappe Organization.OrganizationPolicy vers OrganizationPolicyDto
     */
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

    /**
     * Mappe Organization.OrganizationSettings vers OrganizationSettingsDto
     */
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

    /**
     * Met à jour les limites de l'organisation selon le plan d'abonnement
     */
    @Transactional
    public void updateOrganizationLimits(UUID organizationId, SubscriptionPlan plan) {
        log.info("Updating organization limits for organization: {} with plan: {}", organizationId, plan.getName());

        Organization organization = getOrganizationOrThrow(organizationId);

        // Mettre à jour les limites selon le plan
        organization.setMaxAgencies(plan.getMaxAgencies());
        organization.setMaxVehicles(plan.getMaxVehicles());
        organization.setMaxDrivers(plan.getMaxDrivers());

        // Mettre à jour les métadonnées
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setUpdatedBy(SecurityUtils.getCurrentUserId());

        organizationRepository.save(organization);

        log.info("Organization limits updated successfully for organization: {} - Agencies: {}, Vehicles: {}, Drivers: {}",
            organizationId, plan.getMaxAgencies(), plan.getMaxVehicles(), plan.getMaxDrivers());
    }

    /**
     * Met à jour les limites de l'organisation avec des valeurs spécifiques
     */
    @Transactional
    public void updateOrganizationLimits(UUID organizationId, Integer maxAgencies, Integer maxVehicles,
                                         Integer maxDrivers, Integer maxUsers) {
        log.info("Updating organization limits for organization: {} with custom values", organizationId);

        Organization organization = getOrganizationOrThrow(organizationId);

        // Mettre à jour les limites avec les valeurs fournies
        if (maxAgencies != null) {
            organization.setMaxAgencies(maxAgencies);
        }
        if (maxVehicles != null) {
            organization.setMaxVehicles(maxVehicles);
        }
        if (maxDrivers != null) {
            organization.setMaxDrivers(maxDrivers);
        }
        if (maxUsers != null) {
            organization.setMaxUsers(maxUsers);
        }

        // Mettre à jour les métadonnées
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setUpdatedBy(SecurityUtils.getCurrentUserId());

        organizationRepository.save(organization);

        log.info("Organization limits updated successfully with custom values for organization: {}", organizationId);
    }

    /**
     * Récupère les limites d'utilisation actuelles de l'organisation
     */
    public OrganizationUsageLimitsDto getUsageLimits(UUID organizationId) {
        log.debug("Fetching usage limits for organization: {}", organizationId);

        Organization organization = getOrganizationOrThrow(organizationId);
        validateOrganizationAccess(organization);

        return OrganizationUsageLimitsDto.builder()
            .organizationId(organizationId)
            .maxAgencies(organization.getMaxAgencies())
            .maxVehicles(organization.getMaxVehicles())
            .maxDrivers(organization.getMaxDrivers())
            .maxUsers(organization.getMaxUsers())
            .currentAgencies(organization.getCurrentAgencies())
            .currentVehicles(organization.getCurrentVehicles())
            .currentDrivers(organization.getCurrentDrivers())
            .currentUsers(organization.getCurrentUsers())
            .agencyUsagePercentage(calculateUsagePercentage(organization.getCurrentAgencies(), organization.getMaxAgencies()))
            .vehicleUsagePercentage(calculateUsagePercentage(organization.getCurrentVehicles(), organization.getMaxVehicles()))
            .driverUsagePercentage(calculateUsagePercentage(organization.getCurrentDrivers(), organization.getMaxDrivers()))
            .userUsagePercentage(calculateUsagePercentage(organization.getCurrentUsers(), organization.getMaxUsers()))
            .canCreateAgency(organization.getCurrentAgencies() < organization.getMaxAgencies())
            .canCreateVehicle(organization.getCurrentVehicles() < organization.getMaxVehicles())
            .canCreateDriver(organization.getCurrentDrivers() < organization.getMaxDrivers())
            .canCreateUser(organization.getCurrentUsers() < organization.getMaxUsers())
            .build();
    }

    /**
     * Calcule le pourcentage d'utilisation
     */
    private Double calculateUsagePercentage(Integer current, Integer max) {
        if (max == null || max == 0) {
            return 0.0;
        }
        if (current == null) {
            return 0.0;
        }
        return (current * 100.0) / max;
    }
}
