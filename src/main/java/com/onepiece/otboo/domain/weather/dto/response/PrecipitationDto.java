package com.onepiece.otboo.domain.weather.dto.response;

import com.onepiece.otboo.domain.weather.enums.PrecipitationType;

public record PrecipitationDto(
    PrecipitationType type,
    Double amount,
    Double probability
) {

}
