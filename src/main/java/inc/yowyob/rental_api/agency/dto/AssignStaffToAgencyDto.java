package inc.yowyob.rental_api.agency.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO pour assigner un personnel à une agence
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignStaffToAgencyDto {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Agency ID is required")
    private UUID agencyId;

    @Size(max = 50, message = "Employee ID must not exceed 50 characters")
    private String employeeId;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Size(max = 100, message = "Position must not exceed 100 characters")
    private String position;

    private UUID supervisorId;
}
