package autoservice.dto;

import autoservice.enums.SortCarServiceMasters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CarServiceMastersQueryTest {
    @Test
    @DisplayName("Создание запроса со всеми параметрами")
    void testBuilderWithAllParameters() {
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        boolean testIsOccupied = true;
        SortCarServiceMasters testSort = SortCarServiceMasters.NAME;

        CarServiceMastersQuery query = CarServiceMastersQuery.builder()
                .localDate(testDate)
                .isOccupied(testIsOccupied)
                .sort(testSort)
                .build();

        assertEquals(testDate, query.localDate());
        assertEquals(testIsOccupied, query.isOccupied());
        assertEquals(testSort, query.sort());
    }

    @Test
    @DisplayName("Создание запроса с минимальными параметрами")
    void testBuilderWithRequiredParametersOnly() {
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        boolean testIsOccupied = false;
        SortCarServiceMasters testSort = SortCarServiceMasters.NAME;

        CarServiceMastersQuery query = CarServiceMastersQuery.builder()
                .localDate(testDate)
                .isOccupied(testIsOccupied)
                .sort(testSort)
                .build();

        assertEquals(testDate, query.localDate());
        assertEquals(testIsOccupied, query.isOccupied());
        assertEquals(testSort, query.sort());
    }

    @Test
    @DisplayName("Проверка валидации null для localDate")
    void testNullLocalDateValidation() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            CarServiceMastersQuery.builder()
                    .localDate(null)
                    .isOccupied(true)
                    .sort(SortCarServiceMasters.NAME)
                    .build();
        });

        assertTrue(exception.getMessage().contains("localDate cannot be null"));
    }

    @Test
    @DisplayName("Проверка валидации null для isOccupied")
    void testNullIsOccupiedValidation() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            CarServiceMastersQuery.builder()
                    .localDate(LocalDate.now())
                    .isOccupied(null)
                    .sort(SortCarServiceMasters.NAME)
                    .build();
        });

        assertTrue(exception.getMessage().contains("isOccupied cannot be null"));
    }

    @Test
    @DisplayName("Проверка валидации null для sort")
    void testNullSortValidation() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            CarServiceMastersQuery.builder()
                    .localDate(LocalDate.now())
                    .isOccupied(true)
                    .sort(null)
                    .build();
        });

        assertTrue(exception.getMessage().contains("sort cannot be null"));
    }

    @Test
    @DisplayName("Проверка работы с разными датами")
    void testDifferentDateScenarios() {
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        LocalDate futureDate = LocalDate.of(2030, 1, 1);
        LocalDate currentDate = LocalDate.now();

        CarServiceMastersQuery query1 = CarServiceMastersQuery.builder()
                .localDate(pastDate)
                .isOccupied(true)
                .sort(SortCarServiceMasters.NAME)
                .build();
        assertEquals(pastDate, query1.localDate());

        CarServiceMastersQuery query2 = CarServiceMastersQuery.builder()
                .localDate(futureDate)
                .isOccupied(false)
                .sort(SortCarServiceMasters.NAME)
                .build();
        assertEquals(futureDate, query2.localDate());

        CarServiceMastersQuery query3 = CarServiceMastersQuery.builder()
                .localDate(currentDate)
                .isOccupied(true)
                .sort(SortCarServiceMasters.NAME)
                .build();
        assertEquals(currentDate, query3.localDate());
    }

    @Test
    @DisplayName("Проверка разных значений isOccupied")
    void testDifferentIsOccupiedValues() {
        CarServiceMastersQuery query1 = CarServiceMastersQuery.builder()
                .localDate(LocalDate.now())
                .isOccupied(true)
                .sort(SortCarServiceMasters.NAME)
                .build();
        assertTrue(query1.isOccupied());

        CarServiceMastersQuery query2 = CarServiceMastersQuery.builder()
                .localDate(LocalDate.now())
                .isOccupied(false)
                .sort(SortCarServiceMasters.NAME)
                .build();
        assertFalse(query2.isOccupied());
    }
}
