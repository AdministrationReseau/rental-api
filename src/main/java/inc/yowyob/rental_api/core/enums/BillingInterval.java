package inc.yowyob.rental_api.core.enums;

import lombok.Getter;

@Getter
public enum BillingInterval {
    MONTHLY("monthly", "Mensuel", 30),
    QUARTERLY("quarterly", "Trimestriel", 90),
    YEARLY("yearly", "Annuel", 365);

    private final String code;
    private final String description;
    private final int days;

    BillingInterval(String code, String description, int days) {
        this.code = code;
        this.description = description;
        this.days = days;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getDays() {
        return days;
    }
}
