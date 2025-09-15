package com.onepiece.otboo.domain.weather.dto.response;

import lombok.Builder;

@Builder
public record TemperatureDto(
    Double current,
    Double comparedToDayBefore,
    Double min,
    Double max
) {

}
