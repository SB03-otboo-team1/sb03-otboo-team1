package com.onepiece.otboo.domain.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.exception.InvalidCoordinateException;
import com.onepiece.otboo.domain.weather.dto.response.WeatherDto;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.domain.weather.mapper.WeatherMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private WeatherCacheHelper cacheHelper;

    @Mock
    private WeatherMapper weatherMapper;

    @Mock
    private Clock clock; // 현재 구현에선 사용되지 않지만 생성자 의존성 존재

    @InjectMocks
    private WeatherServiceImpl weatherService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private Location mockLocation(UUID id) {
        Location l = mock(Location.class);
        lenient().when(l.getId()).thenReturn(id);
        return l;
    }

    private Weather mockWeather(UUID id, Location loc, Instant forecastAt, Instant forecastedAt,
        SkyStatus sky) {
        Weather w = mock(Weather.class);
        lenient().when(w.getId()).thenReturn(id);
        lenient().when(w.getLocation()).thenReturn(loc);
        lenient().when(w.getForecastAt()).thenReturn(forecastAt);
        lenient().when(w.getForecastedAt()).thenReturn(forecastedAt);
        lenient().when(w.getSkyStatus()).thenReturn(sky);
        // WeatherMapper가 반환하는 DTO 하위필드는 null이어도 무관 (빌더에 null 주입 허용 가정)
        lenient().when(weatherMapper.toPrecipitationDto(w)).thenReturn(null);
        lenient().when(weatherMapper.toHumidityDto(w)).thenReturn(null);
        lenient().when(weatherMapper.toTemperatureDto(w)).thenReturn(null);
        lenient().when(weatherMapper.toWindSpeedDto(w)).thenReturn(null);
        return w;
    }

    private ZonedDateTime roundToNearestHour(ZonedDateTime zdt) {
        return (zdt.getMinute() >= 30)
            ? zdt.plusHours(1).withMinute(0).withSecond(0).withNano(0)
            : zdt.withMinute(0).withSecond(0).withNano(0);
    }

    @BeforeEach
    void setUp() {
        // clock은 현재 코드에 사용되지 않지만 @InjectMocks를 위해 존재
    }

    @Test
    void 위경도_범위를_벗어나면_InvalidCoordinateException() {
        // given
        double lon = 181.0;
        double lat = -91.0;

        // when
        Throwable thrown = catchThrowable(() -> weatherService.getWeather(lon, lat));

        // then
        assertThat(thrown).isInstanceOf(InvalidCoordinateException.class);
    }

    @Test
    void 위치_해석시_소수4자리_반올림값으로_cacheHelper_resolveLocation_호출() {
        // given
        double lat = 37.123456;
        double lon = 127.987654;
        double roundedLat = 37.1235;
        double roundedLon = 127.9877;

        Location loc = mockLocation(UUID.randomUUID());
        given(cacheHelper.resolveLocation(anyDouble(), anyDouble())).willReturn(loc);
        // 5일치 요청에 대해 모두 빈 리스트 반환
        given(cacheHelper.getWeathersOfDay(eq(loc.getId()), any(LocalDate.class)))
            .willReturn(List.of());

        // when
        List<WeatherDto> result = weatherService.getWeather(lon, lat);

        // then
        assertThat(result).isEmpty();

        ArgumentCaptor<Double> latCap = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> lonCap = ArgumentCaptor.forClass(Double.class);
        verify(cacheHelper).resolveLocation(latCap.capture(), lonCap.capture());
        assertThat(latCap.getValue()).isCloseTo(roundedLat, within(1e-4));
        assertThat(lonCap.getValue()).isCloseTo(roundedLon, within(1e-4));

        // 타깃 5개 일자에 대해 getWeathersOfDay가 5회 호출되는지 정도만 검증
        verify(cacheHelper, times(5)).getWeathersOfDay(eq(loc.getId()), any(LocalDate.class));
    }

    @Test
    void 각_타깃시각에_가장_가까운_예보를_선택한다() {
        // given
        double lat = 37.5, lon = 127.0;
        Location loc = mockLocation(UUID.randomUUID());
        given(cacheHelper.resolveLocation(anyDouble(), anyDouble())).willReturn(loc);

        // 서비스 내부 기준시간 계산과 동일하게 테스트에서도 now→정시 반올림
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        ZonedDateTime base = roundToNearestHour(nowKst);
        // 타깃 5개 일자: base의 시각 그대로 day+0..+4
        List<ZonedDateTime> targets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            targets.add(base.plusDays(i));
        }

        // 각 날짜별로 (-20분, +10분) 두 개 예보를 제공 → +10분 쪽이 더 근접하므로 선택 기대
        List<UUID> expectedIds = new ArrayList<>();
        for (ZonedDateTime t : targets) {
            UUID idMinus = UUID.randomUUID();
            UUID idPlus = UUID.randomUUID();

            Weather wMinus = mockWeather(idMinus, loc, t.minusMinutes(20).toInstant(),
                t.minusHours(3).toInstant(), SkyStatus.CLEAR);
            Weather wPlus = mockWeather(idPlus, loc, t.plusMinutes(10).toInstant(),
                t.minusHours(2).toInstant(), SkyStatus.MOSTLY_CLOUDY);

            expectedIds.add(idPlus);

            given(cacheHelper.getWeathersOfDay(eq(loc.getId()), eq(t.toLocalDate())))
                .willReturn(List.of(wMinus, wPlus));
        }

        // when
        List<WeatherDto> result = weatherService.getWeather(lon, lat);

        // then
        assertEquals(5, result.size());
        for (int i = 0; i < 5; i++) {
            WeatherDto dto = result.get(i);
            assertThat(dto.id()).isEqualTo(expectedIds.get(i));
            // DTO의 ZonedDateTime이 KST 타임존인지 가볍게 확인
            assertThat(dto.forecastAt().getZone()).isEqualTo(KST);
            assertThat(dto.forecastedAt().getZone()).isEqualTo(KST);
        }
    }

    @Test
    void 모든_일자에서_예보가_없으면_빈_리스트() {
        // given
        double lat = 37.5, lon = 127.0;
        Location loc = mockLocation(UUID.randomUUID());
        given(cacheHelper.resolveLocation(anyDouble(), anyDouble())).willReturn(loc);

        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        ZonedDateTime base = roundToNearestHour(nowKst);

        for (int i = 0; i < 5; i++) {
            given(cacheHelper.getWeathersOfDay(eq(loc.getId()), eq(base.toLocalDate().plusDays(i))))
                .willReturn(List.of());
        }

        // when
        List<WeatherDto> result = weatherService.getWeather(lon, lat);

        // then
        assertThat(result).isEmpty();
    }
}
