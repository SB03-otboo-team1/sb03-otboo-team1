package com.onepiece.otboo.domain.weather.dto.response;

import com.onepiece.otboo.domain.weather.enums.WindSpeedWord;

public record WindSpeedDto(
    Double speed,
    WindSpeedWord asWord
) {

}
