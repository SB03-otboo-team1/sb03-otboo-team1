package com.onepiece.otboo.infra.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BaseDt(
    LocalDate date,
    String time
) {

    public BaseDt minusHours(long h) {
        LocalDateTime ldt = LocalDateTime.of(date,
                LocalTime.of(Integer.parseInt(time) / 100, Integer.parseInt(time) % 100))
            .minusHours(h);
        return new BaseDt(ldt.toLocalDate(),
            String.format("%02d%02d", ldt.getHour(), ldt.getMinute()));
    }
}
