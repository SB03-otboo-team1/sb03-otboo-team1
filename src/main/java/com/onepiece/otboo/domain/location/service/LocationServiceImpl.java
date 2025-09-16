package com.onepiece.otboo.domain.location.service;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import com.onepiece.otboo.infra.api.dto.KakaoLocationItem;
import com.onepiece.otboo.infra.api.provider.LocationProvider;
import com.onepiece.otboo.infra.converter.LatLonToXYConverter;
import com.onepiece.otboo.infra.converter.LatLonToXYConverter.Point;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationProvider locationProvider;
    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public WeatherAPILocation getLocation(double longitude, double latitude) {

        KakaoLocationItem item = locationProvider.getLocation(longitude, latitude);

        // 위치 정보 저장
        Location location = createLocationEntity(item, latitude, longitude);
        Location savedLocation = locationRepository.save(location);

        String locationString = savedLocation.getLocationNames();
        log.info("[LocationService] 위치 정보 조회 완료 - x: {}, y: {} locationNames: {}",
            savedLocation.getXCoordinate(), savedLocation.getYCoordinate(), locationString);

        List<String> locationNames = Arrays.stream(locationString.split(","))
            .filter(s -> !s.isBlank())
            .toList();

        return new WeatherAPILocation(
            latitude,
            longitude,
            location.getXCoordinate(),
            location.getYCoordinate(),
            locationNames
        );
    }

    private Location createLocationEntity(KakaoLocationItem item, double latitude,
        double longitude) {
        String locationNames = String.join(",",
            item.region1(),
            item.region2(),
            item.region3(),
            item.region4()
        );

        // 위도, 경도를 x, y 좌표로 변환
        Point point = LatLonToXYConverter.latLonToXY(latitude, longitude);

        return Location.builder()
            .latitude(roundTo4(latitude))
            .longitude(roundTo4(longitude))
            .locationNames(locationNames)
            .xCoordinate(point.x)
            .yCoordinate(point.y)
            .build();
    }

    private double roundTo4(double value) {
        return Math.round(value * 10000d) / 10000d;
    }
}
