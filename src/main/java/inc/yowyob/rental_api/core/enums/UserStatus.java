package inc.yowyob.rental_api.core.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    PENDING_VERIFICATION("pending_verification", "En attente de vérification"),
    ACTIVE("active", "Actif"),
    SUSPENDED("suspended", "Suspendu"),
    BANNED("banned", "Banni"),
    DELETED("deleted", "Supprimé");

    private final String code;
    private final String description;

    UserStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canLogin() {
        return this == ACTIVE || this == PENDING_VERIFICATION;
    }
}
