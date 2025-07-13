package inc.yowyob.rental_api.utilities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@UserDefinedType("working_hours")
public class WorkingHours {
    private TimeSlot monday;
    private TimeSlot tuesday;
    private TimeSlot wednesday;
    private TimeSlot thursday;
    private TimeSlot friday;
    private TimeSlot saturday;
    private TimeSlot sunday;
}