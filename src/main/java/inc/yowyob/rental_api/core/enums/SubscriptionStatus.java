package inc.yowyob.rental_api.core.enums;

import lombok.Getter;

@Getter
public enum SubscriptionStatus {
    TRIAL("Trial", "Période d'essai"),
    ACTIVE("Active", "Souscription active"),
    EXPIRED("Expired", "Souscription expirée"),
    CANCELLED("Cancelled", "Souscription annulée"),
    SUSPENDED("Suspended", "Souscription suspendue"),
    PENDING_PAYMENT("Pending Payment", "En attente de paiement"),
    PAYMENT_FAILED("Payment Failed", "Échec du paiement");

    private final String code;
    private final String description;

    SubscriptionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public boolean isActive() {
        return this == ACTIVE || this == TRIAL;
    }

    public boolean canBeReactivated() {
        return this == SUSPENDED || this == EXPIRED;
    }
}
