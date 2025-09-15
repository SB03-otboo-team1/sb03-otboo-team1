package com.onepiece.otboo.domain.feed.dto.response;

import java.util.UUID;

public record WeatherDto(
    UUID weatherId,
    String skyStatus,
    Precipitation precipitation,
    Temperature temperature
) {
    public record Precipitation(String type, Double amount, Double probability) {}
    public record Temperature(Double current, Double comparedToDayBefore, Double min, Double max) {}
}