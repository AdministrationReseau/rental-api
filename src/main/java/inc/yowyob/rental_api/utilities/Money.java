package inc.yowyob.rental_api.utilities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@UserDefinedType("money")
public class Money {
    private BigDecimal amount;
    private String currency; // ex: "XAF", "EUR", "USD"
}