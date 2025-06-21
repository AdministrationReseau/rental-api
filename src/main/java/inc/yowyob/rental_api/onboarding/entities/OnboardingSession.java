package inc.yowyob.rental_api.onboarding.entities;

import inc.yowyob.rental_api.core.enums.OnboardingStatus;
import inc.yowyob.rental_api.core.enums.OnboardingStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Session d'onboarding pour suivre le processus d'inscription d'un futur propriétaire
 */
@Data
@AllArgsConstructor
@Table("onboarding_sessions")
public class OnboardingSession {

    @Id
    @PrimaryKey
    private UUID id;

    private UUID userId;

    @NotNull(message = "Current step is required")
    private OnboardingStep currentStep;

    @NotNull(message = "Status is required")
    private OnboardingStatus status;

    // JSON string contenant les données de chaque étape
    private String ownerInfoData;
    private String organizationInfoData;
    private String subscriptionInfoData;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;

    // Informations de completion
    private LocalDateTime completedAt;
    private UUID createdOrganizationId;

    /**
     * Constructeur par défaut pour une nouvelle session
     */
    public OnboardingSession(boolean initializeDefaults) {
        if (initializeDefaults) {
            this.id = UUID.randomUUID();
            this.userId = null; // Pas d'utilisateur au début
            this.currentStep = OnboardingStep.OWNER_INFO;
            this.status = OnboardingStatus.IN_PROGRESS;
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.expiresAt = LocalDateTime.now().plusHours(24); // Session expire après 24h
        }
    }

    /**
     * Constructeur par défaut qui initialise les valeurs
     */
    public OnboardingSession() {
        this(true);
    }

    // ==================== MÉTHODES MÉTIER ====================

    /**
     * Vérifie si la session a expiré
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Vérifie si la session est terminée
     */
    public boolean isCompleted() {
        return status == OnboardingStatus.COMPLETED;
    }

    /**
     * Vérifie si on peut procéder à une étape donnée
     */
    public boolean canProceedToStep(OnboardingStep step) {
        return switch (step) {
            case OWNER_INFO -> true; // Toujours possible
            case ORGANIZATION_INFO -> currentStep.ordinal() >= OnboardingStep.OWNER_INFO.ordinal()
                && ownerInfoData != null && !ownerInfoData.trim().isEmpty();
            case SUBSCRIPTION_PAYMENT -> currentStep.ordinal() >= OnboardingStep.ORGANIZATION_INFO.ordinal()
                && organizationInfoData != null && !organizationInfoData.trim().isEmpty();
        };
    }

    /**
     * Passe à l'étape suivante
     */
    public void moveToNextStep() {
        OnboardingStep[] steps = OnboardingStep.values();
        int currentIndex = currentStep.ordinal();
        if (currentIndex < steps.length - 1) {
            this.currentStep = steps[currentIndex + 1];
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Met à jour les informations du propriétaire et passe à l'étape suivante
     */
    public void updateOwnerInfo(String ownerInfoJson) {
        this.ownerInfoData = ownerInfoJson;
        this.updatedAt = LocalDateTime.now();

        // Passer à l'étape suivante si on est encore à la première étape
        if (currentStep == OnboardingStep.OWNER_INFO) {
            moveToNextStep();
        }
    }

    /**
     * Met à jour les informations de l'organisation et passe à l'étape suivante
     */
    public void updateOrganizationInfo(String organizationInfoJson) {
        this.organizationInfoData = organizationInfoJson;
        this.updatedAt = LocalDateTime.now();

        // Passer à l'étape suivante si on est à l'étape organisation
        if (currentStep == OnboardingStep.ORGANIZATION_INFO) {
            moveToNextStep();
        }
    }

    /**
     * Met à jour les informations de souscription
     */
    public void updateSubscriptionInfo(String subscriptionInfoJson) {
        this.subscriptionInfoData = subscriptionInfoJson;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marque la session comme terminée avec succès
     */
    public void complete(UUID organizationId) {
        this.status = OnboardingStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.createdOrganizationId = organizationId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marque la session comme expirée
     */
    public void expire() {
        this.status = OnboardingStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marque la session comme annulée
     */
    public void cancel() {
        this.status = OnboardingStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marque la session comme échouée
     */
    public void fail() {
        this.status = OnboardingStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calcule le pourcentage de completion
     */
    public double getCompletionPercentage() {
        if (status == OnboardingStatus.COMPLETED) {
            return 100.0;
        }

        int totalSteps = OnboardingStep.values().length;
        int completedSteps = 0;

        // Compter les étapes complétées
        if (ownerInfoData != null && !ownerInfoData.trim().isEmpty()) {
            completedSteps++;
        }
        if (organizationInfoData != null && !organizationInfoData.trim().isEmpty()) {
            completedSteps++;
        }
        if (subscriptionInfoData != null && !subscriptionInfoData.trim().isEmpty()) {
            completedSteps++;
        }

        return (double) completedSteps / totalSteps * 100.0;
    }

    /**
     * Vérifie si la session peut être modifiée
     */
    public boolean canBeModified() {
        return status == OnboardingStatus.IN_PROGRESS && !isExpired();
    }

    /**
     * Étend la durée de vie de la session
     */
    public void extendExpiry(int additionalHours) {
        if (canBeModified()) {
            this.expiresAt = this.expiresAt.plusHours(additionalHours);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Réinitialise la session à l'état initial
     */
    public void reset() {
        this.currentStep = OnboardingStep.OWNER_INFO;
        this.status = OnboardingStatus.IN_PROGRESS;
        this.ownerInfoData = null;
        this.organizationInfoData = null;
        this.subscriptionInfoData = null;
        this.completedAt = null;
        this.createdOrganizationId = null;
        this.updatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }

    /**
     * Obtient un résumé du statut de la session
     */
    public String getStatusSummary() {
        if (isCompleted()) {
            return "Session terminée avec succès";
        }
        if (isExpired()) {
            return "Session expirée";
        }
        if (status == OnboardingStatus.CANCELLED) {
            return "Session annulée";
        }
        if (status == OnboardingStatus.FAILED) {
            return "Session échouée";
        }

        return String.format("En cours - Étape %d/%d (%.0f%%)",
            currentStep.ordinal() + 1,
            OnboardingStep.values().length,
            getCompletionPercentage());
    }
}
