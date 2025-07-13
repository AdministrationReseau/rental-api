package inc.yowyob.rental_api.utilities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@UserDefinedType("time_slot")
public class TimeSlot {
    private LocalTime startTime;
    private LocalTime endTime;
}