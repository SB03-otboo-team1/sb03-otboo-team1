package com.onepiece.otboo.domain.weather.support;

import java.util.Map;

public record Extremes(
    Map<String, Double> tmxByDate,
    Map<String, Double> tmnByDate
) {

}
