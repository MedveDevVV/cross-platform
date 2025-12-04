package autoservice.utils;

import autoservice.exception.InvalidDateException;

import java.time.LocalDate;
import java.util.Objects;

public class DateUtils {

    /**
     * Проверяет корректность диапазона дат
     *
     * @param start начальная дата диапазона (не может быть null)
     * @param end конечная дата диапазона (не может быть null)
     * @throws InvalidDateException если начальная дата находится в прошлом
     *                              или конечная дата раньше начальной
     * @throws NullPointerException если start или end равны null
     */
    public static void validateDateRange(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start cannot be null");
        Objects.requireNonNull(end, "end cannot be null");

        if (start.isBefore(LocalDate.now()))
            throw new InvalidDateException("Дата начала не может быть в прошлом: " + start);
        if (end.isBefore(start))
            throw new InvalidDateException("Дата окончания не может быть раньше начала: " + end);
    }

    public static boolean isDateInRange(LocalDate dateToCheck,
                                        LocalDate startDate,
                                        LocalDate endDate) {

        Objects.requireNonNull(dateToCheck, "dateToCheck cannot be null");
        Objects.requireNonNull(startDate, "startDate cannot be null");
        Objects.requireNonNull(endDate, "endDate cannot be null");

        return !dateToCheck.isBefore(startDate) && !dateToCheck.isAfter(endDate);
    }
}