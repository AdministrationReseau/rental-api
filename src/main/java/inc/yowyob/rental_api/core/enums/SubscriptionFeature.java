package inc.yowyob.rental_api.core.enums;

import lombok.Getter;

@Getter
public enum SubscriptionFeature {
    BASIC_VEHICLE_MANAGEMENT("basic_vehicle", "Gestion basique des véhicules"),
    BASIC_DRIVER_MANAGEMENT("basic_driver", "Gestion basique des chauffeurs"),
    BASIC_RENTAL_MANAGEMENT("basic_rental", "Gestion basique des locations"),
    CHAT_SUPPORT("chat", "Support par chat"),
    GEOFENCING("geofencing", "Géofencing et tracking"),
    ADVANCED_REPORTS("advanced_reports", "Rapports avancés"),
    API_ACCESS("api_access", "Accès API"),
    PRIORITY_SUPPORT("priority_support", "Support prioritaire"),
    CUSTOM_BRANDING("custom_branding", "Personnalisation de marque"),
    UNLIMITED_USERS("unlimited_users", "Utilisateurs illimités"),
    ADVANCED_ANALYTICS("advanced_analytics", "Analytiques avancées"),
    WEBHOOK_NOTIFICATIONS("webhooks", "Notifications webhook");

    private final String code;
    private final String description;

    SubscriptionFeature(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
