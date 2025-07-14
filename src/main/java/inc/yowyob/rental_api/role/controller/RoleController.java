package inc.yowyob.rental_api.role.controller;

import inc.yowyob.rental_api.common.response.ApiResponse;
import inc.yowyob.rental_api.common.response.ApiResponseUtil;
import inc.yowyob.rental_api.role.dto.*;
import inc.yowyob.rental_api.role.service.RoleService;
import inc.yowyob.rental_api.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs de gestion des rôles et permissions")
public class RoleController {

    private final RoleService roleService;

    @Operation(
        summary = "Créer un nouveau rôle",
        description = "Crée un nouveau rôle avec les permissions spécifiées pour une organisation"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Rôle créé avec succès"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Données d'entrée invalides"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Permissions insuffisantes"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Un rôle avec ce nom existe déjà"
        )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public ResponseEntity<ApiResponse<RoleDto>> createRole(
        @Parameter(description = "Informations du nouveau rôle")
        @Valid @RequestBody CreateRoleDto createRoleDto
    ) {
        log.info("POST /roles - Creating new role: {}", createRoleDto.getName());

        try {
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            RoleDto createdRole = roleService.createRole(createRoleDto, currentUserId);

            log.info("Role created successfully with ID: {}", createdRole.getId());

            return ApiResponseUtil.created(
                createdRole,
                "Rôle créé avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data for role creation: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating role", e);
            return ApiResponseUtil.error(
                "Erreur lors de la création du rôle",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer un rôle par ID",
        description = "Retourne les détails d'un rôle spécifique"
    )
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<RoleDto>> getRoleById(
        @Parameter(description = "ID du rôle")
        @PathVariable UUID roleId
    ) {
        log.info("GET /roles/{} - Fetching role", roleId);

        try {
            RoleDto role = roleService.getRoleById(roleId);

            return ApiResponseUtil.success(
                role,
                "Rôle récupéré avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Role not found: {}", roleId);
            return ApiResponseUtil.notFound("Rôle non trouvé");
        } catch (Exception e) {
            log.error("Error fetching role: {}", roleId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération du rôle",
                500
            );
        }
    }

    @Operation(
        summary = "Mettre à jour un rôle",
        description = "Met à jour les informations d'un rôle existant"
    )
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(
        @Parameter(description = "ID du rôle")
        @PathVariable UUID roleId,
        @Parameter(description = "Nouvelles informations du rôle")
        @Valid @RequestBody UpdateRoleDto updateRoleDto
    ) {
        log.info("PUT /roles/{} - Updating role", roleId);

        try {
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            RoleDto updatedRole = roleService.updateRole(roleId, updateRoleDto, currentUserId);

            return ApiResponseUtil.success(
                updatedRole,
                "Rôle mis à jour avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data for role update: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Cannot update role: {}", e.getMessage());
            return ApiResponseUtil.forbidden(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating role: {}", roleId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la mise à jour du rôle",
                500
            );
        }
    }

    @Operation(
        summary = "Supprimer un rôle",
        description = "Supprime un rôle existant (si aucun utilisateur ne l'a assigné)"
    )
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
        @Parameter(description = "ID du rôle")
        @PathVariable UUID roleId
    ) {
        log.info("DELETE /roles/{} - Deleting role", roleId);

        try {
            roleService.deleteRole(roleId);

            return ApiResponseUtil.success(
                null,
                "Rôle supprimé avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Role not found for deletion: {}", roleId);
            return ApiResponseUtil.notFound("Rôle non trouvé");
        } catch (IllegalStateException e) {
            log.warn("Cannot delete role: {}", e.getMessage());
            return ApiResponseUtil.forbidden(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting role: {}", roleId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la suppression du rôle",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les rôles d'une organisation",
        description = "Retourne tous les rôles d'une organisation spécifique"
    )
    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<List<RoleDto>>> getRolesByOrganization(
        @Parameter(description = "ID de l'organisation")
        @PathVariable UUID organizationId,
        @Parameter(description = "Inclure uniquement les rôles actifs")
        @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        log.info("GET /roles/organization/{} - Fetching roles (activeOnly: {})", organizationId, activeOnly);

        try {
            List<RoleDto> roles = activeOnly
                ? roleService.getActiveRolesByOrganizationId(organizationId)
                : roleService.getRolesByOrganizationId(organizationId);

            return ApiResponseUtil.success(
                roles,
                "Rôles récupérés avec succès",
                roles.size()
            );
        } catch (Exception e) {
            log.error("Error fetching roles for organization: {}", organizationId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des rôles",
                500
            );
        }
    }

    @Operation(
        summary = "Assigner un rôle à un utilisateur",
        description = "Assigne un rôle spécifique à un utilisateur"
    )
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('USER_MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<UserRoleDto>> assignRole(
        @Parameter(description = "Informations d'assignation du rôle")
        @Valid @RequestBody AssignRoleDto assignRoleDto
    ) {
        log.info("POST /roles/assign - Assigning role {} to user {}",
            assignRoleDto.getRoleId(), assignRoleDto.getUserId());

        try {
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            UserRoleDto userRole = roleService.assignRole(assignRoleDto, currentUserId);

            return ApiResponseUtil.created(
                userRole,
                "Rôle assigné avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data for role assignment: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Cannot assign role: {}", e.getMessage());
            return ApiResponseUtil.conflict(e.getMessage());
        } catch (Exception e) {
            log.error("Error assigning role", e);
            return ApiResponseUtil.error(
                "Erreur lors de l'assignation du rôle",
                500
            );
        }
    }

    @Operation(
        summary = "Révoquer un rôle d'un utilisateur",
        description = "Retire un rôle spécifique d'un utilisateur"
    )
    @DeleteMapping("/revoke/{userId}/{roleId}")
    @PreAuthorize("hasAuthority('USER_MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<Void>> revokeRole(
        @Parameter(description = "ID de l'utilisateur")
        @PathVariable UUID userId,
        @Parameter(description = "ID du rôle")
        @PathVariable UUID roleId
    ) {
        log.info("DELETE /roles/revoke/{}/{} - Revoking role", userId, roleId);

        try {
            roleService.revokeRole(userId, roleId);

            return ApiResponseUtil.success(
                null,
                "Rôle révoqué avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Role assignment not found: {}", e.getMessage());
            return ApiResponseUtil.notFound("Assignation de rôle non trouvée");
        } catch (Exception e) {
            log.error("Error revoking role", e);
            return ApiResponseUtil.error(
                "Erreur lors de la révocation du rôle",
                500
            );
        }
    }

    @Operation(
        summary = "Assignation en masse de rôles",
        description = "Assigne un rôle à plusieurs utilisateurs en une seule opération"
    )
    @PostMapping("/bulk-assign")
    @PreAuthorize("hasAuthority('USER_MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<List<UserRoleDto>>> bulkAssignRole(
        @Parameter(description = "Informations d'assignation en masse")
        @Valid @RequestBody BulkAssignRoleDto bulkAssignRoleDto
    ) {
        log.info("POST /roles/bulk-assign - Bulk assigning role {} to {} users",
            bulkAssignRoleDto.getRoleId(), bulkAssignRoleDto.getUserIds().size());

        try {
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            List<UserRoleDto> assignments = roleService.bulkAssignRole(bulkAssignRoleDto, currentUserId);

            return ApiResponseUtil.success(
                assignments,
                String.format("Rôle assigné à %d utilisateur(s) avec succès", assignments.size()),
                assignments.size()
            );
        } catch (Exception e) {
            log.error("Error in bulk role assignment", e);
            return ApiResponseUtil.error(
                "Erreur lors de l'assignation en masse",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les rôles d'un utilisateur",
        description = "Retourne tous les rôles assignés à un utilisateur spécifique"
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('USER_READ') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<UserRoleDto>>> getUserRoles(
        @Parameter(description = "ID de l'utilisateur")
        @PathVariable UUID userId
    ) {
        log.info("GET /roles/user/{} - Fetching user roles", userId);

        try {
            List<UserRoleDto> userRoles = roleService.getUserRoles(userId);

            return ApiResponseUtil.success(
                userRoles,
                "Rôles de l'utilisateur récupérés avec succès",
                userRoles.size()
            );
        } catch (Exception e) {
            log.error("Error fetching user roles: {}", userId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des rôles de l'utilisateur",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les permissions effectives d'un utilisateur",
        description = "Calcule et retourne toutes les permissions qu'un utilisateur possède via ses rôles"
    )
    @GetMapping("/user/{userId}/permissions")
    @PreAuthorize("hasAuthority('USER_READ') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserPermissionsDto>> getUserPermissions(
        @Parameter(description = "ID de l'utilisateur")
        @PathVariable UUID userId,
        @Parameter(description = "ID de l'organisation")
        @RequestParam UUID organizationId
    ) {
        log.info("GET /roles/user/{}/permissions - Fetching user permissions for org {}", userId, organizationId);

        try {
            UserPermissionsDto permissions = roleService.getUserEffectivePermissions(userId, organizationId);

            return ApiResponseUtil.success(
                permissions,
                "Permissions de l'utilisateur récupérées avec succès"
            );
        } catch (Exception e) {
            log.error("Error fetching user permissions: {}", userId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des permissions",
                500
            );
        }
    }

    @Operation(
        summary = "Mettre à jour les permissions d'un rôle",
        description = "Met à jour la liste des permissions assignées à un rôle"
    )
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN_PERMISSIONS')")
    public ResponseEntity<ApiResponse<RoleDto>> updateRolePermissions(
        @Parameter(description = "ID du rôle")
        @PathVariable UUID roleId,
        @Parameter(description = "Nouvelles permissions du rôle")
        @Valid @RequestBody RolePermissionsDto rolePermissionsDto
    ) {
        log.info("PUT /roles/{}/permissions - Updating role permissions", roleId);

        try {
            // Vérifier que l'ID du rôle correspond
            if (!roleId.equals(rolePermissionsDto.getRoleId())) {
                return ApiResponseUtil.badRequest("L'ID du rôle ne correspond pas");
            }

            UUID currentUserId = SecurityUtils.getCurrentUserId();
            RoleDto updatedRole = roleService.updateRolePermissions(rolePermissionsDto, currentUserId);

            return ApiResponseUtil.success(
                updatedRole,
                "Permissions du rôle mises à jour avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data for permissions update: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Cannot update permissions: {}", e.getMessage());
            return ApiResponseUtil.forbidden(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating role permissions: {}", roleId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la mise à jour des permissions",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer toutes les permissions disponibles",
        description = "Retourne toutes les permissions du système groupées par ressource"
    )
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<List<PermissionGroupDto>>> getAllPermissions() {
        log.info("GET /roles/permissions - Fetching all available permissions");

        try {
            List<PermissionGroupDto> permissionGroups = roleService.getAllPermissionsGrouped();

            return ApiResponseUtil.success(
                permissionGroups,
                "Permissions récupérées avec succès",
                permissionGroups.size()
            );
        } catch (Exception e) {
            log.error("Error fetching permissions", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des permissions",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les statistiques des rôles",
        description = "Retourne les statistiques des rôles pour une organisation"
    )
    @GetMapping("/stats/{organizationId}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<RoleStatsDto>> getRoleStats(
        @Parameter(description = "ID de l'organisation")
        @PathVariable UUID organizationId
    ) {
        log.info("GET /roles/stats/{} - Fetching role statistics", organizationId);

        try {
            RoleStatsDto stats = roleService.getRoleStats(organizationId);

            return ApiResponseUtil.success(
                stats,
                "Statistiques des rôles récupérées avec succès"
            );
        } catch (Exception e) {
            log.error("Error fetching role statistics: {}", organizationId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des statistiques",
                500
            );
        }
    }

    @Operation(
        summary = "Créer les rôles par défaut",
        description = "Crée les rôles par défaut pour une nouvelle organisation"
    )
    @PostMapping("/default/{organizationId}")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public ResponseEntity<ApiResponse<List<RoleDto>>> createDefaultRoles(
        @Parameter(description = "ID de l'organisation")
        @PathVariable UUID organizationId
    ) {
        log.info("POST /roles/default/{} - Creating default roles", organizationId);

        try {
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            List<RoleDto> createdRoles = roleService.createDefaultRoles(organizationId, currentUserId);

            return ApiResponseUtil.created(
                createdRoles,
                String.format("%d rôles par défaut créés avec succès", createdRoles.size())
            );
        } catch (Exception e) {
            log.error("Error creating default roles for organization: {}", organizationId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la création des rôles par défaut",
                500
            );
        }
    }

    @Operation(
        summary = "Nettoyer les assignations expirées",
        description = "Met à jour le statut des assignations de rôles expirées (tâche administrative)"
    )
    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<String>> cleanupExpiredAssignments() {
        log.info("POST /roles/cleanup-expired - Cleaning up expired assignments");

        try {
            roleService.cleanupExpiredAssignments();

            return ApiResponseUtil.success(
                "Nettoyage effectué avec succès",
                "Assignations expirées mises à jour"
            );
        } catch (Exception e) {
            log.error("Error cleaning up expired assignments", e);
            return ApiResponseUtil.error(
                "Erreur lors du nettoyage des assignations expirées",
                500
            );
        }
    }

    @Operation(
        summary = "Activer/Désactiver un rôle",
        description = "Change le statut actif/inactif d'un rôle"
    )
    @PatchMapping("/{roleId}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleDto>> toggleRoleStatus(
        @Parameter(description = "ID du rôle")
        @PathVariable UUID roleId
    ) {
        log.info("PATCH /roles/{}/toggle-status - Toggling role status", roleId);

        try {
            UUID currentUserId = SecurityUtils.getCurrentUserId();

            // Récupérer le rôle actuel pour inverser son statut
            RoleDto currentRole = roleService.getRoleById(roleId);

            UpdateRoleDto updateDto = new UpdateRoleDto();
            updateDto.setIsActive(!Boolean.TRUE.equals(currentRole.getIsActive()));

            RoleDto updatedRole = roleService.updateRole(roleId, updateDto, currentUserId);

            String message = Boolean.TRUE.equals(updatedRole.getIsActive())
                ? "Rôle activé avec succès"
                : "Rôle désactivé avec succès";

            return ApiResponseUtil.success(updatedRole, message);
        } catch (IllegalArgumentException e) {
            log.warn("Role not found: {}", roleId);
            return ApiResponseUtil.notFound("Rôle non trouvé");
        } catch (IllegalStateException e) {
            log.warn("Cannot toggle role status: {}", e.getMessage());
            return ApiResponseUtil.forbidden(e.getMessage());
        } catch (Exception e) {
            log.error("Error toggling role status: {}", roleId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la modification du statut du rôle",
                500
            );
        }
    }

    @Operation(
        summary = "Cloner un rôle",
        description = "Crée une copie d'un rôle existant avec un nouveau nom"
    )
    @PostMapping("/{roleId}/clone")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public ResponseEntity<ApiResponse<RoleDto>> cloneRole(
        @Parameter(description = "ID du rôle à cloner")
        @PathVariable UUID roleId,
        @Parameter(description = "Nom du nouveau rôle")
        @RequestParam String newName,
        @Parameter(description = "ID de l'organisation de destination (optionnel)")
        @RequestParam(required = false) UUID targetOrganizationId
    ) {
        log.info("POST /roles/{}/clone - Cloning role with new name: {}", roleId, newName);

        try {
            // Récupérer le rôle original
            RoleDto originalRole = roleService.getRoleById(roleId);

            // Utiliser l'organisation courante si pas spécifiée
            UUID orgId = targetOrganizationId != null ? targetOrganizationId : originalRole.getOrganizationId();

            // Créer le nouveau rôle
            CreateRoleDto createDto = new CreateRoleDto();
            createDto.setName(newName);
            createDto.setDescription("Copie de: " + originalRole.getDescription());
            createDto.setOrganizationId(orgId);
            createDto.setRoleType(originalRole.getRoleType());
            createDto.setPermissions(originalRole.getPermissions());
            createDto.setPriority(originalRole.getPriority());
            createDto.setColor(originalRole.getColor());
            createDto.setIcon(originalRole.getIcon());
            createDto.setIsDefaultRole(false); // Les clones ne sont jamais des rôles par défaut

            UUID currentUserId = SecurityUtils.getCurrentUserId();
            RoleDto clonedRole = roleService.createRole(createDto, currentUserId);

            return ApiResponseUtil.created(
                clonedRole,
                "Rôle cloné avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Error cloning role: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error cloning role: {}", roleId, e);
            return ApiResponseUtil.error(
                "Erreur lors du clonage du rôle",
                500
            );
        }
    }
}