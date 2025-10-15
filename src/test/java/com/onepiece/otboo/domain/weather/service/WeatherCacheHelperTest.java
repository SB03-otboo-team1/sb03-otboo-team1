package com.onepiece.otboo.domain.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WeatherCacheHelperTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private WeatherRepository weatherRepository;

    @InjectMocks
    private WeatherCacheHelper cacheHelper;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @BeforeEach
    void setUp() {
        // @Value 주입 필드 설정
        ReflectionTestUtils.setField(cacheHelper, "DEFAULT_LOCATION_NAME", "서울특별시,중구,태평로1가");
    }

    private Location loc(UUID id) {
        Location l = org.mockito.Mockito.mock(Location.class);
        org.mockito.Mockito.lenient().when(l.getId()).thenReturn(id);
        return l;
    }

    @Test
    void resolveLocation_정확좌표_우선() {

        // given
        double lat = 37.1234, lon = 127.9876;
        Location exact = loc(UUID.randomUUID());
        given(locationRepository.findByLatitudeAndLongitude(lat, lon)).willReturn(
            Optional.of(exact));

        // when
        Location result = cacheHelper.resolveLocation(lat, lon);

        // then
        assertEquals(exact, result);
        verify(locationRepository).findByLatitudeAndLongitude(lat, lon);
    }

    @Test
    void resolveLocation_정확좌표없으면_nearest_다음_default_순서() {

        // given
        double lat = 37.0, lon = 127.0;
        given(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble())).willReturn(
            Optional.empty());

        Location nearest = loc(UUID.randomUUID());
        given(locationRepository.findNearest(lat, lon)).willReturn(Optional.of(nearest));

        // when
        Location result = cacheHelper.resolveLocation(lat, lon);

        // then
        assertEquals(nearest, result);
        verify(locationRepository).findNearest(lat, lon);
    }

    @Test
    void resolveLocation_nearest도없으면_default_조회() {

        // given
        double lat = 37.0, lon = 127.0;
        given(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble())).willReturn(
            Optional.empty());
        given(locationRepository.findNearest(anyDouble(), anyDouble())).willReturn(
            Optional.empty());

        Location def = loc(UUID.randomUUID());
        given(locationRepository.findByLocationNames("서울특별시,중구,태평로1가")).willReturn(
            Optional.of(def));

        // when
        Location result = cacheHelper.resolveLocation(lat, lon);

        // then
        assertEquals(def, result);
        verify(locationRepository).findByLocationNames("서울특별시,중구,태평로1가");
    }

    @Test
    void resolveLocation_default도없으면_예외() {
        // given
        double lat = 37.0, lon = 127.0;
        given(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble())).willReturn(
            Optional.empty());
        given(locationRepository.findNearest(anyDouble(), anyDouble())).willReturn(
            Optional.empty());
        given(locationRepository.findByLocationNames(any())).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> cacheHelper.resolveLocation(lat, lon));

        // then
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("기본 위치가 존재");
    }

    @Test
    void getWeathersOfDay_KST_자정부터_23_59_59_999999999까지_조회() {
        // given
        UUID locationId = UUID.randomUUID();
        LocalDate day = LocalDate.of(2025, 9, 22);

        ZonedDateTime startKst = day.atStartOfDay(KST);
        Instant expectedFrom = startKst.toInstant();
        Instant expectedTo = startKst.plusDays(1).minusNanos(1).toInstant();

        given(weatherRepository.findRange(eq(locationId), any(Instant.class), any(Instant.class)))
            .willReturn(List.of());

        // when
        List<Weather> result = cacheHelper.getWeathersOfDay(locationId, day);

        // then
        assertThat(result).isEmpty();
        verify(weatherRepository).findRange(eq(locationId), eq(expectedFrom), eq(expectedTo));
    }
}
