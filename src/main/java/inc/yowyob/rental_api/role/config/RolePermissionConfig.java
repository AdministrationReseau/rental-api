package inc.yowyob.rental_api.role.config;

import inc.yowyob.rental_api.core.enums.Permission;
import inc.yowyob.rental_api.core.enums.RoleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

/**
 * Configuration des permissions par défaut pour les différents types de rôles
 */
@Slf4j
@Configuration
public class RolePermissionConfig {

    /**
     * Définit les permissions par défaut pour chaque type de rôle
     */
    @Bean
    public Map<RoleType, Set<String>> defaultRolePermissions() {
        return Map.ofEntries(
            Map.entry(RoleType.SUPER_ADMIN, getSuperAdminPermissions()),
            Map.entry(RoleType.ORGANIZATION_OWNER, getOrganizationOwnerPermissions()),
            Map.entry(RoleType.ORGANIZATION_ADMIN, getOrganizationAdminPermissions()),
            Map.entry(RoleType.AGENCY_MANAGER, getAgencyManagerPermissions()),
            Map.entry(RoleType.AGENCY_SUPERVISOR, getAgencySupervisorPermissions()),
            Map.entry(RoleType.RENTAL_AGENT, getRentalAgentPermissions()),
            Map.entry(RoleType.DRIVER_MANAGER, getDriverManagerPermissions()),
            Map.entry(RoleType.VEHICLE_MANAGER, getVehicleManagerPermissions()),
            Map.entry(RoleType.CLIENT, getClientPermissions()),
            Map.entry(RoleType.VIP_CLIENT, getVipClientPermissions()),
            Map.entry(RoleType.RECEPTIONIST, getReceptionistPermissions()),
            Map.entry(RoleType.MECHANIC, getMechanicPermissions()),
            Map.entry(RoleType.DRIVER, getDriverPermissions()),
            Map.entry(RoleType.ACCOUNTANT, getAccountantPermissions()),
            Map.entry(RoleType.PAYMENT_MANAGER, getPaymentManagerPermissions())
        );
    }

    private Set<String> getSuperAdminPermissions() {
        // Toutes les permissions système
        return Set.of(
            Permission.SYSTEM_ADMIN.getCode(),
            Permission.SYSTEM_BACKUP.getCode(),
            Permission.SYSTEM_LOGS.getCode(),
            Permission.SYSTEM_MONITORING.getCode()
            // + toutes les autres permissions...
        );
    }

    private Set<String> getOrganizationOwnerPermissions() {
        return Set.of(
            // Gestion complète de l'organisation
            Permission.ORGANIZATION_READ.getCode(),
            Permission.ORGANIZATION_UPDATE.getCode(),
            Permission.ORGANIZATION_MANAGE_SETTINGS.getCode(),
            Permission.ORGANIZATION_MANAGE_SUBSCRIPTION.getCode(),

            // Gestion complète des agences
            Permission.AGENCY_READ.getCode(),
            Permission.AGENCY_WRITE.getCode(),
            Permission.AGENCY_UPDATE.getCode(),
            Permission.AGENCY_DELETE.getCode(),
            Permission.AGENCY_MANAGE_STAFF.getCode(),

            // Gestion complète des utilisateurs
            Permission.USER_READ.getCode(),
            Permission.USER_WRITE.getCode(),
            Permission.USER_UPDATE.getCode(),
            Permission.USER_DELETE.getCode(),
            Permission.USER_MANAGE_ROLES.getCode(),
            Permission.USER_RESET_PASSWORD.getCode(),

            // Gestion complète des rôles
            Permission.ROLE_READ.getCode(),
            Permission.ROLE_WRITE.getCode(),
            Permission.ROLE_UPDATE.getCode(),
            Permission.ROLE_DELETE.getCode(),
            Permission.ROLE_ASSIGN_PERMISSIONS.getCode(),

            // Accès complet aux rapports
            Permission.REPORT_READ.getCode(),
            Permission.REPORT_GENERATE.getCode(),
            Permission.REPORT_EXPORT.getCode(),
            Permission.REPORT_ADVANCED.getCode(),

            // Gestion des paramètres
            Permission.SETTINGS_READ.getCode(),
            Permission.SETTINGS_WRITE.getCode(),
            Permission.SETTINGS_MANAGE_NOTIFICATIONS.getCode()
        );
    }

