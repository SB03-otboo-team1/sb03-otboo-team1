package com.onepiece.otboo.domain.weather.service;

import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public WeatherAPILocation getLocation(double longitude, double latitude) {
        return null;
    }
}
