package com.onepiece.otboo.domain.weather.service;

import com.onepiece.otboo.domain.location.exception.InvalidAreaException;
import com.onepiece.otboo.domain.location.exception.InvalidCoordinateException;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.dto.response.WeatherDto;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import com.onepiece.otboo.infra.converter.LatLonToXYConverter;
import com.onepiece.otboo.infra.converter.LatLonToXYConverter.Point;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final LocationRepository locationRepository;
    private final WeatherRepository weatherRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WeatherDto> getWeather(Double longitude, Double latitude) {

        if (!validLatLon(longitude, latitude)) {
            throw new InvalidCoordinateException();
        }

        Point point = LatLonToXYConverter.latLonToXY(latitude, longitude);

        return List.of();
    }

    private boolean validLatLon(double longitude, double latitude) {
        return longitude >= -180 && longitude <= 180 && latitude >= -90 && latitude <= 90;
    }
}
