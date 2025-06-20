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
 * Session d'onboarding pour suivre le processus d'inscription
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("onboarding_sessions")
public class OnboardingSession {

    @Id
    @PrimaryKey
    private UUID id;

    @NotNull(message = "User ID is required")
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

    // Constructors
    public OnboardingSession(UUID userId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.currentStep = OnboardingStep.OWNER_INFO;
        this.status = OnboardingStatus.IN_PROGRESS;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24); // Session expire après 24h
    }

    // Business methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isCompleted() {
        return status == OnboardingStatus.COMPLETED;
    }

    public boolean canProceedToStep(OnboardingStep step) {
        return switch (step) {
            case OWNER_INFO -> true;
            case ORGANIZATION_INFO -> currentStep.ordinal() >= OnboardingStep.OWNER_INFO.ordinal()
                && ownerInfoData != null;
            case SUBSCRIPTION_PAYMENT -> currentStep.ordinal() >= OnboardingStep.ORGANIZATION_INFO.ordinal()
                && organizationInfoData != null;
        };
    }

    public void moveToNextStep() {
        OnboardingStep[] steps = OnboardingStep.values();
        int currentIndex = currentStep.ordinal();
        if (currentIndex < steps.length - 1) {
            this.currentStep = steps[currentIndex + 1];
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void updateOwnerInfo(String ownerInfoJson) {
        this.ownerInfoData = ownerInfoJson;
        this.updatedAt = LocalDateTime.now();
        if (currentStep == OnboardingStep.OWNER_INFO) {
            moveToNextStep();
        }
    }

    public void updateOrganizationInfo(String organizationInfoJson) {
        this.organizationInfoData = organizationInfoJson;
        this.updatedAt = LocalDateTime.now();
        if (currentStep == OnboardingStep.ORGANIZATION_INFO) {
            moveToNextStep();
        }
    }

    public void updateSubscriptionInfo(String subscriptionInfoJson) {
        this.subscriptionInfoData = subscriptionInfoJson;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete(UUID organizationId) {
        this.status = OnboardingStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.createdOrganizationId = organizationId;
        this.updatedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = OnboardingStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    public double getCompletionPercentage() {
        int totalSteps = OnboardingStep.values().length;
        int completedSteps = currentStep.ordinal() + 1;

        if (status == OnboardingStatus.COMPLETED) {
            return 100.0;
        }

        return (double) completedSteps / totalSteps * 100.0;
    }
}
