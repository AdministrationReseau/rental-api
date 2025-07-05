package inc.yowyob.rental_api.user.controller;

import inc.yowyob.rental_api.common.response.ApiResponse;
import inc.yowyob.rental_api.common.response.ApiResponseUtil;
import inc.yowyob.rental_api.user.dto.*;
import inc.yowyob.rental_api.user.service.UserService;
import inc.yowyob.rental_api.role.dto.UserRoleDto;
import inc.yowyob.rental_api.role.dto.UserPermissionsDto;
import inc.yowyob.rental_api.role.service.RoleService;
import inc.yowyob.rental_api.security.util.SecurityUtils;
import inc.yowyob.rental_api.core.enums.UserType;
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
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
@Tag(name = "Staff Management", description = "APIs de gestion du personnel - Accessible uniquement aux propriétaires et personnel autorisé")
public class StaffController {

    private final UserService userService;
    private final RoleService roleService;

    @Operation(
        summary = "Créer un nouveau membre du personnel",
        description = "Crée un nouveau utilisateur de type STAFF. Accessible uniquement aux propriétaires d'organisation ou au personnel ayant la permission USER_WRITE"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Personnel créé avec succès"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Données d'entrée invalides"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Permissions insuffisantes - Seuls les propriétaires ou personnel autorisé peuvent créer du personnel"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Un utilisateur avec cet email existe déjà"
        )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE') or @securityUtils.isCurrentUserOwner()")
    public ResponseEntity<ApiResponse<UserDto>> createStaff(
        @Parameter(description = "Informations du nouveau membre du personnel")
        @Valid @RequestBody CreateStaffDto createDto
    ) {
        log.info("POST /staff - Creating new staff member: {} for organization: {}",
            createDto.getEmail(), createDto.getOrganizationId());

        try {
            // Vérification supplémentaire de sécurité
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            if (!userService.canCreateStaff(currentUserId, createDto.getOrganizationId())) {
                log.warn("User {} attempted to create staff without proper permissions", currentUserId);
                return ApiResponseUtil.forbidden("Vous n'avez pas les permissions nécessaires pour créer du personnel");
            }

            // Convertir en RegisterRequestDto avec type STAFF
            RegisterRequestDto registerDto = new RegisterRequestDto();
            registerDto.setEmail(createDto.getEmail());
            registerDto.setPassword(createDto.getPassword());
            registerDto.setFirstName(createDto.getFirstName());
            registerDto.setLastName(createDto.getLastName());
            registerDto.setPhone(createDto.getPhone());
            registerDto.setUserType(UserType.STAFF);
            registerDto.setAcceptTerms(true);

            UserDto createdUser = userService.createUser(registerDto, createDto.getOrganizationId());

            // Assigner à l'agence si spécifiée
            if (createDto.getAgencyId() != null) {
                createdUser = userService.assignUserToAgency(createdUser.getId(), createDto.getAgencyId());
            }

            // Définir les informations employé si fournies
            if (createDto.getEmployeeId() != null || createDto.getDepartment() != null ||
                createDto.getPosition() != null) {
                createdUser = userService.setEmployeeInfo(
                    createdUser.getId(),
                    createDto.getEmployeeId(),
                    createDto.getDepartment(),
                    createDto.getPosition(),
                    createDto.getSupervisorId()
                );
            }

            log.info("Staff member created successfully with ID: {}", createdUser.getId());

            return ApiResponseUtil.created(
                createdUser,
                "Membre du personnel créé avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data for staff creation: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Cannot create staff: {}", e.getMessage());
            return ApiResponseUtil.conflict(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied for staff creation: {}", e.getMessage());
            return ApiResponseUtil.forbidden("Accès refusé");
        } catch (Exception e) {
            log.error("Error creating staff member", e);
            return ApiResponseUtil.error(
                "Erreur lors de la création du membre du personnel",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer tous les membres du personnel",
        description = "Retourne la liste des utilisateurs de type STAFF selon les permissions"
    )
    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllStaff(
        @RequestParam(required = false) UUID organizationId,
        @RequestParam(required = false) UUID agencyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Boolean isActive
    ) {
        log.info("GET /staff - Fetching staff for organization: {}, agency: {}", organizationId, agencyId);

        try {
            List<UserDto> staff = userService.getUsersByType(UserType.STAFF, organizationId, agencyId, page, size, search, isActive);

            return ApiResponseUtil.success(
                staff,
                "Personnel récupéré avec succès",
                staff.size()
            );
        } catch (Exception e) {
            log.error("Error fetching staff", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération du personnel",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer un membre du personnel par ID",
        description = "Retourne les détails d'un membre du personnel spécifique"
    )
    @GetMapping("/{staffId}")
    @PreAuthorize("hasAuthority('USER_READ') or #staffId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserDto>> getStaffById(
        @Parameter(description = "ID du membre du personnel")
        @PathVariable UUID staffId
    ) {
        log.info("GET /staff/{} - Fetching staff details", staffId);

        try {
            UserDto staff = userService.getUserById(staffId);

            // Vérifier que c'est bien un utilisateur STAFF
            if (!UserType.STAFF.equals(staff.getUserType())) {
                return ApiResponseUtil.badRequest("L'utilisateur spécifié n'est pas un membre du personnel");
            }

            return ApiResponseUtil.success(
                staff,
                "Membre du personnel récupéré avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Staff member not found: {}", staffId);
            return ApiResponseUtil.notFound("Membre du personnel non trouvé");
        } catch (SecurityException e) {
            log.warn("Access denied to staff: {}", staffId);
            return ApiResponseUtil.forbidden("Accès refusé à ce membre du personnel");
        } catch (Exception e) {
            log.error("Error fetching staff: {}", staffId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération du membre du personnel",
                500
            );
        }
    }

    @Operation(
        summary = "Mettre à jour un membre du personnel",
        description = "Met à jour les informations d'un membre du personnel"
    )
    @PutMapping("/{staffId}")
    @PreAuthorize("hasAuthority('USER_UPDATE') or (#staffId == authentication.principal.id)")
    public ResponseEntity<ApiResponse<UserDto>> updateStaff(
        @Parameter(description = "ID du membre du personnel")
        @PathVariable UUID staffId,
        @Parameter(description = "Nouvelles informations du personnel")
        @Valid @RequestBody UpdateUserDto updateDto
    ) {
        log.info("PUT /staff/{} - Updating staff member", staffId);

        try {
            UserDto updatedStaff = userService.updateUser(staffId, updateDto);

            return ApiResponseUtil.success(
                updatedStaff,
                "Membre du personnel mis à jour avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid update data for staff: {}", staffId);
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to update staff: {}", staffId);
            return ApiResponseUtil.forbidden("Accès refusé pour modifier ce membre du personnel");
        } catch (Exception e) {
            log.error("Error updating staff: {}", staffId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la mise à jour du membre du personnel",
                500
            );
        }
    }

    @Operation(
        summary = "Supprimer un membre du personnel",
        description = "Supprime définitivement un membre du personnel"
    )
    @DeleteMapping("/{staffId}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
        @Parameter(description = "ID du membre du personnel")
        @PathVariable UUID staffId
    ) {
        log.info("DELETE /staff/{} - Deleting staff member", staffId);

        try {
            userService.deleteUser(staffId);

            return ApiResponseUtil.success(
                null,
                "Membre du personnel supprimé avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Staff member not found for deletion: {}", staffId);
            return ApiResponseUtil.notFound("Membre du personnel non trouvé");
        } catch (IllegalStateException e) {
            log.warn("Cannot delete staff: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to delete staff: {}", staffId);
            return ApiResponseUtil.forbidden("Accès refusé pour supprimer ce membre du personnel");
        } catch (Exception e) {
            log.error("Error deleting staff: {}", staffId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la suppression du membre du personnel",
                500
            );
        }
    }

    @Operation(
        summary = "Activer/Désactiver un membre du personnel",
        description = "Change le statut d'activation d'un membre du personnel"
    )
    @PatchMapping("/{staffId}/status")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<UserDto>> toggleStaffStatus(
        @Parameter(description = "ID du membre du personnel")
        @PathVariable UUID staffId,
        @Parameter(description = "Nouveau statut d'activation")
        @RequestParam boolean isActive
    ) {
        log.info("PATCH /staff/{}/status - Toggling staff status to: {}", staffId, isActive);

        try {
            UserDto updatedStaff = userService.toggleUserStatus(staffId, isActive);

            return ApiResponseUtil.success(
                updatedStaff,
                String.format("Membre du personnel %s avec succès", isActive ? "activé" : "désactivé")
            );
        } catch (IllegalArgumentException e) {
            log.warn("Staff member not found: {}", staffId);
            return ApiResponseUtil.notFound("Membre du personnel non trouvé");
        } catch (Exception e) {
            log.error("Error toggling staff status: {}", staffId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la modification du statut",
                500
            );
        }
    }

    @Operation(
        summary = "Assigner des rôles à un membre du personnel",
        description = "Assigne ou modifie les rôles d'un membre du personnel"
    )
    @PostMapping("/{staffId}/roles")
    @PreAuthorize("hasAuthority('USER_MANAGE_ROLES')")
    public ResponseEntity<ApiResponse<List<UserRoleDto>>> assignRoles(
        @Parameter(description = "ID du membre du personnel")
        @PathVariable UUID staffId,
        @Parameter(description = "IDs des rôles à assigner")
        @RequestBody List<UUID> roleIds
    ) {
        log.info("POST /staff/{}/roles - Assigning roles to staff member", staffId);

        try {
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            List<UserRoleDto> assignedRoles = roleService.assignRolesToUser(staffId, roleIds, currentUserId);

            return ApiResponseUtil.success(
                assignedRoles,
                "Rôles assignés avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role assignment for staff: {}", staffId);
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to assign roles to staff: {}", staffId);
            return ApiResponseUtil.forbidden("Accès refusé pour assigner des rôles");
        } catch (Exception e) {
            log.error("Error assigning roles to staff: {}", staffId, e);
            return ApiResponseUtil.error(
                "Erreur lors de l'assignation des rôles",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les rôles d'un membre du personnel",
        description = "Retourne la liste des rôles assignés à un membre du personnel"
    )
    @GetMapping("/{staffId}/roles")
    @PreAuthorize("hasAuthority('USER_READ') or #staffId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<UserRoleDto>>> getStaffRoles(
        @Parameter(description = "ID du membre du personnel")
        @PathVariable UUID staffId
    ) {
        log.info("GET /staff/{}/roles - Fetching staff roles", staffId);

        try {
            List<UserRoleDto> roles = roleService.getUserRoles(staffId);

            return ApiResponseUtil.success(
                roles,
                "Rôles du personnel récupérés avec succès",
                roles.size()
            );
        } catch (SecurityException e) {
            log.warn("Access denied to staff roles: {}", staffId);
            return ApiResponseUtil.forbidden("Accès refusé aux rôles de ce membre du personnel");
        } catch (Exception e) {
            log.error("Error fetching staff roles: {}", staffId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des rôles",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les permissions effectives d'un membre du personnel",
        description = "Retourne toutes les permissions qu'un membre du personnel possède via ses rôles"
    )
    @GetMapping("/{staffId}/permissions")
    @PreAuthorize("hasAuthority('USER_READ') or #staffId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserPermissionsDto>> getStaffPermissions(
        @Parameter(description = "ID du membre du personnel")
        @PathVariable UUID staffId,
        @Parameter(description = "ID de l'organisation")
        @RequestParam UUID organizationId
    ) {
        log.info("GET /staff/{}/permissions - Fetching staff permissions for org {}", staffId, organizationId);

        try {
            UserPermissionsDto permissions = roleService.getUserEffectivePermissions(staffId, organizationId);

            return ApiResponseUtil.success(
                permissions,
                "Permissions du personnel récupérées avec succès"
            );
        } catch (SecurityException e) {
            log.warn("Access denied to staff permissions: {}", staffId);
            return ApiResponseUtil.forbidden("Accès refusé aux permissions");
        } catch (Exception e) {
            log.error("Error fetching staff permissions: {}", staffId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des permissions",
                500
            );
        }
    }

    @Operation(
        summary = "Réinitialiser le mot de passe d'un membre du personnel",
        description = "Génère un nouveau mot de passe temporaire pour un membre du personnel"
    )
    @PostMapping("/{staffId}/reset-password")
    @PreAuthorize("hasAuthority('USER_RESET_PASSWORD')")
    public ResponseEntity<ApiResponse<String>> resetStaffPassword(
        @Parameter(description = "ID du membre du personnel")
        @PathVariable UUID staffId
    ) {
        log.info("POST /staff/{}/reset-password - Resetting staff password", staffId);

        try {
            String temporaryPassword = userService.resetUserPassword(staffId);

            return ApiResponseUtil.success(
                temporaryPassword,
                "Mot de passe réinitialisé avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Staff member not found for password reset: {}", staffId);
            return ApiResponseUtil.notFound("Membre du personnel non trouvé");
        } catch (SecurityException e) {
            log.warn("Access denied to reset staff password: {}", staffId);
            return ApiResponseUtil.forbidden("Accès refusé pour réinitialiser le mot de passe");
        } catch (Exception e) {
            log.error("Error resetting staff password: {}", staffId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la réinitialisation du mot de passe",
                500
            );
        }
    }

    @Operation(
        summary = "Assigner un membre du personnel à une agence",
        description = "Assigne un membre du personnel à une agence spécifique"
    )
    @PostMapping("/{staffId}/assign-agency")
    @PreAuthorize("hasAuthority('AGENCY_MANAGE_STAFF')")
    public ResponseEntity<ApiResponse<UserDto>> assignToAgency(
        @Parameter(description = "ID du membre du personnel")
        @PathVariable UUID staffId,
        @Parameter(description = "ID de l'agence")
        @RequestParam UUID agencyId
    ) {
        log.info("POST /staff/{}/assign-agency - Assigning staff to agency {}", staffId, agencyId);

        try {
            UserDto updatedStaff = userService.assignUserToAgency(staffId, agencyId);

            return ApiResponseUtil.success(
                updatedStaff,
                "Personnel assigné à l'agence avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid agency assignment: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to assign staff to agency: {}", staffId);
            return ApiResponseUtil.forbidden("Accès refusé pour assigner à une agence");
        } catch (Exception e) {
            log.error("Error assigning staff to agency: {}", staffId, e);
            return ApiResponseUtil.error(
                "Erreur lors de l'assignation à l'agence",
                500
            );
        }
    }

    @Operation(
        summary = "Retirer un membre du personnel d'une agence",
        description = "Retire l'assignation d'un membre du personnel à son agence"
    )
    @DeleteMapping("/{staffId}/remove-agency")
    @PreAuthorize("hasAuthority('AGENCY_MANAGE_STAFF')")
    public ResponseEntity<ApiResponse<UserDto>> removeFromAgency(
        @Parameter(description = "ID du membre du personnel")
        @PathVariable UUID staffId
    ) {
        log.info("DELETE /staff/{}/remove-agency - Removing staff from agency", staffId);

        try {
            UserDto updatedStaff = userService.removeUserFromAgency(staffId);

            return ApiResponseUtil.success(
                updatedStaff,
                "Personnel retiré de l'agence avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Staff removal from agency failed: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to remove staff from agency: {}", staffId);
            return ApiResponseUtil.forbidden("Accès refusé pour retirer de l'agence");
        } catch (Exception e) {
            log.error("Error removing staff from agency: {}", staffId, e);
            return ApiResponseUtil.error(
                "Erreur lors du retrait de l'agence",
                500
            );
        }
    }

    @Operation(
        summary = "Mettre à jour les informations employé",
        description = "Met à jour les informations spécifiques à l'emploi d'un membre du personnel"
    )
    @PutMapping("/{staffId}/employee-info")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<UserDto>> updateEmployeeInfo(
        @Parameter(description = "ID du membre du personnel")
        @PathVariable UUID staffId,
        @Parameter(description = "Nouvelles informations employé")
        @Valid @RequestBody UpdateEmployeeInfoDto updateDto
    ) {
        log.info("PUT /staff/{}/employee-info - Updating employee information", staffId);

        try {
            UserDto updatedStaff = userService.setEmployeeInfo(
                staffId,
                updateDto.getEmployeeId(),
                updateDto.getDepartment(),
                updateDto.getPosition(),
                updateDto.getSupervisorId()
            );

            return ApiResponseUtil.success(
                updatedStaff,
                "Informations employé mises à jour avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid employee info update: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to update employee info: {}", staffId);
            return ApiResponseUtil.forbidden("Accès refusé pour modifier les informations employé");
        } catch (Exception e) {
            log.error("Error updating employee info: {}", staffId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la mise à jour des informations employé",
                500
            );
        }
    }
}
