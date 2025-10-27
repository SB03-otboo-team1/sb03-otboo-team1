package com.onepiece.otboo.domain.weather.support;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record ForecastKey(
    String date,
    String time
) {
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    public LocalDate toLocalDate() {
        return LocalDate.parse(date, DATE);
    }
}
