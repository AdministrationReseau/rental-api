package inc.yowyob.rental_api.user.dto;

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
public class AuthResponseDto {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String userType;
    private String status;
    private UUID organizationId;
    private Boolean emailVerified;
    private Boolean phoneVerified;

    private LocalDateTime lastLoginAt;
}
