package inc.yowyob.rental_api.user.entities;

import inc.yowyob.rental_api.core.enums.UserStatus;
import inc.yowyob.rental_api.core.enums.UserType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table("users")
public class User {

    @Id
    @PrimaryKey
    private UUID id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50)
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    private String phone;

    @NotNull(message = "User type is required")
    private UserType userType;

    @NotNull(message = "User status is required")
    private UserStatus status;

    private UUID organizationId;
    private String profileImageUrl;
    private String address;
    private String city;
    private String country;

    // Email verification
    private Boolean emailVerified = false;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationExpiry;

    // Phone verification
    private Boolean phoneVerified = false;
    private String phoneVerificationCode;
    private LocalDateTime phoneVerificationExpiry;

    // Password reset
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiry;

    // Security
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private Integer failedLoginAttempts = 0;
    private LocalDateTime lockedUntil;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // Constructors
    public User(String email, String password, String firstName, String lastName, UserType userType) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.status = UserStatus.PENDING_VERIFICATION;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean canLogin() {
        return status.canLogin() && !isLocked();
    }

    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    public void lockAccount(int hours) {
        this.lockedUntil = LocalDateTime.now().plusHours(hours);
        this.updatedAt = LocalDateTime.now();
    }

    public void unlockAccount() {
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        this.updatedAt = LocalDateTime.now();

        // Lock account after 5 failed attempts for 1 hour
        if (this.failedLoginAttempts >= 5) {
            lockAccount(1);
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        this.updatedAt = LocalDateTime.now();
        resetFailedLoginAttempts();
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationExpiry = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void verifyPhone() {
        this.phoneVerified = true;
        this.phoneVerificationCode = null;
        this.phoneVerificationExpiry = null;
        this.updatedAt = LocalDateTime.now();
    }
}
