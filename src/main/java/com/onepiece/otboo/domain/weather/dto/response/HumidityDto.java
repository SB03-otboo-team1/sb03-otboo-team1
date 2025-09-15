package com.onepiece.otboo.domain.weather.dto.response;

public record HumidityDto(
    Double current,
    Double comparedToDayBefore
) {

}
