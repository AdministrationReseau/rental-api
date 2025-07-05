package inc.yowyob.rental_api.agency.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO pour les horaires de travail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHoursDto {

    @NotNull(message = "Is open setting is required")
    private Boolean isOpen = true;

    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    // Validation personnalisée
    @AssertTrue(message = "Close time must be after open time")
    private boolean isValidOpenClose() {
        if (!Boolean.TRUE.equals(isOpen) || openTime == null || closeTime == null) {
            return true;
        }
        return closeTime.isAfter(openTime);
    }

    @AssertTrue(message = "Break end time must be after break start time")
    private boolean isValidBreakTimes() {
        if (breakStartTime == null || breakEndTime == null) {
            return true;
        }
        return breakEndTime.isAfter(breakStartTime);
    }

    @AssertTrue(message = "Break times must be within opening hours")
    private boolean isBreakWithinOpeningHours() {
        if (breakStartTime == null || breakEndTime == null ||
            openTime == null || closeTime == null || !Boolean.TRUE.equals(isOpen)) {
            return true;
        }
        return !breakStartTime.isBefore(openTime) && !breakEndTime.isAfter(closeTime);
    }
}
