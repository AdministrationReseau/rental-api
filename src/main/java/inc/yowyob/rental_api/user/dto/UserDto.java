package inc.yowyob.rental_api.user.dto;

import inc.yowyob.rental_api.core.enums.UserType;
import inc.yowyob.rental_api.core.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserType userType;
    private UserStatus userStatus;
    private UUID organizationId;
    private UUID agencyId;
    private String employeeId;
    private String department;
    private String position;
    private UUID supervisorId;
    private Boolean isActive;
    private Boolean mustChangePassword;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime hiredAt;

    // Informations calculées
    private String fullName;
    private String organizationName;
    private String agencyName;
    private String supervisorName;
    private Boolean hasActiveRoles;
    private Integer roleCount;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
