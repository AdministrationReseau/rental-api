package inc.yowyob.rental_api.driver.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO pour la création d'un nouveau profil de chauffeur.
 * Ce DTO lie un utilisateur existant à un nouveau profil de chauffeur
 * au sein d'une organisation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriverDto {

    @NotNull(message = "L'ID de l'utilisateur est requis.")
    private UUID userId;

    @NotNull(message = "L'ID de l'organisation est requis.")
    private UUID organizationId;

    @NotNull(message = "L'âge est requis.")
    @Min(value = 18, message = "Le chauffeur doit avoir au moins 18 ans.")
    private Integer age;

    @NotBlank(message = "Le numéro de permis est requis.")
    private String licenseNumber;

    @NotBlank(message = "Le type de permis est requis.")
    private String licenseType;

    /**
     * URL vers la photo de la carte d'identité (optionnel à la création).
     */
    private String idCardUrl;

    /**
     * URL vers la photo du permis de conduire (optionnel à la création).
     */
    private String driverLicenseUrl;
}