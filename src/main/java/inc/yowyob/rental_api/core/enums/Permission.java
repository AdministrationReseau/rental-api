package inc.yowyob.rental_api.core.enums;

import lombok.Getter;

/**
 * Énumération des permissions granulaires du système
 */
@Getter
public enum Permission {
    // === PERMISSIONS VÉHICULES ===
    VEHICLE_READ("vehicle_read", "Lire les informations des véhicules", "VEHICLE"),
    VEHICLE_WRITE("vehicle_write", "Créer de nouveaux véhicules", "VEHICLE"),
    VEHICLE_UPDATE("vehicle_update", "Modifier les véhicules existants", "VEHICLE"),
    VEHICLE_DELETE("vehicle_delete", "Supprimer des véhicules", "VEHICLE"),
    VEHICLE_MANAGE_IMAGES("vehicle_manage_images", "Gérer les images des véhicules", "VEHICLE"),
    VEHICLE_CHANGE_STATUS("vehicle_change_status", "Changer le statut des véhicules", "VEHICLE"),

    // === PERMISSIONS CHAUFFEURS ===
    DRIVER_READ("driver_read", "Lire les informations des chauffeurs", "DRIVER"),
    DRIVER_WRITE("driver_write", "Créer de nouveaux chauffeurs", "DRIVER"),
    DRIVER_UPDATE("driver_update", "Modifier les chauffeurs existants", "DRIVER"),
    DRIVER_DELETE("driver_delete", "Supprimer des chauffeurs", "DRIVER"),
    DRIVER_MANAGE_DOCUMENTS("driver_manage_documents", "Gérer les documents des chauffeurs", "DRIVER"),
    DRIVER_MANAGE_SCHEDULE("driver_manage_schedule", "Gérer les plannings des chauffeurs", "DRIVER"),

    // === PERMISSIONS LOCATIONS ===
    RENTAL_READ("rental_read", "Lire les informations des locations", "RENTAL"),
    RENTAL_WRITE("rental_write", "Créer de nouvelles locations", "RENTAL"),
    RENTAL_UPDATE("rental_update", "Modifier les locations existantes", "RENTAL"),
    RENTAL_DELETE("rental_delete", "Supprimer des locations", "RENTAL"),
    RENTAL_APPROVE("rental_approve", "Approuver les demandes de location", "RENTAL"),
    RENTAL_CANCEL("rental_cancel", "Annuler des locations", "RENTAL"),
    RENTAL_EXTEND("rental_extend", "Prolonger des locations", "RENTAL"),

    // === PERMISSIONS UTILISATEURS ===
    USER_READ("user_read", "Lire les informations des utilisateurs", "USER"),
    USER_WRITE("user_write", "Créer de nouveaux utilisateurs", "USER"),
    USER_UPDATE("user_update", "Modifier les utilisateurs existants", "USER"),
    USER_DELETE("user_delete", "Supprimer des utilisateurs", "USER"),
    USER_MANAGE_ROLES("user_manage_roles", "Gérer les rôles des utilisateurs", "USER"),
    USER_RESET_PASSWORD("user_reset_password", "Réinitialiser les mots de passe", "USER"),

    // === PERMISSIONS AGENCES ===
    AGENCY_READ("agency_read", "Lire les informations des agences", "AGENCY"),
    AGENCY_WRITE("agency_write", "Créer de nouvelles agences", "AGENCY"),
    AGENCY_UPDATE("agency_update", "Modifier les agences existantes", "AGENCY"),
    AGENCY_DELETE("agency_delete", "Supprimer des agences", "AGENCY"),
    AGENCY_MANAGE_STAFF("agency_manage_staff", "Gérer le personnel des agences", "AGENCY"),

    // === PERMISSIONS ORGANISATIONS ===
    ORGANIZATION_READ("organization_read", "Lire les informations de l'organisation", "ORGANIZATION"),
    ORGANIZATION_UPDATE("organization_update", "Modifier les informations de l'organisation", "ORGANIZATION"),
    ORGANIZATION_MANAGE_SETTINGS("organization_manage_settings", "Gérer les paramètres de l'organisation", "ORGANIZATION"),
    ORGANIZATION_MANAGE_SUBSCRIPTION("organization_manage_subscription", "Gérer l'abonnement de l'organisation", "ORGANIZATION"),

    // === PERMISSIONS RÔLES ===
    ROLE_READ("role_read", "Lire les informations des rôles", "ROLE"),
    ROLE_WRITE("role_write", "Créer de nouveaux rôles", "ROLE"),
    ROLE_UPDATE("role_update", "Modifier les rôles existants", "ROLE"),
    ROLE_DELETE("role_delete", "Supprimer des rôles", "ROLE"),
    ROLE_ASSIGN_PERMISSIONS("role_assign_permissions", "Assigner des permissions aux rôles", "ROLE"),

    // === PERMISSIONS PAIEMENTS ===
    PAYMENT_READ("payment_read", "Lire les informations des paiements", "PAYMENT"),
    PAYMENT_PROCESS("payment_process", "Traiter les paiements", "PAYMENT"),
    PAYMENT_REFUND("payment_refund", "Effectuer des remboursements", "PAYMENT"),
    PAYMENT_VIEW_DETAILS("payment_view_details", "Voir les détails des transactions", "PAYMENT"),

    // === PERMISSIONS RAPPORTS ===
    REPORT_READ("report_read", "Lire les rapports", "REPORT"),
    REPORT_GENERATE("report_generate", "Générer des rapports", "REPORT"),
    REPORT_EXPORT("report_export", "Exporter des rapports", "REPORT"),
    REPORT_ADVANCED("report_advanced", "Accéder aux rapports avancés", "REPORT"),

    // === PERMISSIONS PARAMÈTRES ===
    SETTINGS_READ("settings_read", "Lire les paramètres", "SETTINGS"),
    SETTINGS_WRITE("settings_write", "Modifier les paramètres", "SETTINGS"),
    SETTINGS_MANAGE_NOTIFICATIONS("settings_manage_notifications", "Gérer les notifications", "SETTINGS"),

    // === PERMISSIONS SYSTÈME ===
    SYSTEM_ADMIN("system_admin", "Administration complète du système", "SYSTEM"),
    SYSTEM_BACKUP("system_backup", "Effectuer des sauvegardes", "SYSTEM"),
    SYSTEM_LOGS("system_logs", "Accéder aux logs système", "SYSTEM"),
    SYSTEM_MONITORING("system_monitoring", "Accéder au monitoring", "SYSTEM");

    private final String code;
    private final String description;
    private final String resource;

    Permission(String code, String description, String resource) {
        this.code = code;
        this.description = description;
        this.resource = resource;
    }

    /**
     * Vérifie si la permission concerne une ressource donnée
     */
    public boolean isForResource(String resourceName) {
        return this.resource.equalsIgnoreCase(resourceName);
    }

    /**
     * Obtient toutes les permissions pour une ressource
     */
    public static Permission[] getPermissionsForResource(String resourceName) {
        return java.util.Arrays.stream(Permission.values())
            .filter(permission -> permission.isForResource(resourceName))
            .toArray(Permission[]::new);
    }

    /**
     * Obtient toutes les ressources disponibles
     */
    public static String[] getAllResources() {
        return java.util.Arrays.stream(Permission.values())
            .map(Permission::getResource)
            .distinct()
            .toArray(String[]::new);
    }
}
