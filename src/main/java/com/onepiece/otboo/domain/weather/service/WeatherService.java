package com.onepiece.otboo.domain.weather.service;

import com.onepiece.otboo.domain.weather.dto.response.WeatherDto;
import java.util.List;

public interface WeatherService {

    List<WeatherDto> getWeather(Double longitude, Double latitude);
}
