package inc.yowyob.rental_api.onboarding.dto;

import inc.yowyob.rental_api.core.enums.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * DTO pour les informations de l'organisation (Étape 2)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationInfoDto {

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String organizationName;

    @NotNull(message = "Organization type is required")
    private OrganizationType organizationType;

    private String registrationNumber;
    private String taxNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Organization policies are required")
    private OrganizationPolicyDto policies;
}

