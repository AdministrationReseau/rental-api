package inc.yowyob.rental_api.onboarding.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import inc.yowyob.rental_api.core.enums.OnboardingStatus;
import inc.yowyob.rental_api.core.enums.OnboardingStep;
import inc.yowyob.rental_api.core.enums.UserStatus;
import inc.yowyob.rental_api.core.enums.UserType;
import inc.yowyob.rental_api.onboarding.dto.*;
import inc.yowyob.rental_api.onboarding.entities.OnboardingSession;
import inc.yowyob.rental_api.onboarding.repository.OnboardingSessionRepository;
import inc.yowyob.rental_api.organization.dto.CreateOrganizationDto;
import inc.yowyob.rental_api.organization.dto.OrganizationDto;
import inc.yowyob.rental_api.organization.dto.OrganizationPolicyDto;
import inc.yowyob.rental_api.organization.dto.OrganizationSettingsDto;
import inc.yowyob.rental_api.organization.service.OrganizationService;
import inc.yowyob.rental_api.subscription.entities.OrganizationSubscription;
import inc.yowyob.rental_api.subscription.entities.SubscriptionPlan;
import inc.yowyob.rental_api.subscription.service.SubscriptionService;
import inc.yowyob.rental_api.user.entities.User;
import inc.yowyob.rental_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final OnboardingSessionRepository onboardingSessionRepository;
    private final SubscriptionService subscriptionService;
    private final OrganizationService organizationService; // NOUVEAU: Service d'organisation
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    /**
     * Crée une nouvelle session d'onboarding pour un futur propriétaire
     * Note: L'utilisateur n'existe PAS encore - nous créons juste une session
     */
    public OnboardingSessionDto createOnboardingSession() {
        log.info("Creating new onboarding session for future owner");

        // Créer une session sans userId car l'utilisateur n'existe pas encore
        OnboardingSession session = new OnboardingSession();
        OnboardingSession saved = onboardingSessionRepository.save(session);

        log.info("Created new onboarding session: {}", saved.getId());
        return mapToDto(saved);
    }

    /**
     * Récupère une session d'onboarding par ID
     */
    public Optional<OnboardingSessionDto> getOnboardingSession(UUID sessionId) {
        log.debug("Fetching onboarding session: {}", sessionId);
        return onboardingSessionRepository.findById(sessionId)
            .map(this::mapToDto);
    }

    /**
     * Sauvegarde les informations du futur propriétaire (Étape 1)
     */
    @Transactional
    public OnboardingSessionDto saveOwnerInfo(UUID sessionId, OwnerInfoDto ownerInfo) {
        log.info("Saving owner info for session: {}", sessionId);

        OnboardingSession session = getSessionOrThrow(sessionId);
        validateSessionActive(session);

        // Vérifier que l'email n'existe pas déjà
        if (userRepository.existsByEmail(ownerInfo.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà. Veuillez vous connecter ou utiliser un autre email.");
        }

        try {
            String ownerInfoJson = objectMapper.writeValueAsString(ownerInfo);
            session.updateOwnerInfo(ownerInfoJson);

            OnboardingSession updated = onboardingSessionRepository.save(session);
            log.info("Owner info saved for session: {}", sessionId);

            return mapToDto(updated);
        } catch (JsonProcessingException e) {
            log.error("Error serializing owner info for session: {}", sessionId, e);
            throw new RuntimeException("Error saving owner information", e);
        }
    }

    /**
     * Sauvegarde les informations de l'organisation (Étape 2)
     */
    @Transactional
    public OnboardingSessionDto saveOrganizationInfo(UUID sessionId, OrganizationInfoDto organizationInfo) {
        log.info("Saving organization info for session: {}", sessionId);

        OnboardingSession session = getSessionOrThrow(sessionId);
        validateSessionActive(session);

        if (!session.canProceedToStep(OnboardingStep.ORGANIZATION_INFO)) {
            throw new IllegalStateException("Cannot proceed to organization info step. Complete previous steps first.");
        }

        try {
            String organizationInfoJson = objectMapper.writeValueAsString(organizationInfo);
            session.updateOrganizationInfo(organizationInfoJson);

            OnboardingSession updated = onboardingSessionRepository.save(session);
            log.info("Organization info saved for session: {}", sessionId);

            return mapToDto(updated);
        } catch (JsonProcessingException e) {
            log.error("Error serializing organization info for session: {}", sessionId, e);
            throw new RuntimeException("Error saving organization information", e);
        }
    }

    /**
     * Finalise le processus d'onboarding (Étape 3)
     * CRÉATION RÉELLE DE L'UTILISATEUR OWNER ET DE L'ORGANISATION
     */
    @Transactional
    public OnboardingCompletedDto completeOnboarding(UUID sessionId, SubscriptionInfoDto subscriptionInfo) {
        log.info("Completing onboarding for session: {}", sessionId);

        OnboardingSession session = getSessionOrThrow(sessionId);
        validateSessionActive(session);

        if (!session.canProceedToStep(OnboardingStep.SUBSCRIPTION_PAYMENT)) {
            throw new IllegalStateException("Cannot proceed to subscription step. Complete previous steps first.");
        }

        try {
            // Récupérer les données des étapes précédentes
            OwnerInfoDto ownerInfo = objectMapper.readValue(session.getOwnerInfoData(), OwnerInfoDto.class);
            OrganizationInfoDto organizationInfo = objectMapper.readValue(session.getOrganizationInfoData(), OrganizationInfoDto.class);

            // Vérifier encore une fois que l'email n'existe pas
            if (userRepository.existsByEmail(ownerInfo.getEmail())) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
            }

            // 1. CRÉER L'UTILISATEUR OWNER
            User owner = createOwnerUser(ownerInfo);
            log.info("Owner user created with ID: {}", owner.getId());

            // 2. CRÉER L'ORGANISATION (RÉELLEMENT MAINTENANT)
            OrganizationDto organization = createOrganizationFromOnboarding(organizationInfo, owner.getId());
            log.info("Organization created with ID: {}", organization.getId());

            // 3. LIER L'UTILISATEUR À L'ORGANISATION
            owner.setOrganizationId(organization.getId());
            userRepository.save(owner);

            // 4. CRÉER L'ABONNEMENT
            SubscriptionPlan plan = subscriptionService.getPlanById(subscriptionInfo.getSubscriptionPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan d'abonnement non trouvé"));

            OrganizationSubscription subscription = subscriptionService.createSubscription(
                organization.getId(),
                subscriptionInfo.getSubscriptionPlanId(),
                subscriptionInfo.getPaymentMethod(),
                subscriptionInfo.getPaymentReference(),
                plan.getPrice()
            );

            // 5. METTRE À JOUR LES LIMITES DE L'ORGANISATION SELON LE PLAN
            organizationService.updateOrganizationLimits(organization.getId(), plan);

            // 6. SAUVEGARDER LES INFOS DE SOUSCRIPTION
            String subscriptionInfoJson = objectMapper.writeValueAsString(subscriptionInfo);
            session.updateSubscriptionInfo(subscriptionInfoJson);

            // 7. MARQUER LA SESSION COMME TERMINÉE
            session.complete(organization.getId());
            session.setUserId(owner.getId()); // Maintenant on peut lier l'utilisateur
            onboardingSessionRepository.save(session);

            log.info("Onboarding completed successfully for session: {}", sessionId);

            // 8. TODO: Envoyer email de bienvenue avec mot de passe temporaire
            // sendWelcomeEmail(owner, temporaryPassword);

            return new OnboardingCompletedDto(
                organization.getId(),
                organization.getName(),
                subscription.getId(),
                plan.getName()
            );

        } catch (JsonProcessingException e) {
            log.error("Error processing onboarding completion for session: {}", sessionId, e);
            throw new RuntimeException("Error completing onboarding", e);
        }
    }

    /**
     * Récupère les informations du propriétaire pour une session
     */
    public Optional<OwnerInfoDto> getOwnerInfo(UUID sessionId) {
        log.debug("Fetching owner info for session: {}", sessionId);

        return onboardingSessionRepository.findById(sessionId)
            .filter(session -> session.getOwnerInfoData() != null)
            .map(session -> {
                try {
                    return objectMapper.readValue(session.getOwnerInfoData(), OwnerInfoDto.class);
                } catch (JsonProcessingException e) {
                    log.error("Error deserializing owner info for session: {}", sessionId, e);
                    return null;
                }
            });
    }

    /**
     * Récupère les informations de l'organisation pour une session
     */
    public Optional<OrganizationInfoDto> getOrganizationInfo(UUID sessionId) {
        log.debug("Fetching organization info for session: {}", sessionId);

        return onboardingSessionRepository.findById(sessionId)
            .filter(session -> session.getOrganizationInfoData() != null)
            .map(session -> {
                try {
                    return objectMapper.readValue(session.getOrganizationInfoData(), OrganizationInfoDto.class);
                } catch (JsonProcessingException e) {
                    log.error("Error deserializing organization info for session: {}", sessionId, e);
                    return null;
                }
            });
    }

    /**
     * Annule une session d'onboarding
     */
    @Transactional
    public void cancelOnboardingSession(UUID sessionId) {
        log.info("Cancelling onboarding session: {}", sessionId);

        OnboardingSession session = getSessionOrThrow(sessionId);
        session.setStatus(OnboardingStatus.CANCELLED);
        session.setUpdatedAt(LocalDateTime.now());

        onboardingSessionRepository.save(session);
        log.info("Onboarding session cancelled: {}", sessionId);
    }

    /**
     * Met à jour les sessions expirées
     */
    @Transactional
    public void updateExpiredSessions() {
        log.info("Updating expired onboarding sessions");

        List<OnboardingSession> expiredSessions = onboardingSessionRepository.findExpiredSessions(LocalDateTime.now());

        for (OnboardingSession session : expiredSessions) {
            session.expire();
            onboardingSessionRepository.save(session);
        }

        log.info("Updated {} expired onboarding sessions", expiredSessions.size());
    }

    /**
     * Récupère les statistiques d'onboarding
     */
    public OnboardingStatsDto getOnboardingStats() {
        log.debug("Fetching onboarding statistics");

        Long totalSessions = onboardingSessionRepository.count();
        Long completedSessions = onboardingSessionRepository.countByStatus(OnboardingStatus.COMPLETED);
        Long inProgressSessions = onboardingSessionRepository.countByStatus(OnboardingStatus.IN_PROGRESS);
        Long expiredSessions = onboardingSessionRepository.countByStatus(OnboardingStatus.EXPIRED);
        Long cancelledSessions = onboardingSessionRepository.countByStatus(OnboardingStatus.CANCELLED);

        double completionRate = totalSessions > 0 ? (double) completedSessions / totalSessions * 100 : 0.0;

        return OnboardingStatsDto.builder()
            .totalSessions(totalSessions)
            .completedSessions(completedSessions)
            .inProgressSessions(inProgressSessions)
            .expiredSessions(expiredSessions)
            .cancelledSessions(cancelledSessions)
            .completionRate(completionRate)
            .build();
    }

    /**
     * Trouve les sessions inactives depuis longtemps
     */
    public List<OnboardingSession> findStaleSessions(int hoursThreshold) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursThreshold);
        return onboardingSessionRepository.findStaleInProgressSessions(cutoffTime);
    }

    // ==================== MÉTHODES PRIVÉES ====================

    /**
     * Crée l'utilisateur propriétaire à partir des informations d'onboarding
     */
    private User createOwnerUser(OwnerInfoDto ownerInfo) {
        log.info("Creating owner user for email: {}", ownerInfo.getEmail());

        // Générer un mot de passe temporaire sécurisé
        String temporaryPassword = generateSecureTemporaryPassword();

        User owner = new User(
            ownerInfo.getEmail(),
            passwordEncoder.encode(temporaryPassword),
            ownerInfo.getFirstName(),
            ownerInfo.getLastName(),
            UserType.OWNER
        );

        owner.setPhone(ownerInfo.getPhone());
        owner.setAddress(ownerInfo.getAddress());
        owner.setCity(ownerInfo.getCity());
        owner.setCountry(ownerInfo.getCountry());
        owner.setStatus(UserStatus.ACTIVE); // Directement actif pour les propriétaires après onboarding
        owner.setEmailVerified(false); // Sera vérifié par email
        owner.setPhoneVerified(false);

        User savedOwner = userRepository.save(owner);

        // TODO: Envoyer email avec mot de passe temporaire
        log.info("Temporary password for {}: {} (TODO: Send via email)", ownerInfo.getEmail(), temporaryPassword);

        return savedOwner;
    }

    /**
     * NOUVEAU: Crée l'organisation à partir des informations d'onboarding
     */
    private OrganizationDto createOrganizationFromOnboarding(OrganizationInfoDto organizationInfo, UUID ownerId) {
        log.info("Creating organization: {} for owner: {}", organizationInfo.getOrganizationName(), ownerId);

        // Mapper les données d'onboarding vers le DTO de création d'organisation
        CreateOrganizationDto createOrgDto = new CreateOrganizationDto();
        createOrgDto.setName(organizationInfo.getOrganizationName());
        createOrgDto.setOrganizationType(organizationInfo.getOrganizationType());
        createOrgDto.setDescription(organizationInfo.getDescription());
        createOrgDto.setRegistrationNumber(organizationInfo.getRegistrationNumber());
        createOrgDto.setTaxNumber(organizationInfo.getTaxNumber());
        createOrgDto.setAddress(organizationInfo.getAddress());
        createOrgDto.setCity(organizationInfo.getCity());
        createOrgDto.setCountry(organizationInfo.getCountry());

        // Mapper les politiques
        if (organizationInfo.getPolicies() != null) {
            createOrgDto.setPolicies(mapOnboardingPolicyToOrganizationPolicy(organizationInfo.getPolicies()));
        }

        // Paramètres par défaut
        OrganizationSettingsDto defaultSettings = new OrganizationSettingsDto();
        createOrgDto.setSettings(defaultSettings);

        // Créer l'organisation via le service
        return organizationService.createOrganization(createOrgDto, ownerId);
    }

    /**
     * Mappe les politiques d'onboarding vers les politiques d'organisation
     */
    private OrganizationPolicyDto mapOnboardingPolicyToOrganizationPolicy(OrganizationPolicyDto onboardingPolicy) {
        OrganizationPolicyDto orgPolicy = new OrganizationPolicyDto();

        // Copier toutes les propriétés (les DTOs sont identiques)
        orgPolicy.setWithDriverOption(onboardingPolicy.getWithDriverOption());
        orgPolicy.setWithoutDriverOption(onboardingPolicy.getWithoutDriverOption());
        orgPolicy.setDriverMandatory(onboardingPolicy.getDriverMandatory());
        orgPolicy.setMinRentalHours(onboardingPolicy.getMinRentalHours());
        orgPolicy.setMaxRentalDays(onboardingPolicy.getMaxRentalDays());
        orgPolicy.setSecurityDeposit(onboardingPolicy.getSecurityDeposit());
        orgPolicy.setLateReturnPenalty(onboardingPolicy.getLateReturnPenalty());
        orgPolicy.setMinDriverAge(onboardingPolicy.getMinDriverAge());
        orgPolicy.setMaxDriverAge(onboardingPolicy.getMaxDriverAge());
        orgPolicy.setRequireDriverLicense(onboardingPolicy.getRequireDriverLicense());
        orgPolicy.setRequireCreditCard(onboardingPolicy.getRequireCreditCard());
        orgPolicy.setAllowWeekendRental(onboardingPolicy.getAllowWeekendRental());
        orgPolicy.setAllowHolidayRental(onboardingPolicy.getAllowHolidayRental());
        orgPolicy.setCancellationPolicy(onboardingPolicy.getCancellationPolicy());
        orgPolicy.setFreeCancellationHours(onboardingPolicy.getFreeCancellationHours());
        orgPolicy.setCancellationFeePercentage(onboardingPolicy.getCancellationFeePercentage());
        orgPolicy.setRefundPolicy(onboardingPolicy.getRefundPolicy());
        orgPolicy.setRefundProcessingDays(onboardingPolicy.getRefundProcessingDays());

        return orgPolicy;
    }

    /**
     * Génère un mot de passe temporaire sécurisé
     */
    private String generateSecureTemporaryPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%&*";
        String allChars = upperCase + lowerCase + digits + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);

        // Garantir au moins un caractère de chaque type
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Compléter avec des caractères aléatoires
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Mélanger les caractères
        for (int i = 0; i < password.length(); i++) {
            int randomIndex = random.nextInt(password.length());
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(randomIndex));
            password.setCharAt(randomIndex, temp);
        }

        return password.toString();
    }

    /**
     * Récupère une session ou lance une exception
     */
    private OnboardingSession getSessionOrThrow(UUID sessionId) {
        return onboardingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Onboarding session not found: " + sessionId));
    }

    /**
     * Valide qu'une session est active et utilisable
     */
    private void validateSessionActive(OnboardingSession session) {
        if (session.isExpired()) {
            throw new IllegalStateException("Onboarding session has expired");
        }

        if (session.isCompleted()) {
            throw new IllegalStateException("Onboarding session is already completed");
        }

        if (session.getStatus() != OnboardingStatus.IN_PROGRESS) {
            throw new IllegalStateException("Onboarding session is not in progress");
        }
    }

    /**
     * Convertit une session en DTO
     */
    private OnboardingSessionDto mapToDto(OnboardingSession session) {
        OnboardingSessionDto dto = new OnboardingSessionDto();
        dto.setId(session.getId());
        dto.setUserId(session.getUserId()); // Peut être null au début
        dto.setCurrentStep(session.getCurrentStep());
        dto.setStatus(session.getStatus());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setExpiresAt(session.getExpiresAt());
        dto.setCompletionPercentage(session.getCompletionPercentage());

        // Indiquer quelles étapes sont complétées
        dto.setOwnerInfoCompleted(session.getOwnerInfoData() != null);
        dto.setOrganizationInfoCompleted(session.getOrganizationInfoData() != null);
        dto.setSubscriptionInfoCompleted(session.getSubscriptionInfoData() != null);

        dto.setCreatedOrganizationId(session.getCreatedOrganizationId());
        dto.setCompletedAt(session.getCompletedAt());

        return dto;
    }
}
