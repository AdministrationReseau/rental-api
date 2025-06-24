package inc.yowyob.rental_api.core.enums;

import lombok.Getter;

/**
 * Types de rôles prédéfinis dans le système
 */
@Getter
public enum RoleType {
    // Rôles système
    SUPER_ADMIN("super_admin", "Super Administrateur", true),

    // Rôles organisation
    ORGANIZATION_OWNER("organization_owner", "Propriétaire d'Organisation", false),
    ORGANIZATION_ADMIN("organization_admin", "Administrateur d'Organisation", false),

    // Rôles agence
    AGENCY_MANAGER("agency_manager", "Gestionnaire d'Agence", false),
    AGENCY_SUPERVISOR("agency_supervisor", "Superviseur d'Agence", false),

    // Rôles opérationnels
    RENTAL_AGENT("rental_agent", "Agent de Location", false),
    DRIVER_MANAGER("driver_manager", "Gestionnaire de Chauffeurs", false),
    VEHICLE_MANAGER("vehicle_manager", "Gestionnaire de Véhicules", false),

    // Rôles client
    CLIENT("client", "Client", false),
    VIP_CLIENT("vip_client", "Client VIP", false),

    // Rôles personnel
    RECEPTIONIST("receptionist", "Réceptionniste", false),
    MECHANIC("mechanic", "Mécanicien", false),
    DRIVER("driver", "Chauffeur", false),

    // Rôles financiers
    ACCOUNTANT("accountant", "Comptable", false),
    PAYMENT_MANAGER("payment_manager", "Gestionnaire de Paiements", false);

    private final String code;
    private final String description;
    private final boolean isSystemRole;

    RoleType(String code, String description, boolean isSystemRole) {
        this.code = code;
        this.description = description;
        this.isSystemRole = isSystemRole;
    }

    /**
     * Vérifie si le rôle est un rôle système (non modifiable)
     */
    public boolean isSystemRole() {
        return isSystemRole;
    }

    /**
     * Vérifie si le rôle est un rôle d'administration
     */
    public boolean isAdminRole() {
        return this == SUPER_ADMIN || this == ORGANIZATION_OWNER || this == ORGANIZATION_ADMIN;
    }

    /**
     * Vérifie si le rôle est un rôle de gestion
     */
    public boolean isManagerRole() {
        return this == AGENCY_MANAGER || this == DRIVER_MANAGER || this == VEHICLE_MANAGER || this == PAYMENT_MANAGER;
    }

    /**
     * Obtient les rôles système uniquement
     */
    public static RoleType[] getSystemRoles() {
        return java.util.Arrays.stream(RoleType.values())
            .filter(RoleType::isSystemRole)
            .toArray(RoleType[]::new);
    }

    /**
     * Obtient les rôles d'organisation uniquement
     */
    public static RoleType[] getOrganizationRoles() {
        return java.util.Arrays.stream(RoleType.values())
            .filter(role -> !role.isSystemRole())
            .toArray(RoleType[]::new);
    }
}
