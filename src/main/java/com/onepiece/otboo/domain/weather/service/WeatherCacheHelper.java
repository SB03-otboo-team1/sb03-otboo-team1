package com.onepiece.otboo.domain.weather.service;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherCacheHelper {

    private final LocationRepository locationRepository;
    private final WeatherRepository weatherRepository;

    @Value("${otboo.location.default}")
    private String DEFAULT_LOCATION_NAME;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Cacheable(value = "locByRoundedLatLon",
        key = "T(String).format('%.4f,%.4f', #lat, #lon)")
    public Location resolveLocation(double lat, double lon) {
        return locationRepository.findByLatitudeAndLongitude(lat, lon)
            .or(() -> locationRepository.findNearest(lat, lon))
            .or(() -> locationRepository.findByLocationNames(DEFAULT_LOCATION_NAME))
            .orElseThrow(() -> new IllegalArgumentException("DB에 기본 위치가 존재하지 않습니다."));
    }

    @Cacheable(value = "weathersByDay",
        key = "#locationId + ':' + #dayKst.toString()",
        unless = "#result == null || #result.isEmpty()")
    public List<Weather> getWeathersOfDay(UUID locationId, LocalDate dayKst) {
        ZonedDateTime startKst = dayKst.atStartOfDay(KST);
        Instant from = startKst.toInstant();
        Instant to = startKst.plusDays(1).minusNanos(1).toInstant();
        return weatherRepository.findRange(locationId, from, to);
    }
}
