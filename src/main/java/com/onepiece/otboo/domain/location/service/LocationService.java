package com.onepiece.otboo.domain.location.service;

import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;

public interface LocationService {

    WeatherAPILocation getLocation(double longitude, double latitude);
}
