package autoservice.utils.date;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

public class DateUtils {
    public static boolean isDateInRange(@NotNull LocalDate dateToCheck,
                                        @NotNull LocalDate startDate,
                                        @NotNull LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }

        return !dateToCheck.isBefore(startDate) && !dateToCheck.isAfter(endDate);
    }
}