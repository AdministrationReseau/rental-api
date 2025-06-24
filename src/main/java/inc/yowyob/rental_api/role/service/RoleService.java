package inc.yowyob.rental_api.role.service;

import inc.yowyob.rental_api.core.enums.Permission;
import inc.yowyob.rental_api.core.enums.RoleType;
import inc.yowyob.rental_api.role.dto.*;
import inc.yowyob.rental_api.role.entities.Role;
import inc.yowyob.rental_api.role.entities.UserRole;
import inc.yowyob.rental_api.role.repository.RoleRepository;
import inc.yowyob.rental_api.role.repository.UserRoleRepository;
import inc.yowyob.rental_api.user.entities.User;
import inc.yowyob.rental_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;

    /**
     * Crée un nouveau rôle
     */
    @Transactional
    public RoleDto createRole(CreateRoleDto createRoleDto, UUID createdBy) {
        log.info("Creating new role: {} for organization: {}", createRoleDto.getName(), createRoleDto.getOrganizationId());

        // Vérifier que le nom n'existe pas déjà dans l'organisation
        if (roleRepository.existsByOrganizationIdAndName(createRoleDto.getOrganizationId(), createRoleDto.getName())) {
            throw new IllegalArgumentException("A role with this name already exists in the organization");
        }

        // Valider les permissions
        validatePermissions(createRoleDto.getPermissions());

        // Créer le rôle
        Role role = new Role(
            createRoleDto.getName(),
            createRoleDto.getDescription(),
            createRoleDto.getOrganizationId(),
            createRoleDto.getRoleType()
        );

        role.setPriority(createRoleDto.getPriority());
        role.setPermissions(createRoleDto.getPermissions() != null ? new HashSet<>(createRoleDto.getPermissions()) : new HashSet<>());
        role.setColor(createRoleDto.getColor());
        role.setIcon(createRoleDto.getIcon());
        role.setIsDefaultRole(createRoleDto.getIsDefaultRole());
        role.setCreatedBy(createdBy);
        role.setUpdatedBy(createdBy);

        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully with ID: {}", savedRole.getId());

        return mapToRoleDto(savedRole);
    }

    /**
     * Met à jour un rôle existant
     */
    @Transactional
    public RoleDto updateRole(UUID roleId, UpdateRoleDto updateRoleDto, UUID updatedBy) {
        log.info("Updating role: {}", roleId);

        Role role = getRoleOrThrow(roleId);

        // Vérifier que le rôle peut être modifié
        if (!role.canBeModified()) {
            throw new IllegalStateException("System roles cannot be modified");
        }

        // Vérifier le nom unique si changé
        if (updateRoleDto.getName() != null && !updateRoleDto.getName().equals(role.getName())) {
            if (roleRepository.existsByOrganizationIdAndName(role.getOrganizationId(), updateRoleDto.getName())) {
                throw new IllegalArgumentException("A role with this name already exists in the organization");
            }
            role.setName(updateRoleDto.getName());
        }

        // Mettre à jour les autres champs
        if (updateRoleDto.getDescription() != null) {
            role.setDescription(updateRoleDto.getDescription());
        }
        if (updateRoleDto.getPriority() != null) {
            role.setPriority(updateRoleDto.getPriority());
        }
        if (updateRoleDto.getPermissions() != null) {
            validatePermissions(updateRoleDto.getPermissions());
            role.setPermissions(new HashSet<>(updateRoleDto.getPermissions()));
        }
        if (updateRoleDto.getColor() != null) {
            role.setColor(updateRoleDto.getColor());
        }
        if (updateRoleDto.getIcon() != null) {
            role.setIcon(updateRoleDto.getIcon());
        }
        if (updateRoleDto.getIsDefaultRole() != null) {
            role.setIsDefaultRole(updateRoleDto.getIsDefaultRole());
        }
        if (updateRoleDto.getIsActive() != null) {
            role.setIsActive(updateRoleDto.getIsActive());
        }

        role.setUpdatedBy(updatedBy);
        role.setUpdatedAt(LocalDateTime.now());

        Role savedRole = roleRepository.save(role);
        log.info("Role updated successfully: {}", roleId);

        return mapToRoleDto(savedRole);
    }

    /**
     * Supprime un rôle
     */
    @Transactional
    public void deleteRole(UUID roleId) {
        log.info("Deleting role: {}", roleId);

        Role role = getRoleOrThrow(roleId);

        // Vérifier que le rôle peut être supprimé
        if (!role.canBeDeleted()) {
            throw new IllegalStateException("System roles or default roles cannot be deleted");
        }

        // Vérifier qu'aucun utilisateur n'a ce rôle
        Long userCount = userRoleRepository.countActiveByRoleId(roleId);
        if (userCount > 0) {
            throw new IllegalStateException("Cannot delete role that is assigned to users. Remove all assignments first.");
        }

        roleRepository.delete(role);
        log.info("Role deleted successfully: {}", roleId);
    }

    /**
     * Récupère un rôle par ID
     */
    public RoleDto getRoleById(UUID roleId) {
        log.debug("Fetching role: {}", roleId);
        Role role = getRoleOrThrow(roleId);
        return mapToRoleDto(role);
    }

    /**
     * Récupère tous les rôles d'une organisation
     */
    public List<RoleDto> getRolesByOrganizationId(UUID organizationId) {
        log.debug("Fetching roles for organization: {}", organizationId);
        List<Role> roles = roleRepository.findByOrganizationId(organizationId);
        return roles.stream()
            .map(this::mapToRoleDto)
            .collect(Collectors.toList());
    }

    /**
     * Récupère les rôles actifs d'une organisation
     */
    public List<RoleDto> getActiveRolesByOrganizationId(UUID organizationId) {
        log.debug("Fetching active roles for organization: {}", organizationId);
        List<Role> roles = roleRepository.findActiveByOrganizationId(organizationId);
        return roles.stream()
            .map(this::mapToRoleDto)
            .collect(Collectors.toList());
    }

    /**
     * Assigne un rôle à un utilisateur
     */
    @Transactional
    public UserRoleDto assignRole(AssignRoleDto assignRoleDto, UUID assignedBy) {
        log.info("Assigning role {} to user {} in organization {}",
            assignRoleDto.getRoleId(), assignRoleDto.getUserId(), assignRoleDto.getOrganizationId());

        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(assignRoleDto.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Vérifier que le rôle existe
        Role role = getRoleOrThrow(assignRoleDto.getRoleId());

        // Vérifier que le rôle appartient à la même organisation
        if (!role.getOrganizationId().equals(assignRoleDto.getOrganizationId())) {
            throw new IllegalArgumentException("Role does not belong to the specified organization");
        }

        // Vérifier si l'utilisateur a déjà ce rôle actif
        if (userRoleRepository.hasActiveRole(assignRoleDto.getUserId(), assignRoleDto.getRoleId())) {
            throw new IllegalStateException("User already has this role assigned");
        }

        // Créer l'assignation
        UserRole userRole = new UserRole(
            assignRoleDto.getUserId(),
            assignRoleDto.getRoleId(),
            assignRoleDto.getOrganizationId(),
            assignRoleDto.getAgencyId()
        );

        userRole.setExpiresAt(assignRoleDto.getExpiresAt());
        userRole.setAssignmentReason(assignRoleDto.getAssignmentReason());
        userRole.setAssignedBy(assignedBy);

        UserRole savedUserRole = userRoleRepository.save(userRole);
        log.info("Role assigned successfully: {}", savedUserRole.getId());

        return mapToUserRoleDto(savedUserRole, role, user);
    }

    /**
     * Révoque un rôle d'un utilisateur
     */
    @Transactional
    public void revokeRole(UUID userId, UUID roleId) {
        log.info("Revoking role {} from user {}", roleId, userId);

        UserRole userRole = userRoleRepository.findActiveByUserIdAndRoleId(userId, roleId)
            .orElseThrow(() -> new IllegalArgumentException("Active role assignment not found"));

        userRole.revoke();
        userRoleRepository.save(userRole);

        log.info("Role revoked successfully");
    }

    /**
     * Assigne des rôles en masse
     */
    @Transactional
    public List<UserRoleDto> bulkAssignRole(BulkAssignRoleDto bulkAssignRoleDto, UUID assignedBy) {
        log.info("Bulk assigning role {} to {} users", bulkAssignRoleDto.getRoleId(), bulkAssignRoleDto.getUserIds().size());

        // Vérifier que le rôle existe
        Role role = getRoleOrThrow(bulkAssignRoleDto.getRoleId());

        List<UserRoleDto> results = new ArrayList<>();

        for (UUID userId : bulkAssignRoleDto.getUserIds()) {
            try {
                // Vérifier que l'utilisateur existe
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

                // Vérifier si l'utilisateur n'a pas déjà ce rôle
                if (!userRoleRepository.hasActiveRole(userId, bulkAssignRoleDto.getRoleId())) {
                    UserRole userRole = new UserRole(
                        userId,
                        bulkAssignRoleDto.getRoleId(),
                        bulkAssignRoleDto.getOrganizationId(),
                        bulkAssignRoleDto.getAgencyId()
                    );

                    userRole.setExpiresAt(bulkAssignRoleDto.getExpiresAt());
                    userRole.setAssignmentReason(bulkAssignRoleDto.getAssignmentReason());
                    userRole.setAssignedBy(assignedBy);

                    UserRole savedUserRole = userRoleRepository.save(userRole);
                    results.add(mapToUserRoleDto(savedUserRole, role, user));
                }
            } catch (Exception e) {
                log.error("Error assigning role to user {}: {}", userId, e.getMessage());
                // Continue with other users
            }
        }

        log.info("Bulk assignment completed: {} assignments created", results.size());
        return results;
    }

    /**
     * Récupère les rôles d'un utilisateur
     */
    public List<UserRoleDto> getUserRoles(UUID userId) {
        log.debug("Fetching roles for user: {}", userId);

        List<UserRole> userRoles = userRoleRepository.findActiveByUserId(userId);
        List<UserRoleDto> result = new ArrayList<>();

        for (UserRole userRole : userRoles) {
            try {
                Role role = roleRepository.findById(userRole.getRoleId()).orElse(null);
                User user = userRepository.findById(userRole.getUserId()).orElse(null);
                if (role != null && user != null) {
                    result.add(mapToUserRoleDto(userRole, role, user));
                }
            } catch (Exception e) {
                log.error("Error mapping user role {}: {}", userRole.getId(), e.getMessage());
            }
        }

        return result;
    }

    /**
     * Récupère les permissions effectives d'un utilisateur
     */
    public UserPermissionsDto getUserEffectivePermissions(UUID userId, UUID organizationId) {
        log.debug("Calculating effective permissions for user {} in organization {}", userId, organizationId);

        List<UserRole> userRoles = userRoleRepository.findValidByUserId(userId, LocalDateTime.now());
        Set<String> effectivePermissions = new HashSet<>();
        List<RoleDto> assignedRoles = new ArrayList<>();

        for (UserRole userRole : userRoles) {
            if (userRole.getOrganizationId().equals(organizationId)) {
                try {
                    Role role = roleRepository.findById(userRole.getRoleId()).orElse(null);
                    if (role != null && Boolean.TRUE.equals(role.getIsActive())) {
                        effectivePermissions.addAll(role.getPermissions());
                        assignedRoles.add(mapToRoleDto(role));
                    }
                } catch (Exception e) {
                    log.error("Error processing role {}: {}", userRole.getRoleId(), e.getMessage());
                }
            }
        }

        // Grouper les permissions par ressource
        List<PermissionGroupDto> permissionGroups = groupPermissionsByResource(effectivePermissions);

        // Déterminer le niveau d'accès
        String accessLevel = determineAccessLevel(effectivePermissions);
        boolean hasFullAccess = effectivePermissions.contains(Permission.SYSTEM_ADMIN.getCode());

        return UserPermissionsDto.builder()
            .userId(userId)
            .organizationId(organizationId)
            .effectivePermissions(effectivePermissions)
            .assignedRoles(assignedRoles)
            .permissionGroups(permissionGroups)
            .hasFullAccess(hasFullAccess)
            .accessLevel(accessLevel)
            .build();
    }

    /**
     * Met à jour les permissions d'un rôle
     */
    @Transactional
    public RoleDto updateRolePermissions(RolePermissionsDto rolePermissionsDto, UUID updatedBy) {
        log.info("Updating permissions for role: {}", rolePermissionsDto.getRoleId());

        Role role = getRoleOrThrow(rolePermissionsDto.getRoleId());

        // Vérifier que le rôle peut être modifié
        if (!role.canBeModified()) {
            throw new IllegalStateException("System roles cannot be modified");
        }

        // Valider les permissions
        validatePermissions(rolePermissionsDto.getPermissions());

        // Mettre à jour les permissions
        role.setPermissions(new HashSet<>(rolePermissionsDto.getPermissions()));
        role.setUpdatedBy(updatedBy);
        role.setUpdatedAt(LocalDateTime.now());

        Role savedRole = roleRepository.save(role);
        log.info("Role permissions updated successfully");

        return mapToRoleDto(savedRole);
    }

    /**
     * Récupère toutes les permissions disponibles groupées par ressource
     */
    public List<PermissionGroupDto> getAllPermissionsGrouped() {
        log.debug("Fetching all permissions grouped by resource");

        Map<String, List<Permission>> permissionsByResource = Arrays.stream(Permission.values())
            .collect(Collectors.groupingBy(Permission::getResource));

        return permissionsByResource.entrySet().stream()
            .map(entry -> {
                String resource = entry.getKey();
                List<Permission> permissions = entry.getValue();

                List<PermissionDto> permissionDtos = permissions.stream()
                    .map(permission -> PermissionDto.builder()
                        .code(permission.getCode())
                        .description(permission.getDescription())
                        .resource(permission.getResource())
                        .category(permission.getResource())
                        .isAssigned(false)
                        .build())
                    .collect(Collectors.toList());

                return PermissionGroupDto.builder()
                    .resource(resource)
                    .resourceLabel(getResourceLabel(resource))
                    .description(getResourceDescription(resource))
                    .permissions(permissionDtos)
                    .totalPermissions(permissions.size())
                    .assignedPermissions(0)
                    .build();
            })
            .sorted(Comparator.comparing(PermissionGroupDto::getResourceLabel))
            .collect(Collectors.toList());
    }

    /**
     * Récupère les statistiques des rôles pour une organisation
     */
    public RoleStatsDto getRoleStats(UUID organizationId) {
        log.debug("Calculating role statistics for organization: {}", organizationId);

        List<Role> allRoles = roleRepository.findByOrganizationId(organizationId);
        List<UserRole> allUserRoles = userRoleRepository.findActiveByOrganizationId(organizationId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        long totalRoles = allRoles.size();
        long activeRoles = allRoles.stream().mapToLong(role -> Boolean.TRUE.equals(role.getIsActive()) ? 1 : 0).sum();
        long inactiveRoles = totalRoles - activeRoles;
        long systemRoles = allRoles.stream().mapToLong(role -> Boolean.TRUE.equals(role.getIsSystemRole()) ? 1 : 0).sum();
        long customRoles = totalRoles - systemRoles;
        long defaultRoles = allRoles.stream().mapToLong(role -> Boolean.TRUE.equals(role.getIsDefaultRole()) ? 1 : 0).sum();

        long totalUserRoles = allUserRoles.size();
        long expiredUserRoles = allUserRoles.stream()
            .mapToLong(ur -> ur.isExpired() ? 1 : 0).sum();
        long expiringSoonUserRoles = allUserRoles.stream()
            .mapToLong(ur -> ur.isExpiringSoon(7) ? 1 : 0).sum();

        long rolesCreatedThisPeriod = allRoles.stream()
            .mapToLong(role -> role.getCreatedAt().isAfter(thirtyDaysAgo) ? 1 : 0).sum();

        double averagePermissionsPerRole = allRoles.stream()
            .mapToInt(role -> role.getPermissions() != null ? role.getPermissions().size() : 0)
            .average().orElse(0.0);

        return RoleStatsDto.builder()
            .totalRoles(totalRoles)
            .activeRoles(activeRoles)
            .inactiveRoles(inactiveRoles)
            .systemRoles(systemRoles)
            .customRoles(customRoles)
            .defaultRoles(defaultRoles)
            .totalUserRoles(totalUserRoles)
            .activeUserRoles(totalUserRoles - expiredUserRoles)
            .expiredUserRoles(expiredUserRoles)
            .expiringSoonUserRoles(expiringSoonUserRoles)
            .periodStart(thirtyDaysAgo)
            .periodEnd(now)
            .rolesCreatedThisPeriod(rolesCreatedThisPeriod)
            .averagePermissionsPerRole(averagePermissionsPerRole)
            .build();
    }

    /**
     * Nettoie les assignations expirées
     */
    @Transactional
    public void cleanupExpiredAssignments() {
        log.info("Cleaning up expired role assignments");

        List<UserRole> expiredAssignments = userRoleRepository.findExpired(LocalDateTime.now());

        for (UserRole userRole : expiredAssignments) {
            userRole.revoke();
            userRoleRepository.save(userRole);
        }

        log.info("Cleaned up {} expired role assignments", expiredAssignments.size());
    }

    /**
     * Crée des rôles par défaut pour une nouvelle organisation
     */
    @Transactional
    public List<RoleDto> createDefaultRoles(UUID organizationId, UUID createdBy) {
        log.info("Creating default roles for organization: {}", organizationId);

        List<RoleTemplateDto> templates = getDefaultRoleTemplates();
        List<RoleDto> createdRoles = new ArrayList<>();

        for (RoleTemplateDto template : templates) {
            try {
                CreateRoleDto createRoleDto = new CreateRoleDto();
                createRoleDto.setName(template.getName());
                createRoleDto.setDescription(template.getDescription());
                createRoleDto.setOrganizationId(organizationId);
                createRoleDto.setRoleType(template.getRoleType());
                createRoleDto.setPermissions(template.getDefaultPermissions());
                createRoleDto.setPriority(template.getDefaultPriority());
                createRoleDto.setColor(template.getDefaultColor());
                createRoleDto.setIcon(template.getDefaultIcon());
                createRoleDto.setIsDefaultRole(true);

                RoleDto createdRole = createRole(createRoleDto, createdBy);
                createdRoles.add(createdRole);
            } catch (Exception e) {
                log.error("Error creating default role {}: {}", template.getName(), e.getMessage());
            }
        }

        log.info("Created {} default roles for organization: {}", createdRoles.size(), organizationId);
        return createdRoles;
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private Role getRoleOrThrow(UUID roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
    }

    private void validatePermissions(Set<String> permissions) {
        if (permissions == null) return;

        Set<String> validPermissions = Arrays.stream(Permission.values())
            .map(Permission::getCode)
            .collect(Collectors.toSet());

        for (String permission : permissions) {
            if (!validPermissions.contains(permission)) {
                throw new IllegalArgumentException("Invalid permission: " + permission);
            }
        }
    }

    private RoleDto mapToRoleDto(Role role) {
        Long userCount = userRoleRepository.countActiveByRoleId(role.getId());

        return RoleDto.builder()
            .id(role.getId())
            .name(role.getName())
            .description(role.getDescription())
            .organizationId(role.getOrganizationId())
            .roleType(role.getRoleType())
            .isSystemRole(role.getIsSystemRole())
            .isDefaultRole(role.getIsDefaultRole())
            .isActive(role.getIsActive())
            .priority(role.getPriority())
            .permissions(role.getPermissions())
            .color(role.getColor())
            .icon(role.getIcon())
            .createdAt(role.getCreatedAt())
            .updatedAt(role.getUpdatedAt())
            .createdBy(role.getCreatedBy())
            .updatedBy(role.getUpdatedBy())
            .permissionCount(role.getPermissionCount())
            .userCount(userCount.intValue())
            .summary(role.getSummary())
            .build();
    }

    private UserRoleDto mapToUserRoleDto(UserRole userRole, Role role, User user) {
        return UserRoleDto.builder()
            .id(userRole.getId())
            .userId(userRole.getUserId())
            .roleId(userRole.getRoleId())
            .organizationId(userRole.getOrganizationId())
            .agencyId(userRole.getAgencyId())
            .assignedAt(userRole.getAssignedAt())
            .expiresAt(userRole.getExpiresAt())
            .isActive(userRole.getIsActive())
            .assignmentReason(userRole.getAssignmentReason())
            .assignedBy(userRole.getAssignedBy())
            .roleName(role.getName())
            .roleDescription(role.getDescription())
            .roleColor(role.getColor())
            .roleIcon(role.getIcon())
            .userFullName(user.getFullName())
            .userEmail(user.getEmail())
            .isExpired(userRole.isExpired())
            .daysUntilExpiration(userRole.getDaysUntilExpiration())
            .isExpiringSoon(userRole.isExpiringSoon(7))
            .build();
    }

    private List<PermissionGroupDto> groupPermissionsByResource(Set<String> userPermissions) {
        Map<String, List<Permission>> permissionsByResource = Arrays.stream(Permission.values())
            .collect(Collectors.groupingBy(Permission::getResource));

        return permissionsByResource.entrySet().stream()
            .map(entry -> {
                String resource = entry.getKey();
                List<Permission> permissions = entry.getValue();

                List<PermissionDto> permissionDtos = permissions.stream()
                    .map(permission -> PermissionDto.builder()
                        .code(permission.getCode())
                        .description(permission.getDescription())
                        .resource(permission.getResource())
                        .category(permission.getResource())
                        .isAssigned(userPermissions.contains(permission.getCode()))
                        .build())
                    .collect(Collectors.toList());

                long assignedCount = permissionDtos.stream()
                    .mapToLong(p -> Boolean.TRUE.equals(p.getIsAssigned()) ? 1 : 0)
                    .sum();

                return PermissionGroupDto.builder()
                    .resource(resource)
                    .resourceLabel(getResourceLabel(resource))
                    .description(getResourceDescription(resource))
                    .permissions(permissionDtos)
                    .totalPermissions(permissions.size())
                    .assignedPermissions((int) assignedCount)
                    .build();
            })
            .sorted(Comparator.comparing(PermissionGroupDto::getResourceLabel))
            .collect(Collectors.toList());
    }

    private String determineAccessLevel(Set<String> permissions) {
        if (permissions.contains(Permission.SYSTEM_ADMIN.getCode())) {
            return "ADMIN";
        } else if (permissions.contains(Permission.ORGANIZATION_UPDATE.getCode())) {
            return "MANAGER";
        } else if (permissions.size() > 10) {
            return "USER";
        } else {
            return "LIMITED";
        }
    }

    private String getResourceLabel(String resource) {
        return switch (resource.toUpperCase()) {
            case "VEHICLE" -> "Véhicules";
            case "DRIVER" -> "Chauffeurs";
            case "RENTAL" -> "Locations";
            case "USER" -> "Utilisateurs";
            case "AGENCY" -> "Agences";
            case "ORGANIZATION" -> "Organisation";
            case "ROLE" -> "Rôles";
            case "PAYMENT" -> "Paiements";
            case "REPORT" -> "Rapports";
            case "SETTINGS" -> "Paramètres";
            case "SYSTEM" -> "Système";
            default -> resource;
        };
    }

    private String getResourceDescription(String resource) {
        return switch (resource.toUpperCase()) {
            case "VEHICLE" -> "Gestion des véhicules de la flotte";
            case "DRIVER" -> "Gestion des chauffeurs";
            case "RENTAL" -> "Gestion des locations et réservations";
            case "USER" -> "Gestion des utilisateurs";
            case "AGENCY" -> "Gestion des agences";
            case "ORGANIZATION" -> "Gestion de l'organisation";
            case "ROLE" -> "Gestion des rôles et permissions";
            case "PAYMENT" -> "Gestion des paiements et transactions";
            case "REPORT" -> "Génération et consultation des rapports";
            case "SETTINGS" -> "Configuration des paramètres";
            case "SYSTEM" -> "Administration système";
            default -> "Permissions pour " + resource.toLowerCase();
        };
    }

    private List<RoleTemplateDto> getDefaultRoleTemplates() {
        return Arrays.asList(
            RoleTemplateDto.builder()
                .roleType(RoleType.ORGANIZATION_OWNER)
                .name("Propriétaire")
                .description("Propriétaire de l'organisation avec tous les droits")
                .defaultPermissions(Set.of(
                    Permission.ORGANIZATION_READ.getCode(),
                    Permission.ORGANIZATION_UPDATE.getCode(),
                    Permission.ORGANIZATION_MANAGE_SETTINGS.getCode(),
                    Permission.AGENCY_READ.getCode(),
                    Permission.AGENCY_WRITE.getCode(),
                    Permission.AGENCY_UPDATE.getCode(),
                    Permission.AGENCY_DELETE.getCode(),
                    Permission.ROLE_READ.getCode(),
                    Permission.ROLE_WRITE.getCode(),
                    Permission.ROLE_UPDATE.getCode(),
                    Permission.ROLE_DELETE.getCode(),
                    Permission.USER_READ.getCode(),
                    Permission.USER_WRITE.getCode(),
                    Permission.USER_UPDATE.getCode(),
                    Permission.USER_MANAGE_ROLES.getCode()
                ))
                .defaultPriority(100)
                .defaultColor("#FF6B35")
                .defaultIcon("crown")
                .isRecommended(true)
                .build(),

            RoleTemplateDto.builder()
                .roleType(RoleType.AGENCY_MANAGER)
                .name("Gestionnaire d'Agence")
                .description("Gestionnaire avec droits complets sur son agence")
                .defaultPermissions(Set.of(
                    Permission.VEHICLE_READ.getCode(),
                    Permission.VEHICLE_WRITE.getCode(),
                    Permission.VEHICLE_UPDATE.getCode(),
                    Permission.DRIVER_READ.getCode(),
                    Permission.DRIVER_WRITE.getCode(),
                    Permission.DRIVER_UPDATE.getCode(),
                    Permission.RENTAL_READ.getCode(),
                    Permission.RENTAL_WRITE.getCode(),
                    Permission.RENTAL_UPDATE.getCode(),
                    Permission.RENTAL_APPROVE.getCode(),
                    Permission.USER_READ.getCode(),
                    Permission.REPORT_READ.getCode(),
                    Permission.REPORT_GENERATE.getCode()
                ))
                .defaultPriority(80)
                .defaultColor("#4ECDC4")
                .defaultIcon("building")
                .isRecommended(true)
                .build(),

            RoleTemplateDto.builder()
                .roleType(RoleType.RENTAL_AGENT)
                .name("Agent de Location")
                .description("Agent responsable des locations")
                .defaultPermissions(Set.of(
                    Permission.VEHICLE_READ.getCode(),
                    Permission.DRIVER_READ.getCode(),
                    Permission.RENTAL_READ.getCode(),
                    Permission.RENTAL_WRITE.getCode(),
                    Permission.RENTAL_UPDATE.getCode(),
                    Permission.USER_READ.getCode()
                ))
                .defaultPriority(50)
                .defaultColor("#45B7D1")
                .defaultIcon("car")
                .isRecommended(true)
                .build(),

            RoleTemplateDto.builder()
                .roleType(RoleType.CLIENT)
                .name("Client")
                .description("Client final avec accès limité")
                .defaultPermissions(Set.of(
                    Permission.VEHICLE_READ.getCode(),
                    Permission.RENTAL_READ.getCode()
                ))
                .defaultPriority(10)
                .defaultColor("#96CEB4")
                .defaultIcon("user")
                .isRecommended(true)
                .build()
        );
    }
}
