package com.onepiece.otboo.domain.weather.dto.data;

import java.util.List;
import lombok.Builder;

@Builder
public record WeatherAPILocation(
    Double latitude,
    Double longitude,
    Integer x,
    Integer y,
    List<String> locationNames
) {

}
