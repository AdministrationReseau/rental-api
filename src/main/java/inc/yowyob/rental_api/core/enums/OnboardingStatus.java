package inc.yowyob.rental_api.core.enums;

import lombok.Getter;

/**
 * Énumération des statuts du processus d'onboarding
 */
@Getter
public enum OnboardingStatus {
    IN_PROGRESS("in_progress", "En cours"),
    COMPLETED("completed", "Terminé"),
    EXPIRED("expired", "Expiré"),
    CANCELLED("cancelled", "Annulé"),
    FAILED("failed", "Échoué");

    private final String code;
    private final String description;

    OnboardingStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public boolean isActive() {
        return this == IN_PROGRESS;
    }

    public boolean isFinished() {
        return this == COMPLETED || this == EXPIRED || this == CANCELLED || this == FAILED;
    }
}