    private Set<String> getOrganizationAdminPermissions() {
        return Set.of(
            // Lecture de l'organisation
            Permission.ORGANIZATION_READ.getCode(),
            Permission.ORGANIZATION_MANAGE_SETTINGS.getCode(),

            // Gestion des agences
            Permission.AGENCY_READ.getCode(),
            Permission.AGENCY_WRITE.getCode(),
            Permission.AGENCY_UPDATE.getCode(),
            Permission.AGENCY_MANAGE_STAFF.getCode(),

            // Gestion des utilisateurs
            Permission.USER_READ.getCode(),
            Permission.USER_WRITE.getCode(),
            Permission.USER_UPDATE.getCode(),
            Permission.USER_MANAGE_ROLES.getCode(),

            // Gestion des rôles (limitée)
            Permission.ROLE_READ.getCode(),
            Permission.ROLE_WRITE.getCode(),
            Permission.ROLE_UPDATE.getCode(),

            // Rapports
            Permission.REPORT_READ.getCode(),
            Permission.REPORT_GENERATE.getCode(),
            Permission.REPORT_EXPORT.getCode(),

            // Paramètres
            Permission.SETTINGS_READ.getCode(),
            Permission.SETTINGS_WRITE.getCode()
        );
    }

    private Set<String> getAgencyManagerPermissions() {
        return Set.of(
            // Lecture agence
            Permission.AGENCY_READ.getCode(),
            Permission.AGENCY_UPDATE.getCode(),

            // Gestion complète des véhicules de l'agence
            Permission.VEHICLE_READ.getCode(),
            Permission.VEHICLE_WRITE.getCode(),
            Permission.VEHICLE_UPDATE.getCode(),
            Permission.VEHICLE_DELETE.getCode(),
            Permission.VEHICLE_MANAGE_IMAGES.getCode(),
            Permission.VEHICLE_CHANGE_STATUS.getCode(),

            // Gestion complète des chauffeurs de l'agence
            Permission.DRIVER_READ.getCode(),
            Permission.DRIVER_WRITE.getCode(),
            Permission.DRIVER_UPDATE.getCode(),
            Permission.DRIVER_DELETE.getCode(),
            Permission.DRIVER_MANAGE_DOCUMENTS.getCode(),
            Permission.DRIVER_MANAGE_SCHEDULE.getCode(),

            // Gestion complète des locations
            Permission.RENTAL_READ.getCode(),
            Permission.RENTAL_WRITE.getCode(),
            Permission.RENTAL_UPDATE.getCode(),
            Permission.RENTAL_DELETE.getCode(),
            Permission.RENTAL_APPROVE.getCode(),
            Permission.RENTAL_CANCEL.getCode(),
            Permission.RENTAL_EXTEND.getCode(),

            // Personnel de l'agence
            Permission.USER_READ.getCode(),
            Permission.USER_WRITE.getCode(),
            Permission.USER_UPDATE.getCode(),

            // Rapports de l'agence
            Permission.REPORT_READ.getCode(),
            Permission.REPORT_GENERATE.getCode(),

            // Paramètres de base
            Permission.SETTINGS_READ.getCode()
        );
    }

    private Set<String> getAgencySupervisorPermissions() {
        return Set.of(
            // Lecture agence
            Permission.AGENCY_READ.getCode(),

            // Gestion des véhicules (sans suppression)
            Permission.VEHICLE_READ.getCode(),
            Permission.VEHICLE_UPDATE.getCode(),
            Permission.VEHICLE_CHANGE_STATUS.getCode(),

            // Gestion des chauffeurs (sans suppression)
            Permission.DRIVER_READ.getCode(),
            Permission.DRIVER_UPDATE.getCode(),
            Permission.DRIVER_MANAGE_SCHEDULE.getCode(),

            // Gestion des locations
            Permission.RENTAL_READ.getCode(),
            Permission.RENTAL_WRITE.getCode(),
            Permission.RENTAL_UPDATE.getCode(),
            Permission.RENTAL_APPROVE.getCode(),
            Permission.RENTAL_CANCEL.getCode(),

            // Personnel (lecture seule)
            Permission.USER_READ.getCode(),

            // Rapports
            Permission.REPORT_READ.getCode(),
            Permission.REPORT_GENERATE.getCode()
        );
    }

