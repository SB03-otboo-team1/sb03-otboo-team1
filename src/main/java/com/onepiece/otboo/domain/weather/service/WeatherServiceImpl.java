package com.onepiece.otboo.domain.weather.service;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.exception.InvalidCoordinateException;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import com.onepiece.otboo.domain.weather.dto.response.WeatherDto;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.mapper.WeatherMapper;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import com.onepiece.otboo.global.util.DoubleRoundingConverter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final LocationRepository locationRepository;
    private final WeatherRepository weatherRepository;
    private final WeatherMapper weatherMapper;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    @Transactional(readOnly = true)
    public List<WeatherDto> getWeather(Double longitude, Double latitude) {

        if (!validLatLon(longitude, latitude)) {
            throw new InvalidCoordinateException();
        }

        double roundedLat = DoubleRoundingConverter.roundTo4(latitude);
        double roundedLon = DoubleRoundingConverter.roundTo4(longitude);

        Location location = locationRepository.findByLatitudeAndLongitude(roundedLat, roundedLon)
            .or(() -> locationRepository.findNearest(roundedLat, roundedLon))
            .orElseThrow(() -> new IllegalArgumentException("근처 좌표를 찾을 수 없습니다."));

        // 2) KST 기준 현재 시각의 "가까운 정시"를 목표 시각으로 사용
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        ZonedDateTime baseHourKst = roundToNearestHour(nowKst);
        LocalTime targetTimeOfDay = baseHourKst.toLocalTime(); // 매일 동일 시각을 타깃

        // 3) 오늘~+4일 루프를 돌며 매일 1건(가장 가까운) 선택
        List<WeatherDto> out = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            LocalDate day = nowKst.toLocalDate().plusDays(i);
            // 그 날짜의 타깃 시각(KST)
            ZonedDateTime targetKst = ZonedDateTime.of(day, targetTimeOfDay, KST);
            Instant targetUtc = targetKst.toInstant();

            // 후보 범위: ±2시간
            Instant from = targetKst.minusHours(2).toInstant();
            Instant to = targetKst.plusHours(2).toInstant();

            // 3-1) 우선 좁은 윈도우에서 후보를 뽑음
            List<Weather> candidates = weatherRepository.findRange(location.getId(), from, to);

            // 3-2) 비어 있으면 당일 00~24시(KST)에서 후보를 전체 재탐색
            if (candidates.isEmpty()) {
                ZonedDateTime dayStartKst = day.atStartOfDay(KST);
                Instant dayStart = dayStartKst.toInstant();
                Instant dayEnd = dayStartKst.plusDays(1).minusNanos(1).toInstant();
                candidates = weatherRepository.findRange(location.getId(), dayStart, dayEnd);
            }

            // 3-3) 가장 가까운 1건 선택
            Weather picked = pickNearestByAbsDiff(candidates, targetUtc);
            if (picked != null) {
                out.add(toDtoKst(picked)); // DTO 변환(KST로 표시)
            }
        }

        return out;
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

    private Weather pickNearestByAbsDiff(List<Weather> list, Instant targetUtc) {
        return list.stream()
            .min(Comparator.comparingLong(
                w -> Math.abs(w.getForecastAt().toEpochMilli() - targetUtc.toEpochMilli())))
            .orElse(null);
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

