package com.onepiece.otboo.infra.api.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

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

    private final AtomicReference<Map<String, String>> lastQuery = new AtomicReference<>(
        new HashMap<>());

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    private void stubWebClientChain() {
        given(webClient.get()).willAnswer(inv -> uriSpec);

        // uri(Function<UriBuilder, URI>)을 캡쳐해 query 파싱
        given(uriSpec.uri(any(Function.class))).willAnswer(inv -> {
            Function<UriBuilder, URI> fn = inv.getArgument(0, Function.class);
            URI uri = fn.apply(UriComponentsBuilder.fromUriString("http://test"));
            Map<String, String> q = parseQuery(uri.getQuery());
            lastQuery.set(q);
            return uriSpec;
        });

        given(uriSpec.retrieve()).willReturn(responseSpec);

        given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);

        given(responseSpec.bodyToMono(eq(String.class))).willReturn(Mono.just("ok"));
    }
    // 유틸: query string -> map
    private Map<String, String> parseQuery(String qs) {
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

    // 테스트 내에서 프로바이더의 "오늘 기준 최신 base" 로직을 동일하게 계산
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

    // KmaItem 목을 안전하게 생성 (필요 필드만)
    private KmaItem mockItem(String category, String fcstDate, String fcstTime,
        String baseDate, String baseTime) {
        KmaItem item = Mockito.mock(KmaItem.class);

        // 호출될 수도/안 될 수도 있는 것들은 모두 lenient()
        Mockito.lenient().when(item.category()).thenReturn(category);
        Mockito.lenient().when(item.fcstDate()).thenReturn(fcstDate);
        Mockito.lenient().when(item.fcstTime()).thenReturn(fcstTime);

        Mockito.lenient().doAnswer(inv -> {
            String bd = inv.getArgument(0, String.class);
            String bt = inv.getArgument(1, String.class);

            KmaItem copy = Mockito.mock(KmaItem.class);
            Mockito.lenient().when(copy.category()).thenReturn(category);
            Mockito.lenient().when(copy.fcstDate()).thenReturn(fcstDate);
            Mockito.lenient().when(copy.fcstTime()).thenReturn(fcstTime);
            Mockito.lenient().when(copy.baseDate()).thenReturn(bd);
            Mockito.lenient().when(copy.baseTime()).thenReturn(bt);
            return copy;
        }).when(item).withBase(anyString(), anyString());

        // 원본의 base*는 보통 안 쓰이지만 혹시 모를 경우 대비(역시 lenient)
        Mockito.lenient().when(item.baseDate()).thenReturn(baseDate);
        Mockito.lenient().when(item.baseTime()).thenReturn(baseTime);

        return item;
    }

    // objectMapper.readValue("ok", Root.class)의 동작을, 마지막 쿼리(base_date, base_time)에 따라 동적으로 구성
    private void stubObjectMapperForVillageResponses(Map<String, List<KmaItem>> byBase)
        throws Exception {
        given(objectMapper.readValue(anyString(), eq(KmaWeatherProvider.Root.class)))
            .willAnswer(inv -> {
                Map<String, String> q = lastQuery.get();
                String key =
                    q.getOrDefault("base_date", "") + "|" + q.getOrDefault("base_time", "");
                List<KmaItem> items = byBase.getOrDefault(key, List.of());
                return wrap(items); // Root 객체로 포장
            });
    }

    // Root 포장 유틸
    private KmaWeatherProvider.Root wrap(List<KmaItem> items) {
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

    @Test
    void 최신_base부터_조회하고_비면_다음_base로_시도한다() throws Exception {

        // given
        stubWebClientChain();
        int nx = 60, ny = 127;
        BaseDt latest = latestBaseToday();

        // 최신 base → 빈 리스트, 다음 base → 아이템 2개
        LocalDate d = latest.date();
        String tLatest = latest.time();
        // 다음 허용 base 계산(2300->2000->1700 순)
        String nextBase = switch (tLatest) {
            case "2300" -> "2000";
            case "2000" -> "1700";
            default -> null; // 1700이면 더 내려갈 게 없음 → 그대로 비어 있게 둬도 무방
        };

        String k1 = d.format(DATE) + "|" + tLatest;
        String k2 = d.format(DATE) + "|" + (nextBase == null ? tLatest : nextBase);

        Map<String, List<KmaItem>> byBase = new HashMap<>();
        byBase.put(k1, List.of()); // 최신 base는 비어있음
        byBase.put(k2, List.of(
            mockItem("TMP", d.format(DATE), "0900", d.format(DATE),
                (nextBase == null ? tLatest : nextBase)),
            mockItem("POP", d.format(DATE), "0900", d.format(DATE),
                (nextBase == null ? tLatest : nextBase))
        ));
        stubObjectMapperForVillageResponses(byBase);

        // when
        List<KmaItem> result = provider.fetchLatestItems(nx, ny);

        // then
        assertEquals(2, result.size());
        // 모두 nextBase의 baseTime을 가지는지 확인
        String expectedBaseTime = (nextBase == null ? tLatest : nextBase);
        assertThat(result).allSatisfy(it -> {
            assertThat(it.baseDate()).isEqualTo(d.format(DATE));
            assertThat(it.baseTime()).isEqualTo(expectedBaseTime);
        });
    }

    @Test
    void 어제_fcst가_없으면_어제_base로_보완조회한다() throws Exception {

        // given
        stubWebClientChain();
        int nx = 55;
        int ny = 127;
        BaseDt latest = latestBaseToday();
        LocalDate today = latest.date();
        LocalDate yesterday = today.minusDays(1);

        // 오늘 데이터만 반환(어제 fcst 없음) → 보완 로직이 어제 base(2300 또는 2000)도 조회
        String todayKey = today.format(DATE) + "|" + latest.time();
        String yKey2300 = yesterday.format(DATE) + "|2300";

        Map<String, List<KmaItem>> byBase = new HashMap<>();
        byBase.put(todayKey, List.of(
            mockItem("TMP", today.format(DATE), "0600", today.format(DATE), latest.time())
        ));
        // 어제 2300에서 한 건 제공
        byBase.put(yKey2300, List.of(
            mockItem("TMP", yesterday.format(DATE), "2300", yesterday.format(DATE), "2300")
        ));
        // (2000은 호출 전 break 되므로 비워둬도 OK)
        stubObjectMapperForVillageResponses(byBase);

        // when
        List<KmaItem> result = provider.fetchLatestItems(nx, ny);

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(i -> i.fcstDate().equals(yesterday.format(DATE))));
        assertTrue(result.stream().anyMatch(i -> i.fcstDate().equals(today.format(DATE))));
    }

    @Test
    void 동일키_충돌시_base가_더_최근인_것으로_dedup된다() throws Exception {

        // given
        stubWebClientChain();
        int nx = 60;
        int ny = 120;
        BaseDt latest = latestBaseToday();
        LocalDate d = latest.date();

        // 같은 (category|fcstDate|fcstTime) = (TMP|today|0900)
        // base (today|1700) vs (today|2300) → 2300이 남아야 함
        String key1700 = d.format(DATE) + "|1700";
        String key2300 = d.format(DATE) + "|2300";

        Map<String, List<KmaItem>> byBase = new HashMap<>();
        byBase.put(key1700, List.of(
            mockItem("TMP", d.format(DATE), "0900", d.format(DATE), "1700")
        ));
        byBase.put(key2300, List.of(
            mockItem("TMP", d.format(DATE), "0900", d.format(DATE), "2300")
        ));
        stubObjectMapperForVillageResponses(byBase);

        // when
        List<KmaItem> result = provider.fetchLatestItems(nx, ny);

        // then
        assertEquals(1, result.size());
        KmaItem only = result.get(0);
        assertEquals("TMP", only.category());
        assertEquals(d.format(DATE), only.fcstDate());
        assertEquals("0900", only.fcstTime());
        assertEquals("1700", only.baseTime());
    }

    @Test
    void http에러나_파싱실패시_빈리스트반환() throws Exception {

        // given
        stubWebClientChain();
        int nx = 60;
        int ny = 120;

        given(objectMapper.readValue(anyString(), eq(KmaWeatherProvider.Root.class)))
            .willThrow(new RuntimeException("json parse error"));

        // when
        List<KmaItem> result = provider.fetchLatestItems(nx, ny);

        // then
        assertEquals(0, result.size());
    }

    @Test
    void nearestAllowedIndex_커버() throws Exception {
        // given
        Method nearestAllowedIndex = KmaWeatherProvider.class
            .getDeclaredMethod("nearestAllowedIndex", String.class);
        nearestAllowedIndex.setAccessible(true);

        // when
        // 정확히 일치하면 해당 인덱스
        int idxEq2300 = (int) nearestAllowedIndex.invoke(provider, "2300");
        int idxEq2000 = (int) nearestAllowedIndex.invoke(provider, "2000");
        // 허용 리스트에 없고 사이값이면 "해당 값 이하 중 가장 큰 허용값"의 인덱스
        int idx2110 = (int) nearestAllowedIndex.invoke(provider, "2110"); // → 2000(인덱스 1)
        int idx1800 = (int) nearestAllowedIndex.invoke(provider, "1800"); // → 1700(인덱스 2)
        // 허용 리스트보다 훨씬 작아도 기본은 맨 마지막(1700)
        int idx0100 = (int) nearestAllowedIndex.invoke(provider, "0100"); // → 1700(인덱스 2)

        // then
        assertEquals(0, idxEq2300);
        assertEquals(1, idxEq2000);
        assertEquals(1, idx2110);
        assertEquals(2, idx1800);
        assertEquals(2, idx0100);
    }
}
