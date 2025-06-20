package inc.yowyob.rental_api.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * DTO pour les informations de souscription (Étape 3)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionInfoDto {

    @NotNull(message = "Subscription plan ID is required")
    private UUID subscriptionPlanId;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // MOMO, ORANGE_MONEY, VISA, etc.

    private String paymentToken;
    private String paymentReference;

    @NotNull(message = "Accept terms is required")
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean acceptTerms = false;

    @NotNull(message = "Accept privacy policy is required")
    @AssertTrue(message = "You must accept the privacy policy")
    private Boolean acceptPrivacyPolicy = false;

    private Boolean optInMarketing = false;
}
