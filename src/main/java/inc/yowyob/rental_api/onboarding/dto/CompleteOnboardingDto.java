package inc.yowyob.rental_api.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * DTO pour la finalisation de l'onboarding
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteOnboardingDto {

    @NotNull(message = "Session ID is required")
    private UUID sessionId;

    @NotNull(message = "Subscription info is required")
    private SubscriptionInfoDto subscriptionInfo;
}
