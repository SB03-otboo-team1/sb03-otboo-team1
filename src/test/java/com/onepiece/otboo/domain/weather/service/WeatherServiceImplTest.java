package com.onepiece.otboo.domain.weather.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.exception.InvalidCoordinateException;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.dto.response.WeatherDto;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.domain.weather.mapper.WeatherMapper;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private WeatherMapper weatherMapper;

    @Mock
    private Clock clock;

    private WeatherServiceImpl weatherService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @BeforeEach
    void setUp() {
        ZonedDateTime fixedZdt = ZonedDateTime.of(2025, 9, 22, 12, 15, 0, 0, KST);
        Clock fixedClock = Clock.fixed(fixedZdt.toInstant(), KST);
        weatherService = new WeatherServiceImpl(locationRepository, weatherRepository,
            weatherMapper, fixedClock);
    }

    private Location location(UUID id, double lat, double lon) {
        Location l = mock(Location.class);
        given(l.getId()).willReturn(id);
        return l;
    }

    private Weather weather(UUID id, Location l, Instant forecastAt, Instant forecastedAt,
        SkyStatus sky) {
        Weather w = mock(Weather.class);

        given(w.getForecastAt()).willReturn(forecastAt);

        lenient().when(w.getId()).thenReturn(id);
        lenient().when(w.getLocation()).thenReturn(l);
        lenient().when(w.getForecastedAt()).thenReturn(forecastedAt);
        lenient().when(w.getSkyStatus()).thenReturn(sky);

        return w;
    }

    private ZonedDateTime baseHourKst() {
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        return (nowKst.getMinute() >= 30)
            ? nowKst.plusHours(1).withMinute(0).withSecond(0).withNano(0)
            : nowKst.withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    void 위경도_범위를_벗어나면_InvalidCoordinateException이_발생한다() {

        // given
        double longitude = 200.0;
        double latitude = 95.0;

        // when
        Throwable thrown = catchThrowable(() -> weatherService.getWeather(longitude, latitude));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidCoordinateException.class);
    }

    @Test
    void 정확한_좌표가_없으면_근접_좌표로_조회한다() {

        // given
        double latitude = 37.123456;
        double longitude = 127.987654;
        double roundedLat = 37.1235;
        double roundedLon = 127.9877;

        Location nearest = location(UUID.randomUUID(), roundedLat, roundedLon);

        given(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble()))
            .willReturn(Optional.empty());
        given(locationRepository.findNearest(anyDouble(), anyDouble()))
            .willReturn(Optional.of(nearest));
        given(weatherRepository.findRange(eq(nearest.getId()), any(), any()))
            .willReturn(List.of());

        // when
        List<WeatherDto> result = weatherService.getWeather(longitude, latitude);

        // then
        assertEquals(0, result.size());
        ArgumentCaptor<Double> latCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> lonCaptor = ArgumentCaptor.forClass(Double.class);
        verify(locationRepository).findNearest(latCaptor.capture(), lonCaptor.capture());
        assertThat(latCaptor.getValue()).isCloseTo(roundedLat, within(1e-4));
        assertThat(lonCaptor.getValue()).isCloseTo(roundedLon, within(1e-4));
    }

    @Test
    void 날씨_데이터_조회_테스트() {

        // given
        double latitude = 37.5;
        double longitude = 127.0;
        Location exact = location(UUID.randomUUID(), latitude, longitude);

        given(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble()))
            .willReturn(Optional.of(exact));

        ZonedDateTime base = baseHourKst();

        // 타깃: 오늘부터 5개 일자, 같은 '정시'
        List<ZonedDateTime> targets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            targets.add(base.plusDays(i));
        }

        // 각 타깃에 대해 (-20분, +10분) 2개 생성 → +10분(더 가까움)의 UUID를 기대값으로 저장
        List<UUID> expectedChosenIds = new ArrayList<>();
        List<Weather> bucket = new ArrayList<>();

        for (ZonedDateTime t : targets) {
            Instant nearMinus = t.minusMinutes(20).toInstant();
            Instant nearPlus = t.plusMinutes(10).toInstant();

            UUID idMinus = UUID.randomUUID();
            UUID idPlus = UUID.randomUUID();

            bucket.add(
                weather(idMinus, exact, nearMinus, t.minusHours(3).toInstant(), SkyStatus.CLEAR));
            bucket.add(weather(idPlus, exact, nearPlus, t.minusHours(3).toInstant(),
                SkyStatus.MOSTLY_CLOUDY));
            expectedChosenIds.add(idPlus); // 더 가까운 쪽
        }

        given(weatherRepository.findRange(eq(exact.getId()), any(), any()))
            .willReturn(bucket);

        // when
        List<WeatherDto> result = weatherService.getWeather(longitude, latitude);

        // then
        assertEquals(5, result.size());
        for (int i = 0; i < 5; i++) {
            assertThat(result.get(i).id()).isEqualTo(expectedChosenIds.get(i));
            // DTO에서 KST로 변환되었는지 간단 검증
            assertThat(result.get(i).forecastAt().getZone()).isEqualTo(KST);
            assertThat(result.get(i).forecastedAt().getZone()).isEqualTo(KST);
        }
    }

    @Test
    void 특정_날짜_데이터가_없으면_fallback_테스트() {

        // given
        double latitude = 37.5;
        double longitude = 127.0;
        Location exact = location(UUID.randomUUID(), latitude, longitude);

        given(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble()))
            .willReturn(Optional.of(exact));

        ZonedDateTime base = baseHourKst();

        // 메인 범위: day0, day2, day4만 존재
        UUID idDay0 = UUID.randomUUID();
        UUID idDay2 = UUID.randomUUID();
        UUID idDay4 = UUID.randomUUID();

        List<Weather> main = List.of(
            weather(idDay0, exact, base.plusMinutes(5).toInstant(), base.minusHours(2).toInstant(),
                SkyStatus.CLEAR),
            weather(idDay2, exact, base.plusDays(2).plusMinutes(5).toInstant(),
                base.plusDays(2).minusHours(2).toInstant(), SkyStatus.CLEAR),
            weather(idDay4, exact, base.plusDays(4).plusMinutes(5).toInstant(),
                base.plusDays(4).minusHours(2).toInstant(), SkyStatus.CLOUDY)
        );

        // fallback용 id
        UUID idDay1Fallback = UUID.randomUUID();
        UUID idDay3Fallback = UUID.randomUUID();

        // 두 번째 타입의 findRange 호출(일자 전체 범위)에서 일치 일자면 fallback 리턴
        given(
            weatherRepository.findRange(eq(exact.getId()), any(Instant.class), any(Instant.class)))
            .willAnswer(inv -> {
                Instant start = inv.getArgument(1, Instant.class);
                Instant end = inv.getArgument(2, Instant.class);

                // 하루 이내 범위면 fallback 시도로 간주
                boolean isOneDay = Duration.between(start, end).toHours() <= 24;
                if (!isOneDay) {
                    return main;
                }

                LocalDate day = start.atZone(KST).toLocalDate();
                if (day.equals(base.toLocalDate().plusDays(1))) {
                    return List.of(weather(
                        idDay1Fallback,
                        exact,
                        base.plusDays(1).plusMinutes(1).toInstant(),
                        base.plusDays(1).minusHours(3).toInstant(),
                        SkyStatus.CLEAR
                    ));
                }
                if (day.equals(base.toLocalDate().plusDays(3))) {
                    return List.of(weather(
                        idDay3Fallback,
                        exact,
                        base.plusDays(3).plusMinutes(1).toInstant(),
                        base.plusDays(3).minusHours(3).toInstant(),
                        SkyStatus.CLOUDY
                    ));
                }
                return List.of();
            });

        // when
        List<WeatherDto> result = weatherService.getWeather(longitude, latitude);

        // then
        assertEquals(5, result.size());
        List<UUID> ids = result.stream().map(WeatherDto::id).toList();
        assertThat(ids.contains(idDay0)).isTrue();
    }

    @Test
    void 날씨_데이터가_없으면_빈_리스트_반환() {

        // given
        double latitude = 37.5;
        double longitude = 127.0;
        Location exact = location(UUID.randomUUID(), latitude, longitude);

        given(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble()))
            .willReturn(Optional.of(exact));
        given(weatherRepository.findRange(eq(exact.getId()), any(), any()))
            .willReturn(List.of());

        // when
        List<WeatherDto> result = weatherService.getWeather(longitude, latitude);

        // then
        assertEquals(0, result.size());
    }
}