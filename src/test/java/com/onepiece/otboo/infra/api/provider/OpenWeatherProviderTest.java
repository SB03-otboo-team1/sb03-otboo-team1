package com.onepiece.otboo.infra.api.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onepiece.otboo.infra.api.client.OpenWeatherClient;
import com.onepiece.otboo.infra.api.client.OpenWeatherClient.Root;
import com.onepiece.otboo.infra.api.dto.KmaItem;
import com.onepiece.otboo.infra.api.mapper.OwmToKmaItemMapper;
import com.onepiece.otboo.infra.converter.LatLonXYConverter;
import com.onepiece.otboo.infra.converter.LatLonXYConverter.Point;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
class OpenWeatherProviderTest {

    private final OpenWeatherClient owmClient = mock(OpenWeatherClient.class);
    private final CacheManager cacheManager = mock(CacheManager.class);

    private OpenWeatherProvider provider() {
        return new OpenWeatherProvider(owmClient, cacheManager);
    }

    private KmaItem kma(
        String category,
        String fcstDate, String fcstTime,
        String baseDate, String baseTime
    ) {
        return new KmaItem(
            category,      // category
            fcstDate,      // fcstDate
            fcstTime,      // fcstTime
            "10",          // fcstValue (임의 값)
            60,            // nx
            127,           // ny
            baseDate,      // baseDate
            baseTime       // baseTime
        );
    }

    // -------------------------------------------------------
    @Test
    void 응답_결과가_null_또는_empty면_경고_로그와_캐시_삭제_빈_리스트를_반환한다() {

        // given
        double lat = 37.5665, lon = 126.9780;
        when(owmClient.get5Day3HourForecast(lat, lon)).thenReturn(null);

        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("owm:forecast")).thenReturn(cache);

        // when
        List<KmaItem> result = provider().fetchLatestItems(lat, lon);

        // then
        assertThat(result).isEmpty();
        // key 포맷: "%f:%f"
        verify(cache).evictIfPresent(eq(String.format("%f:%f", lat, lon)));
    }

    // -------------------------------------------------------
    @Test
    void 매핑_결과가_비어_있으면_빈_리스트를_반환한다() {

        // given
        double lat = 35.0, lon = 129.0;

        Root root = new Root();
        root.list = new ArrayList<>(); // 비어 있지 않게 최소 엔트리 필요 시 더미 추가 가능
        root.list.add(new OpenWeatherClient.Item()); // 더미 1개 (필요 시 주석 처리)

        given(owmClient.get5Day3HourForecast(lat, lon)).willReturn(root);

        // 정적 메소드 mock
        try (MockedStatic<LatLonXYConverter> latlonMock = mockStatic(LatLonXYConverter.class);
            MockedStatic<OwmToKmaItemMapper> mapperMock = mockStatic(OwmToKmaItemMapper.class)) {

            latlonMock.when(() -> LatLonXYConverter.latLonToXY(anyDouble(), anyDouble()))
                .thenReturn(new Point(60, 127));

            mapperMock.when(() -> OwmToKmaItemMapper.map(eq(root), eq(60), eq(127)))
                .thenReturn(List.of()); // 매핑 결과 비어있음

            // when
            List<KmaItem> result = provider().fetchLatestItems(lat, lon);

            // then
            assertThat(result).isEmpty();
            // root/list는 존재하므로 evict는 발생하지 않아야 함
            verify(cacheManager, never()).getCache(any());
        }
    }

    // -------------------------------------------------------
    @Test
    void 동일_키_중_base_최신값을_남긴다() {

        // given
        double lat = 37.4, lon = 127.1;

        Root root = new Root();
        root.list = List.of(new OpenWeatherClient.Item()); // 더미

        given(owmClient.get5Day3HourForecast(lat, lon)).willReturn(root);

        // 동일 키(category A, 20250101, 0300)지만 base가 다른 두 개
        KmaItem older = kma("TMP", "20250101", "0300", "20241231", "2300");
        KmaItem newer = kma("TMP", "20250101", "0300", "20250101", "0000");
        // 다른 키 하나
        KmaItem another = kma("POP", "20250101", "0300", "20250101", "0000");

        try (MockedStatic<LatLonXYConverter> latlonMock = mockStatic(LatLonXYConverter.class);
            MockedStatic<OwmToKmaItemMapper> mapperMock = mockStatic(OwmToKmaItemMapper.class)) {

            latlonMock.when(() -> LatLonXYConverter.latLonToXY(lat, lon))
                .thenReturn(new Point(60, 127));

            mapperMock.when(() -> OwmToKmaItemMapper.map(eq(root), eq(60), eq(127)))
                .thenReturn(List.of(older, newer, another));

            // when
            List<KmaItem> result = provider().fetchLatestItems(lat, lon);

            // then
            // 키 2개만 남아야 함: (TMP,20250101,0300) → 최신 base(newer), (POP,20250101,0300)
            assertEquals(2, result.size());
            assertThat(result).contains(another);
            // 최신 base가 선택되었는지 확인
            KmaItem chosen = result.stream()
                .filter(it -> it.category().equals("TMP"))
                .findFirst()
                .orElseThrow();
            assertEquals("202501010000", chosen.baseDate() + chosen.baseTime());
        }
    }

    @Test
    void 응답_결과가_빈_리스트면_캐시_evict_및_빈_리스트를_반환한다() {

        // given
        double lat = 33.0, lon = 126.0;

        Root root = new Root();
        root.list = List.of(); // 완전 빈 리스트

        given(owmClient.get5Day3HourForecast(lat, lon)).willReturn(root);

        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("owm:forecast")).thenReturn(cache);

        // when
        List<KmaItem> result = provider().fetchLatestItems(lat, lon);

        // then
        assertThat(result).isEmpty();
        verify(cache).evictIfPresent(eq(String.format("%f:%f", lat, lon)));
    }
}
