package com.onepiece.otboo.domain.weather.support;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class KmaTime {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    public static Instant toKstInstant(String yyyymmdd, String hhmm) {
        LocalDate d = LocalDate.parse(yyyymmdd, DATE);
        LocalTime t = LocalTime.of(Integer.parseInt(hhmm) / 100, Integer.parseInt(hhmm) % 100);
        return ZonedDateTime.of(d, t, KST).toInstant();
    }
}
