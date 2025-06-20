package inc.yowyob.rental_api.onboarding.dto;

import inc.yowyob.rental_api.core.enums.OnboardingStep;
import inc.yowyob.rental_api.core.enums.OnboardingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour les sessions d'onboarding
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingSessionDto {

    private UUID id;
    private UUID userId;
    private OnboardingStep currentStep;
    private OnboardingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private double completionPercentage;

    // Données des étapes (masquées pour la sécurité)
    private Boolean ownerInfoCompleted;
    private Boolean organizationInfoCompleted;
    private Boolean subscriptionInfoCompleted;

    private UUID createdOrganizationId;
    private LocalDateTime completedAt;
}
