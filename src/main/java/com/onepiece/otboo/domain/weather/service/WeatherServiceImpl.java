package com.onepiece.otboo.domain.weather.service;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.exception.InvalidCoordinateException;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import com.onepiece.otboo.domain.weather.dto.response.WeatherDto;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.mapper.WeatherMapper;
import com.onepiece.otboo.global.util.NumberConverter;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherCacheHelper cacheHelper;
    private final WeatherMapper weatherMapper;
    private final Clock clock;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");


    @Override
    @Transactional(readOnly = true)
    public List<WeatherDto> getWeather(Double longitude, Double latitude) {

        if (longitude == null || latitude == null || !validLatLon(longitude, latitude)) {
            throw new InvalidCoordinateException();
        }

        double roundedLat = NumberConverter.round(latitude, 4);
        double roundedLon = NumberConverter.round(longitude, 4);

        Location location = cacheHelper.resolveLocation(roundedLat, roundedLon);

        // KST 기준 현재 시각의 "가까운 정시"
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        ZonedDateTime baseHourKst = roundToNearestHour(nowKst);
        LocalTime targetTimeOfDay = baseHourKst.toLocalTime();

        // 타깃 5개 시각(KST)
        List<ZonedDateTime> targetsKst = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            targetsKst.add(
                ZonedDateTime.of(baseHourKst.toLocalDate().plusDays(i), targetTimeOfDay, KST));
        }

        Map<LocalDate, List<Weather>> byDay = new HashMap<>();
        for (ZonedDateTime targetKst : targetsKst) {
            LocalDate day = targetKst.toLocalDate();
            byDay.computeIfAbsent(day, d -> cacheHelper.getWeathersOfDay(location.getId(), d));
        }

        // (3) 각 타깃 시각에 가장 가까운 예보 선택
        List<WeatherDto> result = new ArrayList<>(5);
        for (ZonedDateTime targetKst : targetsKst) {
            LocalDate day = targetKst.toLocalDate();
            Instant targetUtc = targetKst.toInstant();

            Weather picked = byDay.getOrDefault(day, List.of()).stream()
                .min(Comparator.comparingLong(w ->
                    Math.abs(w.getForecastAt().toEpochMilli() - targetUtc.toEpochMilli())
                ))
                .orElse(null);

            if (picked != null) {
                result.add(toDtoKst(picked));
            }
        }

        return result;
    }


    private boolean validLatLon(double longitude, double latitude) {
        return longitude >= -180 && longitude <= 180 && latitude >= -90 && latitude <= 90;
    }

    private ZonedDateTime roundToNearestHour(ZonedDateTime zdt) {
        if (zdt.getMinute() >= 30) {
            return zdt.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        }
        return zdt.withMinute(0).withSecond(0).withNano(0);
    }

    private WeatherDto toDtoKst(Weather w) {
        ZonedDateTime forecastAtKst = w.getForecastAt().atZone(KST);
        ZonedDateTime forecastedAtKst = w.getForecastedAt().atZone(KST);

        return WeatherDto.builder()
            .id(w.getId())
            .forecastedAt(forecastedAtKst)
            .forecastAt(forecastAtKst)
            .location(WeatherAPILocation.toDto(w.getLocation()))
            .skyStatus(w.getSkyStatus())
            .precipitation(weatherMapper.toPrecipitationDto(w))
            .humidity(weatherMapper.toHumidityDto(w))
            .temperature(weatherMapper.toTemperatureDto(w))
            .windSpeed(weatherMapper.toWindSpeedDto(w))
            .build();
    }
}

