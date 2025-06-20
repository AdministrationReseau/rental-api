package inc.yowyob.rental_api.core.enums;

import lombok.Getter;

/**
 * Énumération des types d'organisation
 */
@Getter
public enum OrganizationType {
    INDIVIDUAL("individual", "Particulier"),
    SMALL_BUSINESS("small_business", "Petite entreprise"),
    MEDIUM_BUSINESS("medium_business", "Moyenne entreprise"),
    LARGE_ENTERPRISE("large_enterprise", "Grande entreprise"),
    GOVERNMENT("government", "Organisme gouvernemental"),
    NON_PROFIT("non_profit", "Organisation à but non lucratif");

    private final String code;
    private final String description;

    OrganizationType(String code, String description) {
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
