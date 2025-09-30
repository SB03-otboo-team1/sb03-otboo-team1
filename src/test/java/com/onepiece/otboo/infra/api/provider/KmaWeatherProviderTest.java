package com.onepiece.otboo.infra.api.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.infra.api.client.KmaClient;
import com.onepiece.otboo.infra.api.dto.KmaItem;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class KmaWeatherProviderTest {

    @Mock
    private KmaClient kmaClient;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;

    @InjectMocks
    private KmaWeatherProvider provider;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final LocalDate FIXED_DATE = LocalDate.of(2025, 9, 22);
    private static final Clock FIXED_CLOCK =
        Clock.fixed(ZonedDateTime.of(FIXED_DATE, LocalTime.of(23, 15), KST).toInstant(), KST);

    @BeforeEach
    void setUp() {
        provider = new KmaWeatherProvider(kmaClient, cacheManager, FIXED_CLOCK);
    }

    @Test
    void 최신_base가_비면_다음_base로_시도하고_빈키는_evict() {
        int nx = 60, ny = 127;
        LocalDate d = FIXED_DATE;

        given(cacheManager.getCache("kma:village")).willReturn(cache);

        given(kmaClient.getVillageForecast(nx, ny, d, "2300")).willReturn(List.of()); // 빈
        given(kmaClient.getVillageForecast(nx, ny, d, "2000")).willReturn(List.of(
            item("TMP", d, "0900", d, "2000", nx, ny, "18"),
            item("POP", d, "0900", d, "2000", nx, ny, "60")
        ));

        List<KmaItem> result = provider.fetchLatestItems(nx, ny);

        assertThat(result).hasSize(2).allSatisfy(it -> {
            assertThat(it.baseDate()).isEqualTo(d.format(DATE));
            assertThat(it.baseTime()).isEqualTo("2000");
        });
        verify(kmaClient).getVillageForecast(nx, ny, d, "2300");
        verify(kmaClient).getVillageForecast(nx, ny, d, "2000");
        verify(kmaClient, never()).getVillageForecast(nx, ny, d, "1700");
        verify(cache).evictIfPresent(eq(key(nx, ny, d, "2300")));
        verify(cache, never()).evictIfPresent(eq(key(nx, ny, d, "2000")));
    }

    @Test
    void 어제_fcst가_없으면_어제_base로_보완조회() {
        int nx = 55, ny = 127;
        LocalDate today = FIXED_DATE;
        LocalDate yesterday = today.minusDays(1);

        // 오늘 2300: 오늘분만 존재 (어제 fcst 없음)
        given(kmaClient.getVillageForecast(nx, ny, today, "2300")).willReturn(List.of(
            item("TMP", today, "0600", today, "2300", nx, ny, "18")
        ));
        // 어제 2300: 어제분 제공 → 보완 조회 성립
        given(kmaClient.getVillageForecast(nx, ny, yesterday, "2300")).willReturn(List.of(
            item("TMP", yesterday, "2300", yesterday, "2300", nx, ny, "15")
        ));

        List<KmaItem> result = provider.fetchLatestItems(nx, ny);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(i -> i.fcstDate().equals(today.format(DATE))));
        assertTrue(
            result.stream().anyMatch(i -> i.fcstDate().equals(yesterday.format(DATE))));
        verify(cacheManager, never()).getCache(any());
    }

    @Test
    void 동일키_충돌시_base가_더_최근인_것으로_dedup() {
        int nx = 60, ny = 120;
        LocalDate d = FIXED_DATE;
        LocalDate yesterday = d.minusDays(1);

        given(kmaClient.getVillageForecast(nx, ny, d, "2300")).willReturn(List.of(
            item("TMP", d, "0900", d, "2300", nx, ny, "22"),
            item("TMP", yesterday, "2300", d, "2300", nx, ny, "18")
        ));

        List<KmaItem> result = provider.fetchLatestItems(nx, ny);

        assertThat(result.stream()
            .filter(i -> i.category().equals("TMP")
                && i.fcstDate().equals(d.format(DATE))
                && i.fcstTime().equals("0900")))
            .hasSize(1)
            .first()
            .satisfies(i -> assertThat(i.baseTime()).isEqualTo("2300"));
        verify(cacheManager, never()).getCache(any());
    }

    @Test
    void 모든_base가_비면_세번_시도하고_각각_evict_후_빈리스트() {
        int nx = 1, ny = 1;
        LocalDate d = FIXED_DATE;

        given(cacheManager.getCache("kma:village")).willReturn(cache);
        given(kmaClient.getVillageForecast(nx, ny, d, "2300")).willReturn(List.of());
        given(kmaClient.getVillageForecast(nx, ny, d, "2000")).willReturn(List.of());
        given(kmaClient.getVillageForecast(nx, ny, d, "1700")).willReturn(List.of());

        List<KmaItem> result = provider.fetchLatestItems(nx, ny);

        assertThat(result).isEmpty();
        verify(kmaClient).getVillageForecast(nx, ny, d, "2300");
        verify(kmaClient).getVillageForecast(nx, ny, d, "2000");
        verify(kmaClient).getVillageForecast(nx, ny, d, "1700");
        verify(cache).evictIfPresent(eq(key(nx, ny, d, "2300")));
        verify(cache).evictIfPresent(eq(key(nx, ny, d, "2000")));
        verify(cache).evictIfPresent(eq(key(nx, ny, d, "1700")));
    }

    private static String key(int nx, int ny, LocalDate baseDate, String baseTime) {
        return String.format("%d:%d:%s:%s", nx, ny, baseDate.format(DATE), baseTime);
    }

    private static KmaItem item(String category, LocalDate fcstDate, String fcstTime,
        LocalDate baseDate, String baseTime,
        int nx, int ny, String fcstValue) {
        return new KmaItem(
            category,
            fcstDate.format(DATE),
            fcstTime,
            fcstValue,
            nx,
            ny,
            baseDate.format(DATE),
            baseTime
        );
    }
}
