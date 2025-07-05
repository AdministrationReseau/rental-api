package inc.yowyob.rental_api.organization.entities;

import inc.yowyob.rental_api.core.enums.OrganizationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant une organisation dans le système
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table("organizations")
public class Organization {

    @Id
    @PrimaryKey
    private UUID id;

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Organization type is required")
    private OrganizationType organizationType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Owner ID is required")
    private UUID ownerId;

    // Informations légales
    private String registrationNumber;
    private String taxNumber;
    private String businessLicense;

    // Adresse
    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country is required")
    private String country;

    private String postalCode;
    private String region;

    // Contact
    private String phone;
    private String email;
    private String website;

    // Politique de l'organisation
    private OrganizationPolicy policies;

    // Configuration
    private OrganizationSettings settings;

    // Statut
    private Boolean isActive = true;
    private Boolean isVerified = false;

    // Limites basées sur l'abonnement
    private Integer maxAgencies = 1;
    private Integer maxVehicles = 5;
    private Integer maxDrivers = 2;
    private Integer maxUsers = 10;

    // Utilisation actuelle
    private Integer currentAgencies = 0;
    private Integer currentVehicles = 0;
    private Integer currentDrivers = 0;
    private Integer currentUsers = 1; // Le propriétaire compte

    // Métadonnées
    private String logoUrl;
    private String primaryColor = "#007bff";
    private String secondaryColor = "#6c757d";

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // Constructors
    public Organization(String name, OrganizationType organizationType, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.organizationType = organizationType;
        this.ownerId = ownerId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Méthodes métier
    public boolean canCreateAgency() {
        return this.currentAgencies < this.maxAgencies;
    }

    public boolean canCreateVehicle() {
        return this.currentVehicles < this.maxVehicles;
    }

    public boolean canCreateDriver() {
        return this.currentDrivers < this.maxDrivers;
    }

    public boolean canCreateUser() {
        return this.currentUsers < this.maxUsers;
    }

    public void incrementAgencyCount() {
        this.currentAgencies++;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrementAgencyCount() {
        if (this.currentAgencies > 0) {
            this.currentAgencies--;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void incrementVehicleCount() {
        this.currentVehicles++;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrementVehicleCount() {
        if (this.currentVehicles > 0) {
            this.currentVehicles--;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void incrementDriverCount() {
        this.currentDrivers++;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrementDriverCount() {
        if (this.currentDrivers > 0) {
            this.currentDrivers--;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void incrementUserCount() {
        this.currentUsers++;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrementUserCount() {
        if (this.currentUsers > 1) { // Au moins le propriétaire
            this.currentUsers--;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void updateLimits(Integer maxAgencies, Integer maxVehicles, Integer maxDrivers, Integer maxUsers) {
        this.maxAgencies = maxAgencies != null ? maxAgencies : this.maxAgencies;
        this.maxVehicles = maxVehicles != null ? maxVehicles : this.maxVehicles;
        this.maxDrivers = maxDrivers != null ? maxDrivers : this.maxDrivers;
        this.maxUsers = maxUsers != null ? maxUsers : this.maxUsers;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOwner(UUID userId) {
        return this.ownerId.equals(userId);
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void verify() {
        this.isVerified = true;
        this.updatedAt = LocalDateTime.now();
    }

    public double getAgencyUsagePercentage() {
        return this.maxAgencies > 0 ? (double) this.currentAgencies / this.maxAgencies * 100 : 0;
    }

    public double getVehicleUsagePercentage() {
        return this.maxVehicles > 0 ? (double) this.currentVehicles / this.maxVehicles * 100 : 0;
    }

    public double getDriverUsagePercentage() {
        return this.maxDrivers > 0 ? (double) this.currentDrivers / this.maxDrivers * 100 : 0;
    }

    public double getUserUsagePercentage() {
        return this.maxUsers > 0 ? (double) this.currentUsers / this.maxUsers * 100 : 0;
    }

    /**
     * Politique de l'organisation (embedded)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @UserDefinedType("organization_policy")
    public static class OrganizationPolicy {

        // Politiques de location
        private Boolean withDriverOption = true;
        private Boolean withoutDriverOption = true;
        private Boolean driverMandatory = false;

        // Paramètres de location
        private Integer minRentalHours = 1;
        private Integer maxRentalDays = 30;
        private BigDecimal securityDeposit = BigDecimal.ZERO;
        private BigDecimal lateReturnPenalty = BigDecimal.ZERO;

        // Politiques client
        private Integer minDriverAge = 21;
        private Integer maxDriverAge = 75;
        private Boolean requireDriverLicense = true;
        private Boolean requireCreditCard = false;

        // Disponibilités
        private Boolean allowWeekendRental = true;
        private Boolean allowHolidayRental = true;

        // Politiques d'annulation
        private String cancellationPolicy = "Annulation gratuite jusqu'à 24h avant la location";
        private Integer freeCancellationHours = 24;
        private BigDecimal cancellationFeePercentage = BigDecimal.ZERO;

        // Politiques de remboursement
        private String refundPolicy = "Remboursement intégral selon les conditions d'annulation";
        private Integer refundProcessingDays = 7;
    }

    /**
     * Paramètres de l'organisation (embedded)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @UserDefinedType("organization_settings")
    public static class OrganizationSettings {

        // Paramètres généraux
        private String timezone = "UTC";
        private String currency = "EUR";
        private String language = "fr";
        private String dateFormat = "dd/MM/yyyy";

        // Notifications
        private Boolean emailNotifications = true;
        private Boolean smsNotifications = false;
        private Boolean pushNotifications = true;

        // Fonctionnalités
        private Boolean enableGeofencing = false;
        private Boolean enableChat = false;
        private Boolean enableAdvancedReports = false;
        private Boolean enableApiAccess = false;

        // Sécurité
        private Boolean requireTwoFactorAuth = false;
        private Integer passwordExpirationDays = 90;
        private Boolean auditLogging = true;

        // Intégrations
        private Boolean enableMobileMoneyPayments = true;
        private Boolean enableCardPayments = true;
        private Boolean enableBankTransfers = false;
    }
}
