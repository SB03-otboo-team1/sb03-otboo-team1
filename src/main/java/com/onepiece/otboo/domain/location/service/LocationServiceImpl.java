package com.onepiece.otboo.domain.location.service;

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

    @Override
    @Transactional
    public WeatherAPILocation getLocation(double longitude, double latitude) {

        KakaoLocationItem item = locationProvider.getLocation(longitude, latitude);
        String locationString = String.join(",",
            item.region1(),
            item.region2(),
            item.region3(),
            item.region4()
        );

        // 위도, 경도를 x, y 좌표로 변환
        Point point = LatLonToXYConverter.latLonToXY(latitude, longitude);

        log.info("[LocationService] 위치 정보 조회 완료 - x: {}, y: {} locationNames: {}",
            point.x, point.y, locationString);

        List<String> locationNames = Arrays.stream(locationString.split(","))
            .filter(s -> !s.isBlank())
            .toList();

        return new WeatherAPILocation(
            latitude,
            longitude,
            point.x,
            point.y,
            locationNames
        );
    }
}
