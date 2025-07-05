package inc.yowyob.rental_api.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * DTO pour les paramètres d'organisation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationSettingsDto {

    // Paramètres généraux
    @NotNull(message = "Timezone is required")
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone = "UTC";

    @NotNull(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency = "EUR";

    @NotNull(message = "Language is required")
    @Size(min = 2, max = 5, message = "Language must be between 2 and 5 characters")
    private String language = "fr";

    @NotNull(message = "Date format is required")
    @Size(max = 20, message = "Date format must not exceed 20 characters")
    private String dateFormat = "dd/MM/yyyy";

    // Notifications
    @NotNull(message = "Email notifications setting is required")
    private Boolean emailNotifications = true;

    @NotNull(message = "SMS notifications setting is required")
    private Boolean smsNotifications = false;

    @NotNull(message = "Push notifications setting is required")
    private Boolean pushNotifications = true;

    // Fonctionnalités (basées sur l'abonnement)
    private Boolean enableGeofencing = false;
    private Boolean enableChat = false;
    private Boolean enableAdvancedReports = false;
    private Boolean enableApiAccess = false;

    // Sécurité
    @NotNull(message = "Require two factor auth setting is required")
    private Boolean requireTwoFactorAuth = false;

    @NotNull(message = "Password expiration days is required")
    @Min(value = 30, message = "Password expiration days must be at least 30")
    @Max(value = 365, message = "Password expiration days cannot exceed 365")
    private Integer passwordExpirationDays = 90;

    @NotNull(message = "Audit logging setting is required")
    private Boolean auditLogging = true;

    // Intégrations
    @NotNull(message = "Enable mobile money payments setting is required")
    private Boolean enableMobileMoneyPayments = true;

    @NotNull(message = "Enable card payments setting is required")
    private Boolean enableCardPayments = true;

    @NotNull(message = "Enable bank transfers setting is required")
    private Boolean enableBankTransfers = false;

    // Validation personnalisée
    @AssertTrue(message = "At least one payment method must be enabled")
    private boolean isValidPaymentMethods() {
        return Boolean.TRUE.equals(enableMobileMoneyPayments) ||
            Boolean.TRUE.equals(enableCardPayments) ||
            Boolean.TRUE.equals(enableBankTransfers);
    }
}
