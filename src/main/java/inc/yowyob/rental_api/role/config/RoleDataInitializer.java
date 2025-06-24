package inc.yowyob.rental_api.role.config;

import inc.yowyob.rental_api.core.enums.Permission;
import inc.yowyob.rental_api.core.enums.RoleType;
import inc.yowyob.rental_api.role.entities.Role;
import inc.yowyob.rental_api.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Initialise les rôles système par défaut
 */
@Slf4j
@Component
@Order(3) // Après UserDataInitializer
@RequiredArgsConstructor
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing system roles data...");

        try {
            createSystemRoles();
            log.info("System roles initialization completed successfully.");

        } catch (Exception e) {
            log.error("Error during system roles initialization: {}", e.getMessage(), e);
        }
    }

    private void createSystemRoles() {
        log.info("Creating system roles...");

        try {
            // Créer le rôle Super Admin système
            createSuperAdminRole();

            log.info("System roles creation completed successfully.");

        } catch (Exception e) {
            log.error("Error creating system roles: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void createSuperAdminRole() {
        // Vérifier si le rôle Super Admin existe déjà
        if (roleRepository.findSystemRoles().stream()
            .anyMatch(role -> RoleType.SUPER_ADMIN.equals(role.getRoleType()))) {
            log.info("Super Admin system role already exists. Skipping creation.");
            return;
        }

        log.info("Creating Super Admin system role...");

        // ID d'organisation système (null ou UUID spécial pour les rôles système)
        UUID systemOrgId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        Role superAdminRole = new Role(
            "Super Administrateur",
            "Administrateur système avec tous les droits sur la plateforme",
            systemOrgId,
            RoleType.SUPER_ADMIN
        );

        // Marquer comme rôle système
        superAdminRole.setIsSystemRole(true);
        superAdminRole.setIsDefaultRole(false);
        superAdminRole.setPriority(1000); // Priorité maximale
        superAdminRole.setColor("#FF0000"); // Rouge pour indiquer le niveau élevé
        superAdminRole.setIcon("shield-check");

        // Ajouter TOUTES les permissions système
        Set<String> allPermissions = Set.of(
            // Permissions système complètes
            Permission.SYSTEM_ADMIN.getCode(),
            Permission.SYSTEM_BACKUP.getCode(),
            Permission.SYSTEM_LOGS.getCode(),
            Permission.SYSTEM_MONITORING.getCode(),

            // Permissions organisations (pour gérer toutes les organisations)
            Permission.ORGANIZATION_READ.getCode(),
            Permission.ORGANIZATION_UPDATE.getCode(),
            Permission.ORGANIZATION_MANAGE_SETTINGS.getCode(),
            Permission.ORGANIZATION_MANAGE_SUBSCRIPTION.getCode(),

            // Permissions agences
            Permission.AGENCY_READ.getCode(),
            Permission.AGENCY_WRITE.getCode(),
            Permission.AGENCY_UPDATE.getCode(),
            Permission.AGENCY_DELETE.getCode(),
            Permission.AGENCY_MANAGE_STAFF.getCode(),

            // Permissions utilisateurs
            Permission.USER_READ.getCode(),
            Permission.USER_WRITE.getCode(),
            Permission.USER_UPDATE.getCode(),
            Permission.USER_DELETE.getCode(),
            Permission.USER_MANAGE_ROLES.getCode(),
            Permission.USER_RESET_PASSWORD.getCode(),

            // Permissions rôles
            Permission.ROLE_READ.getCode(),
            Permission.ROLE_WRITE.getCode(),
            Permission.ROLE_UPDATE.getCode(),
            Permission.ROLE_DELETE.getCode(),
            Permission.ROLE_ASSIGN_PERMISSIONS.getCode(),

            // Permissions véhicules
            Permission.VEHICLE_READ.getCode(),
            Permission.VEHICLE_WRITE.getCode(),
            Permission.VEHICLE_UPDATE.getCode(),
            Permission.VEHICLE_DELETE.getCode(),
            Permission.VEHICLE_MANAGE_IMAGES.getCode(),
            Permission.VEHICLE_CHANGE_STATUS.getCode(),

            // Permissions chauffeurs
            Permission.DRIVER_READ.getCode(),
            Permission.DRIVER_WRITE.getCode(),
            Permission.DRIVER_UPDATE.getCode(),
            Permission.DRIVER_DELETE.getCode(),
            Permission.DRIVER_MANAGE_DOCUMENTS.getCode(),
            Permission.DRIVER_MANAGE_SCHEDULE.getCode(),

            // Permissions locations
            Permission.RENTAL_READ.getCode(),
            Permission.RENTAL_WRITE.getCode(),
            Permission.RENTAL_UPDATE.getCode(),
            Permission.RENTAL_DELETE.getCode(),
            Permission.RENTAL_APPROVE.getCode(),
            Permission.RENTAL_CANCEL.getCode(),
            Permission.RENTAL_EXTEND.getCode(),

            // Permissions paiements
            Permission.PAYMENT_READ.getCode(),
            Permission.PAYMENT_PROCESS.getCode(),
            Permission.PAYMENT_REFUND.getCode(),
            Permission.PAYMENT_VIEW_DETAILS.getCode(),

            // Permissions rapports
            Permission.REPORT_READ.getCode(),
            Permission.REPORT_GENERATE.getCode(),
            Permission.REPORT_EXPORT.getCode(),
            Permission.REPORT_ADVANCED.getCode(),

            // Permissions paramètres
            Permission.SETTINGS_READ.getCode(),
            Permission.SETTINGS_WRITE.getCode(),
            Permission.SETTINGS_MANAGE_NOTIFICATIONS.getCode()
        );

        superAdminRole.setPermissions(allPermissions);
        superAdminRole.setCreatedAt(LocalDateTime.now());
        superAdminRole.setUpdatedAt(LocalDateTime.now());

        roleRepository.save(superAdminRole);
        log.info("Super Admin system role created successfully with {} permissions", allPermissions.size());
    }
}
