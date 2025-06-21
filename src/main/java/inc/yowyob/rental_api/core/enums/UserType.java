package inc.yowyob.rental_api.core.enums;

import lombok.Getter;

@Getter
public enum UserType {
    CLIENT("client", "Client final"),
    OWNER("owner", "Propriétaire d'organisation"),
    STAFF("staff", "Personnel d'organisation"),
    SUPER_ADMIN("super_admin", "Super administrateur");

    private final String code;
    private final String description;

    UserType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