    private Set<String> getRentalAgentPermissions() {
        return Set.of(
            // Lecture des véhicules
            Permission.VEHICLE_READ.getCode(),

            // Lecture des chauffeurs
            Permission.DRIVER_READ.getCode(),

            // Gestion des locations
            Permission.RENTAL_READ.getCode(),
            Permission.RENTAL_WRITE.getCode(),
            Permission.RENTAL_UPDATE.getCode(),

            // Clients
            Permission.USER_READ.getCode()
        );
    }

    private Set<String> getDriverManagerPermissions() {
        return Set.of(
            // Gestion complète des chauffeurs
            Permission.DRIVER_READ.getCode(),
            Permission.DRIVER_WRITE.getCode(),
            Permission.DRIVER_UPDATE.getCode(),
            Permission.DRIVER_DELETE.getCode(),
            Permission.DRIVER_MANAGE_DOCUMENTS.getCode(),
            Permission.DRIVER_MANAGE_SCHEDULE.getCode(),

            // Lecture des véhicules pour assignation
            Permission.VEHICLE_READ.getCode(),

            // Gestion des locations liées aux chauffeurs
            Permission.RENTAL_READ.getCode(),
            Permission.RENTAL_UPDATE.getCode(),

            // Rapports chauffeurs
            Permission.REPORT_READ.getCode(),
            Permission.REPORT_GENERATE.getCode()
        );
    }

    private Set<String> getVehicleManagerPermissions() {
        return Set.of(
            // Gestion complète des véhicules
            Permission.VEHICLE_READ.getCode(),
            Permission.VEHICLE_WRITE.getCode(),
            Permission.VEHICLE_UPDATE.getCode(),
            Permission.VEHICLE_DELETE.getCode(),
            Permission.VEHICLE_MANAGE_IMAGES.getCode(),
            Permission.VEHICLE_CHANGE_STATUS.getCode(),

            // Lecture des chauffeurs pour assignation
            Permission.DRIVER_READ.getCode(),

            // Lecture des locations
            Permission.RENTAL_READ.getCode(),

            // Rapports véhicules
            Permission.REPORT_READ.getCode(),
            Permission.REPORT_GENERATE.getCode()
        );
    }

    private Set<String> getClientPermissions() {
        return Set.of(
            // Lecture des véhicules disponibles
            Permission.VEHICLE_READ.getCode(),

            // Lecture des chauffeurs disponibles
            Permission.DRIVER_READ.getCode(),

            // Gestion de ses propres locations
            Permission.RENTAL_READ.getCode(),
            Permission.RENTAL_WRITE.getCode()
        );
    }

    private Set<String> getVipClientPermissions() {
        return Set.of(
            // Toutes les permissions client
            Permission.VEHICLE_READ.getCode(),
            Permission.DRIVER_READ.getCode(),
            Permission.RENTAL_READ.getCode(),
            Permission.RENTAL_WRITE.getCode(),

            // Permissions supplémentaires VIP
            Permission.RENTAL_EXTEND.getCode(), // Peut prolonger ses locations
            Permission.REPORT_READ.getCode()    // Peut voir ses rapports de location
        );
    }

    private Set<String> getReceptionistPermissions() {
        return Set.of(
            // Lecture des véhicules
            Permission.VEHICLE_READ.getCode(),

            // Lecture des chauffeurs
            Permission.DRIVER_READ.getCode(),

            // Gestion des locations (accueil client)
            Permission.RENTAL_READ.getCode(),
            Permission.RENTAL_WRITE.getCode(),
            Permission.RENTAL_UPDATE.getCode(),

            // Gestion des clients
            Permission.USER_READ.getCode(),
            Permission.USER_WRITE.getCode()
        );
    }

