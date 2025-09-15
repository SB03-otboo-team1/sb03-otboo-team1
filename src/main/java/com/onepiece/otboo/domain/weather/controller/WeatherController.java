package com.onepiece.otboo.domain.weather.controller;

import com.onepiece.otboo.domain.weather.controller.api.WeatherApi;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController implements WeatherApi {

    @Override
    @GetMapping("/location")
    public ResponseEntity<WeatherAPILocation> getLocation(
        @RequestParam Double longitude,
        @RequestParam Double latitude) {

        log.info("[WeatherController] 위치 정보 조회 요청 - 위도: {}, 경도: {}",
            latitude, longitude);

        return null;
    }
}
