package inc.yowyob.rental_api.core.enums;

import lombok.Getter;

/**
 * Énumération des étapes du processus d'onboarding
 */
@Getter
public enum OnboardingStep {
    OWNER_INFO("owner_info", "Informations du propriétaire", 1),
    ORGANIZATION_INFO("organization_info", "Informations de l'organisation", 2),
    SUBSCRIPTION_PAYMENT("subscription_payment", "Sélection forfait et paiement", 3);

    private final String code;
    private final String description;
    private final int order;

    OnboardingStep(String code, String description, int order) {
        this.code = code;
        this.description = description;
        this.order = order;
    }

    public boolean isAfter(OnboardingStep other) {
        return this.order > other.order;
    }

    public boolean isBefore(OnboardingStep other) {
        return this.order < other.order;
    }

    public OnboardingStep getNext() {
        OnboardingStep[] steps = OnboardingStep.values();
        int currentIndex = this.ordinal();
        if (currentIndex < steps.length - 1) {
            return steps[currentIndex + 1];
        }
        return this;
    }

    public OnboardingStep getPrevious() {
        OnboardingStep[] steps = OnboardingStep.values();
        int currentIndex = this.ordinal();
        if (currentIndex > 0) {
            return steps[currentIndex - 1];
        }
        return this;
    }
}
