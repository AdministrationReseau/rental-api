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

    // NOUVEAU: Support des agences pour le personnel
    private UUID organizationId;
    private UUID agencyId; // Pour les utilisateurs STAFF et DRIVER

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

    // NOUVEAU: Informations personnel/employé
    private String employeeId; // ID employé pour le personnel
    private String department; // Département/Service
    private String position; // Poste/Fonction
    private LocalDateTime hiredAt; // Date d'embauche
    private UUID supervisorId; // Superviseur direct

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

    // NOUVEAU: Constructeur pour le personnel avec agence
    public User(String email, String password, String firstName, String lastName,
                UserType userType, UUID organizationId, UUID agencyId) {
        this(email, password, firstName, lastName, userType);
        this.organizationId = organizationId;
        this.agencyId = agencyId;
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

    // NOUVEAUX: Méthodes pour la gestion des agences
    public boolean isStaff() {
        return UserType.STAFF.equals(this.userType);
    }

    public boolean isDriver() {
        return UserType.CLIENT.equals(this.userType); // Les chauffeurs sont des clients spéciaux
    }

    public boolean isAgencyBound() {
        return isStaff() && this.agencyId != null;
    }

    public boolean belongsToAgency(UUID agencyId) {
        return agencyId != null && agencyId.equals(this.agencyId);
    }

    public boolean belongsToOrganization(UUID organizationId) {
        return organizationId != null && organizationId.equals(this.organizationId);
    }

    public void assignToAgency(UUID agencyId) {
        this.agencyId = agencyId;
        this.updatedAt = LocalDateTime.now();
    }

    public void removeFromAgency() {
        this.agencyId = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void setEmployeeInfo(String employeeId, String department, String position, UUID supervisorId) {
        this.employeeId = employeeId;
        this.department = department;
        this.position = position;
        this.supervisorId = supervisorId;
        this.hiredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOwner() {
        return UserType.OWNER.equals(this.userType);
    }

    public boolean isSuperAdmin() {
        return UserType.SUPER_ADMIN.equals(this.userType);
    }

    public boolean isClient() {
        return UserType.CLIENT.equals(this.userType);
    }

    /**
     * Vérifie si l'utilisateur peut accéder aux données d'une agence spécifique
     */
    public boolean canAccessAgency(UUID targetAgencyId) {
        // Super admin peut tout voir
        if (isSuperAdmin()) {
            return true;
        }

        // Propriétaire peut voir toutes les agences de son organisation
        if (isOwner()) {
            return true;
        }

        // Staff ne peut voir que son agence
        if (isStaff()) {
            return belongsToAgency(targetAgencyId);
        }

        return false;
    }

    /**
     * Détermine le niveau d'accès de l'utilisateur
     */
    public String getAccessLevel() {
        if (isSuperAdmin()) {
            return "SUPER_ADMIN";
        } else if (isOwner()) {
            return "ORGANIZATION_OWNER";
        } else if (isStaff() && agencyId != null) {
            return "AGENCY_STAFF";
        } else if (isClient()) {
            return "CLIENT";
        } else {
            return "LIMITED";
        }
    }
}
