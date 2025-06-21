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
}
