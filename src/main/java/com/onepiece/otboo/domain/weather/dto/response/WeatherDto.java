package com.onepiece.otboo.domain.weather.dto.response;

import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record WeatherDto(
    UUID id,
    ZonedDateTime forecastedAt,
    ZonedDateTime forecastAt,
    WeatherAPILocation location,
    SkyStatus skyStatus,
    PrecipitationDto precipitation,
    HumidityDto humidity,
    TemperatureDto temperature,
    WindSpeedDto windSpeed
) {

}
