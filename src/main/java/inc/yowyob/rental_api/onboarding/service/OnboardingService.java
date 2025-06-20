package inc.yowyob.rental_api.onboarding.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import inc.yowyob.rental_api.core.enums.OnboardingStatus;
import inc.yowyob.rental_api.core.enums.OnboardingStep;
import inc.yowyob.rental_api.onboarding.dto.*;
import inc.yowyob.rental_api.onboarding.entities.OnboardingSession;
import inc.yowyob.rental_api.onboarding.repository.OnboardingSessionRepository;
import inc.yowyob.rental_api.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ObjectMapper objectMapper;

    /**
     * Crée une nouvelle session d'onboarding
     */
    public OnboardingSessionDto createOnboardingSession(UUID userId) {
        log.info("Creating onboarding session for user: {}", userId);

        // Vérifier s'il y a déjà une session active
        Optional<OnboardingSession> existingSession = onboardingSessionRepository.findActiveByUserId(userId);
        if (existingSession.isPresent()) {
            log.info("Found existing active onboarding session for user: {}", userId);
            return mapToDto(existingSession.get());
        }

        // Créer une nouvelle session
        OnboardingSession session = new OnboardingSession(userId);
        OnboardingSession saved = onboardingSessionRepository.save(session);

        log.info("Created new onboarding session: {} for user: {}", saved.getId(), userId);
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
     * Récupère la session active d'un utilisateur
     */
    public Optional<OnboardingSessionDto> getActiveSessionByUserId(UUID userId) {
        log.debug("Fetching active onboarding session for user: {}", userId);
        return onboardingSessionRepository.findActiveByUserId(userId)
            .map(this::mapToDto);
    }

    /**
     * Sauvegarde les informations du propriétaire (Étape 1)
     */
    @Transactional
    public OnboardingSessionDto saveOwnerInfo(UUID sessionId, OwnerInfoDto ownerInfo) {
        log.info("Saving owner info for session: {}", sessionId);

        OnboardingSession session = getSessionOrThrow(sessionId);
        validateSessionActive(session);

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
            // Sauvegarder les informations de souscription
            String subscriptionInfoJson = objectMapper.writeValueAsString(subscriptionInfo);
            session.updateSubscriptionInfo(subscriptionInfoJson);

            // Récupérer les données des étapes précédentes
            OwnerInfoDto ownerInfo = objectMapper.readValue(session.getOwnerInfoData(), OwnerInfoDto.class);
            OrganizationInfoDto organizationInfo = objectMapper.readValue(session.getOrganizationInfoData(), OrganizationInfoDto.class);

            // TODO: Créer l'utilisateur, l'organisation et traiter le paiement
            // Pour l'instant, nous simulons la création
            UUID organizationId = UUID.randomUUID();
            String organizationName = organizationInfo.getOrganizationName();

            // Créer la souscription
            UUID subscriptionId = UUID.randomUUID(); // Simulé pour l'instant
            String subscriptionPlan = "GRATUIT"; // Par défaut

            // Marquer la session comme terminée
            session.complete(organizationId);
            onboardingSessionRepository.save(session);

            log.info("Onboarding completed successfully for session: {}", sessionId);

            return new OnboardingCompletedDto(organizationId, organizationName, subscriptionId, subscriptionPlan);

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

    // Méthodes utilitaires privées

    private OnboardingSession getSessionOrThrow(UUID sessionId) {
        return onboardingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Onboarding session not found: " + sessionId));
    }

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

    private OnboardingSessionDto mapToDto(OnboardingSession session) {
        OnboardingSessionDto dto = new OnboardingSessionDto();
        dto.setId(session.getId());
        dto.setUserId(session.getUserId());
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
