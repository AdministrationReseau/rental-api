package inc.yowyob.rental_api.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateTariffDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;
    
    @Size(max = 255)
    private String description;

    @NotNull @PositiveOrZero
    private BigDecimal pricePerHour;

    @NotNull @PositiveOrZero
    private BigDecimal pricePerDay;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    private boolean isDefault = false;
}