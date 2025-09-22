package com.onepiece.otboo.domain.weather.support;

import java.util.Map;

public record Indices(
    Map<ForecastKey, Double> tmpByKey,
    Map<ForecastKey, Double> rehByKey
) {

}
