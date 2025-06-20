package inc.yowyob.rental_api.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * DTO pour la création d'une session d'onboarding
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOnboardingSessionDto {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String referralCode;
    private String campaignSource;
}
