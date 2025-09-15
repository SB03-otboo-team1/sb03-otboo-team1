package com.onepiece.otboo.domain.weather.service;

import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;

public interface WeatherService {

    WeatherAPILocation getLocation(double longitude, double latitude);
}
