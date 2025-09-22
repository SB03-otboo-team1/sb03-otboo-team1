package com.onepiece.otboo.domain.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import com.onepiece.otboo.infra.api.dto.KakaoLocationItem;
import com.onepiece.otboo.infra.api.provider.LocationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationProvider locationProvider;

    @Mock
    private LocationPersistenceService persistenceService;

    @InjectMocks
    private LocationServiceImpl locationService;

    double longitude;
    double latitude;
    KakaoLocationItem item;

    @BeforeEach
    void setUp() {
        longitude = 126.8054724084087;
        latitude = 37.43751196107601;

        item = new KakaoLocationItem("경기도", "시흥시", "은행동", "");
    }

    @Test
    void 위치_정보_조회_성공_테스트() {
        // given
        Location location = Location.builder()
            .latitude(latitude)
            .longitude(longitude)
            .xCoordinate(57)
            .yCoordinate(124)
            .locationNames("경기도,시흥시,은행동,")
            .build();

        given(locationProvider.getLocation(longitude, latitude)).willReturn(item);
        given(persistenceService.save(any(Location.class))).willReturn(location);

        // when
        WeatherAPILocation result = locationService.getLocation(longitude, latitude);

        // then
        assertNotNull(result);
        assertEquals(latitude, result.latitude());
        assertEquals(longitude, result.longitude());
        assertEquals(57, result.x());
        assertEquals(124, result.y());
        assertThat(result.locationNames()).contains("경기도", "시흥시", "은행동");
        verify(locationProvider).getLocation(longitude, latitude);
        verify(persistenceService).save(any(Location.class));
    }

    @Test
    void 위도_경도_반올림_적용_테스트() {
        // given
        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);

        given(locationProvider.getLocation(longitude, latitude)).willReturn(item);
        given(persistenceService.save(any(Location.class)))
            .willAnswer(invocation -> invocation.getArgument(0)); // save 대상 그대로 반환

        // when
        WeatherAPILocation result = locationService.getLocation(longitude, latitude);

        // then
        verify(persistenceService).save(captor.capture());
        Location saved = captor.getValue();
        assertEquals(37.4375, saved.getLatitude());
        assertEquals(126.8055, saved.getLongitude());
        assertEquals("경기도,시흥시,은행동,", saved.getLocationNames());
        assertThat(result.locationNames()).contains("경기도", "시흥시", "은행동");
    }
}