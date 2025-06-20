package inc.yowyob.rental_api.core.enums;

import lombok.Getter;

@Getter
public enum SubscriptionPlanType {
    GRATUIT("GRATUIT", "Forfait gratuit (30 jours)", 0),
    BASIC("BASIC", "Forfait de base", 1),
    PREMIUM("PREMIUM", "Forfait premium", 2),
    ENTERPRISE("ENTERPRISE", "Forfait entreprise", 3);

    private final String code;
    private final String description;
    private final int level;

    SubscriptionPlanType(String code, String description, int level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHigherThan(SubscriptionPlanType other) {
        return this.level > other.level;
    }

    public boolean isLowerThan(SubscriptionPlanType other) {
        return this.level < other.level;
    }
}
