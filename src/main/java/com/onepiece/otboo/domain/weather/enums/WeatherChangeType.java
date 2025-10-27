package com.onepiece.otboo.domain.weather.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WeatherChangeType {

    TEMPERATURE_CHANGE("기온 급변 알림"),
    WIND_CHANGE("강풍 주의보"),
    PRECIPITATION_CHANGE("강수 예보");

    private final String title;
}
