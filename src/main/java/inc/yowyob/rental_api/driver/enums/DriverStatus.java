package inc.yowyob.rental_api.driver.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Driver availability and operational status")
public enum DriverStatus {

    @Schema(description = "Driver is currently active and assigned")
    ACTIVE("ACTIVE", "Driver is currently active and assigned"),

    @Schema(description = "Driver is available and waiting for assignment")
    AVAILABLE("AVAILABLE", "Driver is available for assignments"),

    @Schema(description = "Driver is temporarily out of service")
    OUT_OF_SERVICE("OUT_OF_SERVICE", "Driver is out of service"),

    @Schema(description = "Driver is in emergency situation")
    EMERGENCY("EMERGENCY", "Driver is in emergency state");

    private final String code;
    private final String label;

    DriverStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return this.code;
    }
}
