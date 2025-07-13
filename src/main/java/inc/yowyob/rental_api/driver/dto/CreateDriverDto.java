package inc.yowyob.rental_api.driver.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

import inc.yowyob.rental_api.utilities.Money;
import inc.yowyob.rental_api.utilities.WorkingHours;

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

    // @NotNull(message = "L'ID de l'agence est requis.")
    private UUID agencyId;

    @NotNull(message = "La date de naissance est requise.")
    private LocalDate dateOfBirth;
 
    @NotBlank(message = "Le numéro de permis est requis.")
    private String licenseNumber;

    @NotBlank(message = "Le type de permis est requis.")
    private String licenseType;

    
    @NotNull @Future private LocalDate licenseExpiry;
    @NotNull @Min(0) private Integer experience;
    
    /**
     * URL vers la photo de la carte d'identité (optionnel à la création).
     */
    private String idCardUrl;

    /**
     * URL vers la photo du permis de conduire (optionnel à la création).
     */

    private String driverLicenseUrl;
     
    
    // Staff info
    @NotBlank private String registrationId;
    private String cni;      // La photo de profile du driver

    @NotBlank private String position;
    private String department;
    @NotNull private String staffStatus;
    private Money hourlyRate;
    private WorkingHours workingHours;
    @NotNull private LocalDate hireDate;

}