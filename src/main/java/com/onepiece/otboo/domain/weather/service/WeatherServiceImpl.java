package com.onepiece.otboo.domain.weather.service;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.exception.InvalidCoordinateException;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import com.onepiece.otboo.domain.weather.dto.response.WeatherDto;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.mapper.WeatherMapper;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import com.onepiece.otboo.global.util.NumberConverter;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final LocationRepository locationRepository;
    private final WeatherRepository weatherRepository;
    private final WeatherMapper weatherMapper;
    private final Clock clock;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Value("${otboo.location.default}")
    private String DEFAULT_LOCATION_NAME;

    @Override
    @Transactional(readOnly = true)
    public List<WeatherDto> getWeather(Double longitude, Double latitude) {

        if (!validLatLon(longitude, latitude)) {
            throw new InvalidCoordinateException();
        }

        double roundedLat = NumberConverter.round(latitude, 4);
        double roundedLon = NumberConverter.round(longitude, 4);

        Location location = locationRepository.findByLatitudeAndLongitude(roundedLat, roundedLon)
            .or(() -> locationRepository.findNearest(roundedLat, roundedLon))
            .or(() -> locationRepository.findByLocationNames(DEFAULT_LOCATION_NAME))
            .orElseThrow(() -> new IllegalArgumentException("근처 좌표를 찾을 수 없습니다."));

        // KST 기준 현재 시각의 "가까운 정시"
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        ZonedDateTime baseHourKst = roundToNearestHour(nowKst);
        LocalTime targetTimeOfDay = baseHourKst.toLocalTime();

        // 타깃 시각(KST) 5개
        List<ZonedDateTime> targetsKst = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            targetsKst.add(ZonedDateTime.of(baseHourKst.toLocalDate().plusDays(i), targetTimeOfDay, KST));
        }

        // 범위 한 번만 조회 (±2h 커버)
        Instant from = targetsKst.stream().map(t -> t.minusHours(2).toInstant()).min(Instant::compareTo).orElseThrow();
        Instant to   = targetsKst.stream().map(t -> t.plusHours(2).toInstant()).max(Instant::compareTo).orElseThrow();

        List<Weather> all = weatherRepository.findRange(location.getId(), from, to);
        if (all.isEmpty()) {
            return List.of(); // 전부 비어 있으면 바로 반환
        }

        // KST 날짜 기준으로 미리 그룹핑 (필터 반복 줄임)
        Map<LocalDate, List<Weather>> byDay = all.stream()
            .collect(java.util.stream.Collectors.groupingBy(w -> w.getForecastAt().atZone(KST).toLocalDate()));

        List<WeatherDto> result = new ArrayList<>(5);
        for (ZonedDateTime targetKst : targetsKst) {
            LocalDate day = targetKst.toLocalDate();
            Instant targetUtc = targetKst.toInstant();

            Weather picked = byDay.getOrDefault(day, List.of()).stream()
                .min(Comparator.comparingLong(w -> Math.abs(w.getForecastAt().toEpochMilli() - targetUtc.toEpochMilli())))
                .orElse(null);

            // (선택) 하루 범위 fallback: 그룹에 없으면 그날 00~24(KST) 전체에서 1건 검색
            if (picked == null) {
                ZonedDateTime dayStartKst = day.atStartOfDay(KST);
                Instant dayStart = dayStartKst.toInstant();
                Instant dayEnd   = dayStartKst.plusDays(1).minusNanos(1).toInstant();

                List<Weather> dayAll = weatherRepository.findRange(location.getId(), dayStart, dayEnd);
                picked = dayAll.stream()
                    .min(Comparator.comparingLong(w -> Math.abs(w.getForecastAt().toEpochMilli() - targetUtc.toEpochMilli())))
                    .orElse(null);
            }

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

