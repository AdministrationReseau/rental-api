package inc.yowyob.rental_api.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour l'onboarding terminé
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingCompletedDto {

    private UUID organizationId;
    private String organizationName;
    private UUID subscriptionId;
    private String subscriptionPlan;
    private LocalDateTime activatedAt;
    private String accessToken;
    private String refreshToken;
    private String message;

    public OnboardingCompletedDto(UUID organizationId, String organizationName,
                                  UUID subscriptionId, String subscriptionPlan) {
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.subscriptionId = subscriptionId;
        this.subscriptionPlan = subscriptionPlan;
        this.activatedAt = LocalDateTime.now();
        this.message = "Félicitations ! Votre organisation a été créée avec succès.";
    }
}
