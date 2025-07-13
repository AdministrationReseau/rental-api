// src/main/java/inc/yowyob/rental_api/driver/entities/Driver.java
// RENOMMEZ le fichier de DriverSession.java à Driver.java

package inc.yowyob.rental_api.driver.entities;

import inc.yowyob.rental_api.driver.enums.DriverStatus;
import inc.yowyob.rental_api.utilities.Money;
import inc.yowyob.rental_api.utilities.WorkingHours;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("drivers")
public class Driver {

    @PrimaryKey
    private UUID driverId;

    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @Column("agency_id")
    private UUID agencyId; // Lien vers l'agence (peut être null)
    
    @NotNull
    @Column("date_of_birth")
    private LocalDate dateOfBirth;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "License type is required")
    private String licenseType;

    @Column("license_expiry")
    private LocalDate licenseExpiry;

    @Min(0)
    private Integer experience; // Années d'expérience

    private String location;
    
    private String idCardUrl;
    private String driverLicenseUrl;
    
  
    private List<UUID> assignedVehicleIds;

    @NotNull(message = "Availability status is required")
    private Boolean available = true;

    @DecimalMin(value = "0.0") @DecimalMax(value = "5.0")
    private Double rating = 0.0;
    
    private String insuranceProvider;
    private String insurancePolicy;

    private DriverStatus status = DriverStatus.AVAILABLE;
    private LocalDateTime statusUpdatedAt;
    private UUID statusUpdatedBy;
 
   // --- Attributs de Staff ---
    @NotBlank
    @Column("registration_id")
    private String registrationId; // Numéro matricule

    @Column("cni")
    private String cni; // Numéro de Carte d'Identité

    private String position; // Ex: "Chauffeur Principal", "Chauffeur de Bus"
    private String department; // Ex: "Logistique"

    @NotNull
    @Column("staff_status")
    private String staffStatus; // Utiliser une enum (ON_SHIFT, ON_LEAVE, etc.)

    @Column("hourly_rate")
    private Money hourlyRate;

    @Column("working_hours")
    private WorkingHours workingHours;

    @NotNull
    @Column("hire_date")
    private LocalDate hireDate; // Date d'embauche

 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}