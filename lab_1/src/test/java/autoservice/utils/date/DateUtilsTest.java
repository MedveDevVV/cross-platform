package autoservice.utils.date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {
    @Test
    @DisplayName("Дата находится в диапазоне: между началом и концом")
    void testDateInRange_Middle() {
        LocalDate dateToCheck = LocalDate.of(2024, 1, 15);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        boolean result = DateUtils.isDateInRange(dateToCheck, startDate, endDate);

        assertTrue(result);
    }

    @Test
    @DisplayName("Дата находится в диапазоне: равна начальной дате")
    void testDateInRange_StartDate() {
        LocalDate dateToCheck = LocalDate.of(2024, 1, 1);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        boolean result = DateUtils.isDateInRange(dateToCheck, startDate, endDate);

        assertTrue(result);
    }

    @Test
    @DisplayName("Дата находится в диапазоне: равна конечной дате")
    void testDateInRange_EndDate() {
        LocalDate dateToCheck = LocalDate.of(2024, 1, 31);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        boolean result = DateUtils.isDateInRange(dateToCheck, startDate, endDate);

        assertTrue(result);
    }

    @Test
    @DisplayName("Дата ВНЕ диапазона: до начала")
    void testDateInRange_BeforeStart() {
        LocalDate dateToCheck = LocalDate.of(2023, 12, 31);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        boolean result = DateUtils.isDateInRange(dateToCheck, startDate, endDate);

        assertFalse(result);
    }

    @Test
    @DisplayName("Дата ВНЕ диапазона: после конца")
    void testDateInRange_AfterEnd() {
        LocalDate dateToCheck = LocalDate.of(2024, 2, 1);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        boolean result = DateUtils.isDateInRange(dateToCheck, startDate, endDate);

        assertFalse(result);
    }

    @Test
    @DisplayName("Граничный случай: начальная дата null")
    void testDateInRange_NullStartDate() {
        LocalDate dateToCheck = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.isDateInRange(dateToCheck, null, endDate);
        });
    }

    @Test
    @DisplayName("Граничный случай: конечная дата null")
    void testDateInRange_NullEndDate() {
        LocalDate dateToCheck = LocalDate.of(2024, 1, 15);
        LocalDate startDate = LocalDate.of(2024, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.isDateInRange(dateToCheck, startDate, null);
        });
    }

    @Test
    @DisplayName("Граничный случай: обе даты null")
    void testDateInRange_BothDatesNull() {
        LocalDate dateToCheck = LocalDate.of(2024, 1, 15);

        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.isDateInRange(dateToCheck, null, null);
        });
    }

    @Test
    @DisplayName("Все параметры null ")
    void testDateInRange_AllNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.isDateInRange(null, null, null);
        });
    }
}
