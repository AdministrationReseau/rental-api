package inc.yowyob.rental_api.security.util;

import inc.yowyob.rental_api.security.model.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    /**
     * Récupère l'utilisateur connecté
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Récupère l'ID de l'utilisateur connecté
     */
    public static UUID getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Récupère l'email de l'utilisateur connecté
     */
    public static String getCurrentUserEmail() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Récupère l'ID de l'organisation de l'utilisateur connecté
     */
    public static UUID getCurrentUserOrganizationId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getOrganizationId() : null;
    }

    /**
     * NOUVEAU: Récupère l'ID de l'agence de l'utilisateur connecté
     */
    public static UUID getCurrentUserAgencyId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getAgencyId() : null;
    }

    /**
     * NOUVEAU: Vérifie si l'utilisateur connecté est lié à une agence
     */
    public static boolean isCurrentUserAgencyBound() {
        UserPrincipal user = getCurrentUser();
        return user != null && Boolean.TRUE.equals(user.getIsAgencyBound());
    }

    /**
     * NOUVEAU: Récupère l'ID employé de l'utilisateur connecté
     */
    public static String getCurrentUserEmployeeId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getEmployeeId() : null;
    }

    /**
     * NOUVEAU: Récupère le niveau d'accès de l'utilisateur connecté
     */
    public static String getCurrentUserAccessLevel() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getAccessLevel() : null;
    }

    /**
     * NOUVEAU: Récupère le scope d'accès de l'utilisateur connecté
     */
    public static String getCurrentUserAccessScope() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getAccessScope() : "LIMITED";
    }

    /**
     * Vérifie si l'utilisateur connecté est authentifié
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
            authentication.isAuthenticated() &&
            authentication.getPrincipal() instanceof UserPrincipal;
    }

    /**
     * Vérifie si l'utilisateur connecté a un rôle spécifique
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.toUpperCase()));
        }
        return false;
    }

    /**
     * NOUVEAU: Vérifie si l'utilisateur connecté est propriétaire
     */
    public static boolean isCurrentUserOwner() {
        UserPrincipal user = getCurrentUser();
        return user != null && user.isOwner();
    }

    /**
     * NOUVEAU: Vérifie si l'utilisateur connecté est super admin
     */
    public static boolean isCurrentUserSuperAdmin() {
        UserPrincipal user = getCurrentUser();
        return user != null && user.isSuperAdmin();
    }

    /**
     * NOUVEAU: Vérifie si l'utilisateur connecté est staff
     */
    public static boolean isCurrentUserStaff() {
        UserPrincipal user = getCurrentUser();
        return user != null && user.isStaff();
    }

    /**
     * NOUVEAU: Vérifie si l'utilisateur connecté peut accéder à une organisation
     */
    public static boolean canAccessOrganization(UUID organizationId) {
        UserPrincipal user = getCurrentUser();
        return user != null && user.canAccessOrganization(organizationId);
    }

    /**
     * NOUVEAU: Vérifie si l'utilisateur connecté peut accéder à une agence
     */
    public static boolean canAccessAgency(UUID agencyId) {
        UserPrincipal user = getCurrentUser();
        return user != null && user.canAccessAgency(agencyId);
    }

    /**
     * NOUVEAU: Vérifie si l'utilisateur connecté peut gérer une agence
     */
    public static boolean canManageAgency(UUID agencyId) {
        UserPrincipal user = getCurrentUser();
        return user != null && user.canManageAgency(agencyId);
    }

    /**
     * NOUVEAU: Vérifie si l'utilisateur connecté peut effectuer des actions administratives
     */
    public static boolean canAdministrate() {
        UserPrincipal user = getCurrentUser();
        return user != null && user.canAdministrate();
    }

    /**
     * NOUVEAU: Vérifie si l'utilisateur connecté appartient à la même organisation qu'un autre utilisateur
     */
    public static boolean belongsToSameOrganization(UUID targetOrganizationId) {
        UUID currentOrgId = getCurrentUserOrganizationId();
        return currentOrgId != null && currentOrgId.equals(targetOrganizationId);
    }

    /**
     * NOUVEAU: Vérifie si l'utilisateur connecté appartient à la même agence qu'une autre
     */
    public static boolean belongsToSameAgency(UUID targetAgencyId) {
        UUID currentAgencyId = getCurrentUserAgencyId();
        return currentAgencyId != null && currentAgencyId.equals(targetAgencyId);
    }

    /**
     * NOUVEAU: Valide l'accès basé sur le contexte utilisateur
     */
    public static boolean validateContextAccess(UUID organizationId, UUID agencyId) {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            return false;
        }

        // Super admin a accès à tout
        if (user.isSuperAdmin()) {
            return true;
        }

        // Vérifier l'accès à l'organisation
        if (organizationId != null && !user.canAccessOrganization(organizationId)) {
            return false;
        }

        // Vérifier l'accès à l'agence si spécifiée
        if (agencyId != null && !user.canAccessAgency(agencyId)) {
            return false;
        }

        return true;
    }

    /**
     * NOUVEAU: Filtre les ID d'agences accessibles pour l'utilisateur connecté
     */
    public static boolean isAgencyAccessible(UUID agencyId) {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            return false;
        }

        // Super admin et propriétaires peuvent accéder à toutes les agences de leur org
        if (user.isSuperAdmin() || user.isOwner()) {
            return true;
        }

        // Staff ne peut accéder qu'à son agence
        if (user.isStaff()) {
            return user.canAccessAgency(agencyId);
        }

        return false;
    }

    /**
     * NOUVEAU: Obtient un contexte de sécurité complet
     */
    public static SecurityContext getSecurityContext() {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            return SecurityContext.builder()
                .isAuthenticated(false)
                .build();
        }

        return SecurityContext.builder()
            .isAuthenticated(true)
            .userId(user.getId())
            .email(user.getEmail())
            .userType(user.getUserType())
            .accessLevel(user.getAccessLevel())
            .accessScope(user.getAccessScope())
            .organizationId(user.getOrganizationId())
            .agencyId(user.getAgencyId())
            .isAgencyBound(user.getIsAgencyBound())
            .canAdministrate(user.canAdministrate())
            .build();
    }

    /**
     * NOUVEAU: Classe pour encapsuler le contexte de sécurité
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SecurityContext {
        private Boolean isAuthenticated;
        private UUID userId;
        private String email;
        private String userType;
        private String accessLevel;
        private String accessScope;
        private UUID organizationId;
        private UUID agencyId;
        private Boolean isAgencyBound;
        private Boolean canAdministrate;
    }
}
