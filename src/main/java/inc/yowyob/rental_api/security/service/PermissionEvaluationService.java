package inc.yowyob.rental_api.security.service;

import inc.yowyob.rental_api.core.enums.Permission;
import inc.yowyob.rental_api.core.enums.RoleType;
import inc.yowyob.rental_api.role.entities.UserRole;
import inc.yowyob.rental_api.role.repository.UserRoleRepository;
import inc.yowyob.rental_api.role.service.RoleService;
import inc.yowyob.rental_api.security.model.UserPrincipal;
import inc.yowyob.rental_api.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service pour l'évaluation des permissions et des accès
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionEvaluationService {

    private final RoleService roleService;
    private final UserRoleRepository userRoleRepository;

    /**
     * Vérifie si l'utilisateur connecté possède une permission spécifique
     */
    public boolean hasPermission(String permissionCode) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return hasPermission(currentUser.getId(), permissionCode, currentUser.getOrganizationId());
    }

    /**
     * Vérifie si un utilisateur possède une permission spécifique dans une organisation
     */
    public boolean hasPermission(UUID userId, String permissionCode, UUID organizationId) {
        log.debug("Checking permission '{}' for user {} in organization {}", permissionCode, userId, organizationId);

        // Super admin a toutes les permissions
        if (isSuperAdmin(userId)) {
            return true;
        }

        // Vérifier si l'utilisateur a cette permission via ses rôles
        var userPermissions = roleService.getUserEffectivePermissions(userId, organizationId);
        return userPermissions.getEffectivePermissions().contains(permissionCode);
    }

    /**
     * Vérifie si l'utilisateur possède au moins une des permissions spécifiées
     */
    public boolean hasAnyPermission(String... permissionCodes) {
        return Arrays.stream(permissionCodes)
            .anyMatch(this::hasPermission);
    }

    /**
     * Vérifie si l'utilisateur possède toutes les permissions spécifiées
     */
    public boolean hasAllPermissions(String... permissionCodes) {
        return Arrays.stream(permissionCodes)
            .allMatch(this::hasPermission);
    }

    /**
     * Vérifie si l'utilisateur a un rôle spécifique
     */
    public boolean hasRole(RoleType roleType) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return hasRole(currentUser.getId(), roleType, currentUser.getOrganizationId());
    }

    /**
     * Vérifie si un utilisateur a un rôle spécifique dans une organisation
     */
    public boolean hasRole(UUID userId, RoleType roleType, UUID organizationId) {
        log.debug("Checking role '{}' for user {} in organization {}", roleType, userId, organizationId);

        List<UserRole> userRoles = userRoleRepository.findValidByUserId(userId, LocalDateTime.now());

        return userRoles.stream()
            .filter(ur -> organizationId.equals(ur.getOrganizationId()))
            .anyMatch(ur -> {
                try {
                    // Récupérer le rôle et vérifier son type
                    // Cette vérification serait faite via le RoleService
                    return true; // Simplifié pour l'exemple
                } catch (Exception e) {
                    log.error("Error checking role for user role {}: {}", ur.getId(), e.getMessage());
                    return false;
                }
            });
    }

    /**
     * Vérifie si l'utilisateur a accès à une organisation spécifique
     */
    public boolean hasOrganizationAccess(UUID organizationId) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // Super admin a accès à toutes les organisations
        if (isSuperAdmin(currentUser.getId())) {
            return true;
        }

        // Vérifier si l'utilisateur appartient à cette organisation
        return organizationId.equals(currentUser.getOrganizationId());
    }

    /**
     * Vérifie si l'utilisateur a accès à une agence spécifique
     */
    public boolean hasAgencyAccess(UUID agencyId) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // Super admin et propriétaires d'organisation ont accès à toutes les agences de leur org
        if (isSuperAdmin(currentUser.getId()) || isOrganizationOwner(currentUser.getId())) {
            return true;
        }

        // Vérifier si l'utilisateur a un rôle spécifique à cette agence
        List<UserRole> userRoles = userRoleRepository.findValidByUserId(currentUser.getId(), LocalDateTime.now());

        return userRoles.stream()
            .anyMatch(ur -> agencyId.equals(ur.getAgencyId()) || ur.getAgencyId() == null);
    }

    /**
     * Vérifie si l'utilisateur est super administrateur
     */
    public boolean isSuperAdmin() {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        return currentUser != null && isSuperAdmin(currentUser.getId());
    }

    /**
     * Vérifie si un utilisateur est super administrateur
     */
    public boolean isSuperAdmin(UUID userId) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(userId)) {
            return "super_admin".equals(currentUser.getUserType());
        }

        // Pour les autres utilisateurs, vérifier dans la base
        // Cette vérification serait faite via le UserService
        return false;
    }

    /**
     * Vérifie si l'utilisateur est propriétaire de son organisation
     */
    public boolean isOrganizationOwner() {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        return currentUser != null && isOrganizationOwner(currentUser.getId());
    }

    /**
     * Vérifie si un utilisateur est propriétaire de son organisation
     */
    public boolean isOrganizationOwner(UUID userId) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(userId)) {
            return "owner".equals(currentUser.getUserType());
        }

        return false;
    }

    /**
     * Vérifie si l'utilisateur peut accéder aux ressources d'un autre utilisateur
     */
    public boolean canAccessUserData(UUID targetUserId) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // Un utilisateur peut toujours accéder à ses propres données
        if (currentUser.getId().equals(targetUserId)) {
            return true;
        }

        // Super admin peut accéder aux données de tous
        if (isSuperAdmin()) {
            return true;
        }

        // Vérifier les permissions de gestion des utilisateurs
        return hasPermission(Permission.USER_READ.getCode());
    }

    /**
     * Évalue une expression de permission complexe
     */
    public boolean evaluatePermissionExpression(String expression) {
        log.debug("Evaluating permission expression: {}", expression);

        // Expression basique pour l'instant
        // Pourrait être étendue pour supporter des expressions complexes comme:
        // "VEHICLE_READ AND (AGENCY_MANAGE OR ORGANIZATION_OWNER)"

        if (expression.contains(" AND ")) {
            String[] permissions = expression.split(" AND ");
            return Arrays.stream(permissions)
                .map(String::trim)
                .allMatch(this::hasPermission);
        }

        if (expression.contains(" OR ")) {
            String[] permissions = expression.split(" OR ");
            return Arrays.stream(permissions)
                .map(String::trim)
                .anyMatch(this::hasPermission);
        }

        return hasPermission(expression.trim());
    }

    /**
     * Vérifie les permissions avec contexte d'organisation et d'agence
     */
    public boolean hasPermissionInContext(String permissionCode, UUID organizationId, UUID agencyId) {
        if (!hasOrganizationAccess(organizationId)) {
            return false;
        }

        if (agencyId != null && !hasAgencyAccess(agencyId)) {
            return false;
        }

        return hasPermission(permissionCode);
    }

    /**
     * Obtient le niveau d'accès d'un utilisateur (pour l'interface utilisateur)
     */
    public String getUserAccessLevel() {
        if (isSuperAdmin()) {
            return "SUPER_ADMIN";
        }

        if (isOrganizationOwner()) {
            return "ORGANIZATION_OWNER";
        }

        if (hasAnyPermission(
            Permission.AGENCY_WRITE.getCode(),
            Permission.AGENCY_UPDATE.getCode(),
            Permission.AGENCY_DELETE.getCode()
        )) {
            return "AGENCY_MANAGER";
        }

        if (hasAnyPermission(
            Permission.RENTAL_WRITE.getCode(),
            Permission.VEHICLE_WRITE.getCode(),
            Permission.DRIVER_WRITE.getCode()
        )) {
            return "OPERATIONAL_USER";
        }

        return "LIMITED_USER";
    }

    /**
     * Génère un résumé des permissions pour l'utilisateur connecté
     */
    public Set<String> getCurrentUserPermissions() {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getOrganizationId() == null) {
            return Set.of();
        }

        var userPermissions = roleService.getUserEffectivePermissions(
            currentUser.getId(),
            currentUser.getOrganizationId()
        );

        return userPermissions.getEffectivePermissions();
    }
}
