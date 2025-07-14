package inc.yowyob.rental_api.vehicle.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class TariffDto {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal pricePerHour;
    private BigDecimal pricePerDay;
    private String currency;
    private boolean isDefault;
}