    private Set<String> getMechanicPermissions() {
        return Set.of(
            // Lecture et mise à jour des véhicules (maintenance)
            Permission.VEHICLE_READ.getCode(),
            Permission.VEHICLE_UPDATE.getCode(),
            Permission.VEHICLE_CHANGE_STATUS.getCode(),

            // Lecture des locations (pour planifier maintenance)
            Permission.RENTAL_READ.getCode()
        );
    }

    private Set<String> getDriverPermissions() {
        return Set.of(
            // Lecture des véhicules assignés
            Permission.VEHICLE_READ.getCode(),

            // Lecture de ses informations
            Permission.DRIVER_READ.getCode(),

            // Lecture de ses locations
            Permission.RENTAL_READ.getCode()
        );
    }

    private Set<String> getAccountantPermissions() {
        return Set.of(
            // Lecture pour facturation
            Permission.RENTAL_READ.getCode(),
            Permission.USER_READ.getCode(),
            Permission.VEHICLE_READ.getCode(),
            Permission.DRIVER_READ.getCode(),

            // Gestion complète des paiements
            Permission.PAYMENT_READ.getCode(),
            Permission.PAYMENT_PROCESS.getCode(),
            Permission.PAYMENT_REFUND.getCode(),
            Permission.PAYMENT_VIEW_DETAILS.getCode(),

            // Rapports financiers
            Permission.REPORT_READ.getCode(),
            Permission.REPORT_GENERATE.getCode(),
            Permission.REPORT_EXPORT.getCode(),
            Permission.REPORT_ADVANCED.getCode()
        );
    }

    private Set<String> getPaymentManagerPermissions() {
        return Set.of(
            // Lecture pour paiements
            Permission.RENTAL_READ.getCode(),
            Permission.USER_READ.getCode(),

            // Gestion complète des paiements
            Permission.PAYMENT_READ.getCode(),
            Permission.PAYMENT_PROCESS.getCode(),
            Permission.PAYMENT_REFUND.getCode(),
            Permission.PAYMENT_VIEW_DETAILS.getCode(),

            // Rapports paiements
            Permission.REPORT_READ.getCode(),
            Permission.REPORT_GENERATE.getCode()
        );
    }

    /**
     * Obtient les descriptions des types de rôles pour l'interface utilisateur
     */
    @Bean
    public Map<RoleType, String> roleTypeDescriptions() {
        return Map.ofEntries(
            Map.entry(RoleType.SUPER_ADMIN, "Administrateur système avec accès complet à toute la plateforme"),
            Map.entry(RoleType.ORGANIZATION_OWNER, "Propriétaire de l'organisation avec droits complets sur son organisation"),
            Map.entry(RoleType.ORGANIZATION_ADMIN, "Administrateur d'organisation avec droits étendus"),
            Map.entry(RoleType.AGENCY_MANAGER, "Gestionnaire d'agence avec droits complets sur son agence"),
            Map.entry(RoleType.AGENCY_SUPERVISOR, "Superviseur d'agence avec droits de supervision"),
            Map.entry(RoleType.RENTAL_AGENT, "Agent de location responsable des réservations clients"),
            Map.entry(RoleType.DRIVER_MANAGER, "Gestionnaire spécialisé dans la gestion des chauffeurs"),
            Map.entry(RoleType.VEHICLE_MANAGER, "Gestionnaire spécialisé dans la gestion des véhicules"),
            Map.entry(RoleType.CLIENT, "Client standard avec accès aux fonctionnalités de location"),
            Map.entry(RoleType.VIP_CLIENT, "Client VIP avec privilèges étendus"),
            Map.entry(RoleType.RECEPTIONIST, "Réceptionniste gérant l'accueil et les premières interactions"),
            Map.entry(RoleType.MECHANIC, "Mécanicien responsable de la maintenance des véhicules"),
            Map.entry(RoleType.DRIVER, "Chauffeur avec accès limité aux informations nécessaires"),
            Map.entry(RoleType.ACCOUNTANT, "Comptable gérant les aspects financiers"),
            Map.entry(RoleType.PAYMENT_MANAGER, "Gestionnaire spécialisé dans les paiements")
        );
    }

