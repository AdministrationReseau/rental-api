package inc.yowyob.rental_api.security.model;

import inc.yowyob.rental_api.user.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private UUID id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String userType;
    private String status;
    private String accessLevel;
    private UUID organizationId;
    private UUID agencyId; // NOUVEAU: Support des agences
    private Boolean isAgencyBound; // NOUVEAU: Indique si l'utilisateur est lié à une agence
    private String employeeId; // NOUVEAU: ID employé
    private String department; // NOUVEAU: Département
    private String position; // NOUVEAU: Poste
    private boolean accountNonLocked;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getUserType().getCode().toUpperCase())
        );

        return new UserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getFirstName(),
            user.getLastName(),
            user.getUserType().getCode(),
            user.getStatus().getCode(),
            user.getAccessLevel(), // NOUVEAU
            user.getOrganizationId(),
            user.getAgencyId(), // NOUVEAU
            user.isAgencyBound(), // NOUVEAU
            user.getEmployeeId(), // NOUVEAU
            user.getDepartment(), // NOUVEAU
            user.getPosition(), // NOUVEAU
            !user.isLocked(),
            authorities
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "active".equals(status) || "pending_verification".equals(status);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Méthodes pour la gestion des agences
    public boolean isStaff() {
        return "staff".equals(userType);
    }

    public boolean isOwner() {
        return "owner".equals(userType);
    }

    public boolean isSuperAdmin() {
        return "super_admin".equals(userType);
    }

    public boolean isClient() {
        return "client".equals(userType);
    }

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
        if (isStaff() && Boolean.TRUE.equals(isAgencyBound)) {
            return targetAgencyId != null && targetAgencyId.equals(agencyId);
        }

        return false;
    }

    public boolean canAccessOrganization(UUID targetOrganizationId) {
        // Super admin peut tout voir
        if (isSuperAdmin()) {
            return true;
        }

        // Utilisateurs peuvent voir leur organisation
        return organizationId != null && organizationId.equals(targetOrganizationId);
    }

    public boolean hasOrganizationAccess() {
        return organizationId != null;
    }

    public boolean hasAgencyAccess() {
        return agencyId != null && Boolean.TRUE.equals(isAgencyBound);
    }

    public String getDisplayName() {
        StringBuilder display = new StringBuilder(getFullName());

        if (employeeId != null) {
            display.append(" (").append(employeeId).append(")");
        }

        if (position != null) {
            display.append(" - ").append(position);
        }

        if (department != null) {
            display.append(" [").append(department).append("]");
        }

        return display.toString();
    }

    /**
     * Détermine le scope d'accès de l'utilisateur
     */
    public String getAccessScope() {
        if (isSuperAdmin()) {
            return "GLOBAL";
        } else if (isOwner()) {
            return "ORGANIZATION";
        } else if (isStaff() && hasAgencyAccess()) {
            return "AGENCY";
        } else {
            return "LIMITED";
        }
    }

    /**
     * Vérifie si l'utilisateur peut effectuer des actions administratives
     */
    public boolean canAdministrate() {
        return isSuperAdmin() || isOwner();
    }

    /**
     * Vérifie si l'utilisateur peut gérer une agence spécifique
     */
    public boolean canManageAgency(UUID targetAgencyId) {
        if (isSuperAdmin() || isOwner()) {
            return true;
        }

        // Staff peut gérer son agence selon ses permissions de rôle
        return isStaff() && targetAgencyId != null && targetAgencyId.equals(agencyId);
    }
}
