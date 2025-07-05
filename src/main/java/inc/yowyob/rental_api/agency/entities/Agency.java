package inc.yowyob.rental_api.agency.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entité représentant une agence de location
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table("agencies")
public class Agency {

    @Id
    @PrimaryKey
    private UUID id;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @NotBlank(message = "Agency name is required")
    @Size(min = 2, max = 100, message = "Agency name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

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
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    private String phone;

    @Email(message = "Email should be valid")
    private String email;

    // Géolocalisation
    private GeoLocation geoLocation;

    // Gestionnaire de l'agence
    private UUID managerId;

    // Horaires d'ouverture
    private Map<String, WorkingHours> workingHours;

    // Statut
    private Boolean isActive = true;
    private Boolean is24Hours = false;

    // Statistiques
    private Integer currentVehicles = 0;
    private Integer currentDrivers = 0;
    private Integer currentStaff = 0;

    // Zone de géofencing (si activée)
    private String geofenceZoneId;
    private Double geofenceRadius; // en mètres

    // Configuration
    private AgencySettings settings;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // Constructors
    public Agency(String name, UUID organizationId, String address, String city, String country) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.organizationId = organizationId;
        this.address = address;
        this.city = city;
        this.country = country;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Méthodes métier
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

    public void incrementStaffCount() {
        this.currentStaff++;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrementStaffCount() {
        if (this.currentStaff > 0) {
            this.currentStaff--;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void setManager(UUID managerId) {
        this.managerId = managerId;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isManager(UUID userId) {
        return this.managerId != null && this.managerId.equals(userId);
    }

    public boolean isOpenNow() {
        if (Boolean.TRUE.equals(this.is24Hours)) {
            return true;
        }

        if (workingHours == null) {
            return false;
        }

        String dayOfWeek = LocalDateTime.now().getDayOfWeek().name().toLowerCase();
        WorkingHours todayHours = workingHours.get(dayOfWeek);

        if (todayHours == null || !Boolean.TRUE.equals(todayHours.getIsOpen())) {
            return false;
        }

        LocalTime now = LocalTime.now();
        return !now.isBefore(todayHours.getOpenTime()) && !now.isAfter(todayHours.getCloseTime());
    }

    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        fullAddress.append(this.address);
        if (this.city != null) {
            fullAddress.append(", ").append(this.city);
        }
        if (this.postalCode != null) {
            fullAddress.append(" ").append(this.postalCode);
        }
        if (this.country != null) {
            fullAddress.append(", ").append(this.country);
        }
        return fullAddress.toString();
    }

    public double getDistanceFrom(double latitude, double longitude) {
        if (this.geoLocation == null) {
            return Double.MAX_VALUE;
        }
        return calculateDistance(latitude, longitude,
            this.geoLocation.getLatitude(),
            this.geoLocation.getLongitude());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Formule de Haversine pour calculer la distance entre deux points GPS
        final int EARTH_RADIUS = 6371; // Rayon de la Terre en kilomètres

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Distance en kilomètres
    }

    /**
     * Géolocalisation (embedded)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @UserDefinedType("geo_location")
    public static class GeoLocation {
        private Double latitude;
        private Double longitude;
        private String googlePlaceId;
        private String timezone;
    }

    /**
     * Horaires de travail (embedded)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @UserDefinedType("working_hours")
    public static class WorkingHours {
        private Boolean isOpen = true;
        private LocalTime openTime;
        private LocalTime closeTime;
        private LocalTime breakStartTime;
        private LocalTime breakEndTime;
    }

    /**
     * Paramètres de l'agence (embedded)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @UserDefinedType("agency_settings")
    public static class AgencySettings {

        // Paramètres opérationnels
        private Boolean allowSelfService = false;
        private Boolean requireInspection = true;
        private Boolean enableKeyBox = false;

        // Notifications
        private Boolean notifyOnReservation = true;
        private Boolean notifyOnReturn = true;
        private Boolean notifyOnLateReturn = true;

        // Géofencing (si activé au niveau organisation)
        private Boolean enableGeofenceAlerts = false;
        private Boolean trackVehiclesRealTime = false;

        // Politiques spécifiques à l'agence
        private Boolean allowInstantBooking = true;
        private Integer advanceBookingDays = 30;
        private Integer minBookingNoticeHours = 2;
    }
}