    /**
     * Obtient les couleurs par défaut pour chaque type de rôle
     */
    @Bean
    public Map<RoleType, String> roleTypeColors() {
        return Map.ofEntries(
            Map.entry(RoleType.SUPER_ADMIN, "#FF0000"),
            Map.entry(RoleType.ORGANIZATION_OWNER, "#FF6B35"),
            Map.entry(RoleType.ORGANIZATION_ADMIN, "#FF8C42"),
            Map.entry(RoleType.AGENCY_MANAGER, "#4ECDC4"),
            Map.entry(RoleType.AGENCY_SUPERVISOR, "#45B7D1"),
            Map.entry(RoleType.RENTAL_AGENT, "#96CEB4"),
            Map.entry(RoleType.DRIVER_MANAGER, "#FECA57"),
            Map.entry(RoleType.VEHICLE_MANAGER, "#FF9FF3"),
            Map.entry(RoleType.CLIENT, "#A8E6CF"),
            Map.entry(RoleType.VIP_CLIENT, "#FFD93D"),
            Map.entry(RoleType.RECEPTIONIST, "#88D8C0"),
            Map.entry(RoleType.MECHANIC, "#FFA726"),
            Map.entry(RoleType.DRIVER, "#81C784"),
            Map.entry(RoleType.ACCOUNTANT, "#9575CD"),
            Map.entry(RoleType.PAYMENT_MANAGER, "#7986CB")
        );
    }

    /**
     * Obtient les icônes par défaut pour chaque type de rôle
     */
    @Bean
    public Map<RoleType, String> roleTypeIcons() {
        return Map.ofEntries(
            Map.entry(RoleType.SUPER_ADMIN, "shield-check"),
            Map.entry(RoleType.ORGANIZATION_OWNER, "crown"),
            Map.entry(RoleType.ORGANIZATION_ADMIN, "settings"),
            Map.entry(RoleType.AGENCY_MANAGER, "building"),
            Map.entry(RoleType.AGENCY_SUPERVISOR, "eye"),
            Map.entry(RoleType.RENTAL_AGENT, "clipboard-list"),
            Map.entry(RoleType.DRIVER_MANAGER, "users"),
            Map.entry(RoleType.VEHICLE_MANAGER, "truck"),
            Map.entry(RoleType.CLIENT, "user"),
            Map.entry(RoleType.VIP_CLIENT, "star"),
            Map.entry(RoleType.RECEPTIONIST, "phone"),
            Map.entry(RoleType.MECHANIC, "wrench"),
            Map.entry(RoleType.DRIVER, "steering-wheel"),
            Map.entry(RoleType.ACCOUNTANT, "calculator"),
            Map.entry(RoleType.PAYMENT_MANAGER, "credit-card")
        );
    }

    /**
     * Vérifie la cohérence des permissions configurées
     */
    public boolean validatePermissionConfiguration() {
        Map<RoleType, Set<String>> permissions = defaultRolePermissions();

        log.info("Validating role permission configuration...");

        // Vérifier que tous les codes de permission existent
        Set<String> validPermissions = Set.of(Permission.values())
            .stream()
            .map(Permission::getCode)
            .collect(java.util.stream.Collectors.toSet());

        boolean isValid = true;

        for (Map.Entry<RoleType, Set<String>> entry : permissions.entrySet()) {
            RoleType roleType = entry.getKey();
            Set<String> rolePermissions = entry.getValue();

            for (String permission : rolePermissions) {
                if (!validPermissions.contains(permission)) {
                    log.error("Invalid permission '{}' configured for role type '{}'", permission, roleType);
                    isValid = false;
                }
            }

            log.debug("Role type '{}' has {} permissions configured", roleType, rolePermissions.size());
        }

        if (isValid) {
            log.info("Role permission configuration is valid");
        } else {
            log.error("Role permission configuration contains errors");
        }

        return isValid;
    }
}
