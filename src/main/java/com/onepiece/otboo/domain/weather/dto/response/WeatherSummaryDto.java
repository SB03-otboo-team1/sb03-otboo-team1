package com.onepiece.otboo.domain.weather.dto.response;

import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import java.util.UUID;

public record WeatherSummaryDto(
    UUID weatherId,
    SkyStatus skyStatus,
    PrecipitationDto precipitation,
    TemperatureDto temperature
) {

}
