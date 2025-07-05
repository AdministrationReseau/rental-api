package inc.yowyob.rental_api.agency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalTime;

/**
 * DTO pour la géolocalisation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationDto {

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    private String googlePlaceId;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;
}
