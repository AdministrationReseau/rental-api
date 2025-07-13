// src/main/java/inc/yowyob/rental_api/driver/dto/DriverDto.java
// Ce DTO est maintenant une VUE combinée
package inc.yowyob.rental_api.driver.dto;

import inc.yowyob.rental_api.driver.enums.DriverStatus;
import inc.yowyob.rental_api.utilities.Money;
import inc.yowyob.rental_api.utilities.WorkingHours;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@Builder
public class DriverDto {
    private UUID driverId;
    private UUID userId;
    private UUID organizationId;
    private UUID agencyId;

    // From User entity
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profileImageUrl;

    // From Driver entity
    private Integer age; // <-- Champ calculé, pas stocké
    private LocalDate dateOfBirth;
    private String licenseNumber;
    private String licenseType;
    private String location;
    private String idCardUrl;
    private String driverLicenseUrl;
    private List<UUID> assignedVehicleIds;
    private Boolean available;
    private Double rating;
    private String insuranceProvider;
    private String insurancePolicy;
    @Future private LocalDate licenseExpiry;
    @Min(0) private Integer experience;


    // Staff info
    private String registrationId;
    private String cni;
    private String position;
    private String department;
    private String staffStatus;
    private Money hourlyRate;
    private WorkingHours workingHours;
    private LocalDate hireDate;

    // Statut et Audit
    private DriverStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
}