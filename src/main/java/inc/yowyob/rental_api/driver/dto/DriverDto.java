// src/main/java/inc/yowyob/rental_api/driver/dto/DriverDto.java
// Ce DTO est maintenant une VUE combinée
package inc.yowyob.rental_api.driver.dto;

import inc.yowyob.rental_api.driver.enums.DriverStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@Builder
public class DriverDto {
    private UUID driverId;
    private UUID userId;
    private UUID organizationId;

    // From User entity
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profilePicture;

    // From Driver entity
    private Integer age;
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
    private DriverStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}