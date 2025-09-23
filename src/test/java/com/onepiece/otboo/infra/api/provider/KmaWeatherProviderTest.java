package com.onepiece.otboo.infra.api.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.infra.api.dto.BaseDt;
import com.onepiece.otboo.infra.api.dto.KmaItem;
import java.lang.reflect.Method;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class KmaWeatherProviderTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> uriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KmaWeatherProvider provider;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd
    private static final ZonedDateTime FIXED_NOW =
        ZonedDateTime.of(2025, 9, 22, 23, 15, 0, 0, KST); // latest base = 2300

    private MockedStatic<ZonedDateTime> zdtMock;
    private final AtomicReference<Map<String, String>> lastQuery =
        new AtomicReference<>(new HashMap<>());

    @BeforeEach
    void setUp() {
        zdtMock = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS);
        zdtMock.when(() -> ZonedDateTime.now(KST)).thenReturn(FIXED_NOW);
        zdtMock.when(ZonedDateTime::now).thenReturn(FIXED_NOW);
    }

    @AfterEach
    void tearDown() {
        if (zdtMock != null) {
            zdtMock.close();
        }
    }

    // ===== Tests =====

    @Test
    void 최신_base부터_조회하고_비면_다음_base로_시도한다() throws Exception {
        // given
        stubWebClientChain(); // ✅ 필요한 테스트에서만 스텁
        int nx = 60, ny = 127;
        BaseDt latest = latestBaseToday(); // 2025-09-22 23:15 → "2300"

        LocalDate d = latest.date();
        String tLatest = latest.time(); // "2300"
        String nextBase = "2000";

        String k1 = key(d, tLatest);
        String k2 = key(d, nextBase);

        Map<String, List<KmaItem>> byBase = new HashMap<>();
        byBase.put(k1, List.of()); // 최신 base는 비어있음
        byBase.put(k2, List.of(
            item("TMP", d, "0900", d, nextBase),
            item("POP", d, "0900", d, nextBase)
        ));
        withVillageResponses(byBase);

        // when
        List<KmaItem> result = provider.fetchLatestItems(nx, ny);

        // then
        assertEquals(2, result.size());
        assertThat(result).allSatisfy(it -> {
            assertThat(it.baseDate()).isEqualTo(d.format(DATE));
            assertThat(it.baseTime()).isEqualTo(nextBase);
        });
    }

    @Test
    void 어제_fcst가_없으면_어제_base로_보완조회한다() throws Exception {
        // given
        stubWebClientChain();
        int nx = 55, ny = 127;
        BaseDt latest = latestBaseToday(); // today=2025-09-22, base=2300
        LocalDate today = latest.date();
        LocalDate yesterday = today.minusDays(1);

        String todayKey = key(today, latest.time()); // 20250922|2300
        String yKey2300 = key(yesterday, "2300");    // 20250921|2300

        Map<String, List<KmaItem>> byBase = new HashMap<>();
        byBase.put(todayKey, List.of(
            item("TMP", today, "0600", today, latest.time()) // 오늘분만 제공
        ));
        byBase.put(yKey2300, List.of(
            item("TMP", yesterday, "2300", yesterday, "2300") // 어제 2300 제공
        ));
        withVillageResponses(byBase);

        // when
        List<KmaItem> result = provider.fetchLatestItems(nx, ny);

        // then (보완 조회로 어제/오늘 둘 다 포함됨)
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(i -> i.fcstDate().equals(yesterday.format(DATE))));
        assertTrue(result.stream().anyMatch(i -> i.fcstDate().equals(today.format(DATE))));
    }

    @Test
    void 동일키_충돌시_base가_더_최근인_것으로_dedup된다() throws Exception {
        // given
        stubWebClientChain();
        int nx = 60, ny = 120;
        BaseDt latest = latestBaseToday(); // today=2025-09-22
        LocalDate d = latest.date();

        String key1700 = key(d, "1700");
        String key2300 = key(d, "2300");

        Map<String, List<KmaItem>> byBase = new HashMap<>();
        byBase.put(key1700, List.of(
            item("TMP", d, "0900", d, "1700")
        ));
        byBase.put(key2300, List.of(
            item("TMP", d, "0900", d, "2300")
        ));
        withVillageResponses(byBase);

        // when
        List<KmaItem> result = provider.fetchLatestItems(nx, ny);

        // then (동일 Key에서 base 최신(2300)만 남아야 함)
        assertEquals(1, result.size());
        KmaItem only = result.get(0);
        assertEquals("TMP", only.category());
        assertEquals(d.format(DATE), only.fcstDate());
        assertEquals("0900", only.fcstTime());
        assertEquals("2300", only.baseTime());
    }

    @Test
    void http에러나_파싱실패시_빈리스트반환() throws Exception {
        // given
        stubWebClientChain();
        given(objectMapper.readValue(anyString(), eq(KmaWeatherProvider.Root.class)))
            .willThrow(new RuntimeException("json parse error"));

        // when
        List<KmaItem> result = provider.fetchLatestItems(60, 120);

        // then
        assertEquals(0, result.size());
    }

    @Test
    void nearestAllowedIndex_커버() throws Exception {
        Method nearestAllowedIndex = KmaWeatherProvider.class
            .getDeclaredMethod("nearestAllowedIndex", String.class);
        nearestAllowedIndex.setAccessible(true);

        assertEquals(0, (int) nearestAllowedIndex.invoke(provider, "2300"));
        assertEquals(1, (int) nearestAllowedIndex.invoke(provider, "2000"));
        assertEquals(1, (int) nearestAllowedIndex.invoke(provider, "2110"));
        assertEquals(2, (int) nearestAllowedIndex.invoke(provider, "1800"));
        assertEquals(2, (int) nearestAllowedIndex.invoke(provider, "0100"));
    }

    private void stubWebClientChain() {
        given(webClient.get()).willAnswer(inv -> uriSpec);

        given(uriSpec.uri(any(Function.class))).willAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Function<UriBuilder, URI> fn = inv.getArgument(0, Function.class);
            URI uri = fn.apply(UriComponentsBuilder.fromUriString("http://test"));
            lastQuery.set(parseQuery(uri.getQuery()));
            return uriSpec;
        });

        given(uriSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
        given(responseSpec.bodyToMono(eq(String.class))).willReturn(Mono.just("ok"));
    }

    private BaseDt latestBaseToday() {
        ZonedDateTime now = ZonedDateTime.now(KST);
        LocalDate d = now.toLocalDate();
        LocalTime t = now.toLocalTime();
        String[] BASE_TIMES = {"2300", "2000", "1700"};
        for (String bt : BASE_TIMES) {
            LocalTime cand = LocalTime.of(Integer.parseInt(bt) / 100, 0);
            if (!t.isBefore(cand)) {
                return new BaseDt(d, bt);
            }
        }
        return new BaseDt(d.minusDays(1), "2300");
    }

    private void withVillageResponses(Map<String, List<KmaItem>> byBase) throws Exception {
        given(objectMapper.readValue(anyString(), eq(KmaWeatherProvider.Root.class)))
            .willAnswer(inv -> wrap(byBase.getOrDefault(
                key(lastQuery.get().get("base_date"), lastQuery.get().get("base_time")),
                List.of()
            )));
    }

    private static Map<String, String> parseQuery(String qs) {
        Map<String, String> m = new HashMap<>();
        if (qs == null || qs.isEmpty()) {
            return m;
        }
        for (String p : qs.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2) {
                m.put(kv[0], kv[1]);
            }
        }
        return m;
    }

    private static String key(LocalDate date, String baseTime) {
        return date.format(DATE) + "|" + baseTime;
    }

    private static String key(String baseDate, String baseTime) {
        return baseDate + "|" + baseTime;
    }

    private static KmaItem item(String category, LocalDate fcstDate, String fcstTime,
        LocalDate baseDate, String baseTime) {
        KmaItem item = Mockito.mock(KmaItem.class);
        Mockito.lenient().when(item.category()).thenReturn(category);
        Mockito.lenient().when(item.fcstDate()).thenReturn(fcstDate.format(DATE));
        Mockito.lenient().when(item.fcstTime()).thenReturn(fcstTime);
        Mockito.lenient().when(item.baseDate()).thenReturn(baseDate.format(DATE));
        Mockito.lenient().when(item.baseTime()).thenReturn(baseTime);
        Mockito.lenient().when(item.withBase(anyString(), anyString())).thenAnswer(inv -> {
            String bd = inv.getArgument(0, String.class);
            String bt = inv.getArgument(1, String.class);
            KmaItem copy = Mockito.mock(KmaItem.class);
            Mockito.lenient().when(copy.category()).thenReturn(category);
            Mockito.lenient().when(copy.fcstDate()).thenReturn(fcstDate.format(DATE));
            Mockito.lenient().when(copy.fcstTime()).thenReturn(fcstTime);
            Mockito.lenient().when(copy.baseDate()).thenReturn(bd);
            Mockito.lenient().when(copy.baseTime()).thenReturn(bt);
            return copy;
        });
        return item;
    }

    private static KmaWeatherProvider.Root wrap(List<KmaItem> items) {
        KmaWeatherProvider.Root r = new KmaWeatherProvider.Root();
        KmaWeatherProvider.Resp resp = new KmaWeatherProvider.Resp();
        KmaWeatherProvider.Header h = new KmaWeatherProvider.Header();
        h.resultCode = "00";
        h.resultMsg = "OK";
        KmaWeatherProvider.Body b = new KmaWeatherProvider.Body();
        KmaWeatherProvider.Items it = new KmaWeatherProvider.Items();
        it.item = items;
        b.items = it;
        resp.header = h;
        resp.body = b;
        r.response = resp;
        return r;
    }
}
