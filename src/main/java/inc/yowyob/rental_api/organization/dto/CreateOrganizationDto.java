package inc.yowyob.rental_api.organization.dto;

import inc.yowyob.rental_api.core.enums.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO pour la création d'une organisation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationDto {

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Organization type is required")
    private OrganizationType organizationType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    // Informations légales
    private String registrationNumber;
    private String taxNumber;
    private String businessLicense;

    // Adresse
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    private String postalCode;
    private String region;

    // Contact
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    private String phone;

    @Email(message = "Email should be valid")
    private String email;

    private String website;

    // Politique de l'organisation
    private OrganizationPolicyDto policies;

    // Configuration
    private OrganizationSettingsDto settings;

    // Métadonnées
    private String logoUrl;
    private String primaryColor = "#007bff";
    private String secondaryColor = "#6c757d";
}
