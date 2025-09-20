package com.onepiece.otboo.infra.api.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.infra.api.dto.BaseDt;
import com.onepiece.otboo.infra.api.dto.KmaItem;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KmaWeatherProvider implements WeatherProvider {

    private final WebClient weatherApiClient;
    private final ObjectMapper objectMapper;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd
    private static final String[] BASE_TIMES = {"2300", "2000", "1700"};
    private static final int NUM_ROWS = 1000;

    @Override
    public List<KmaItem> fetchLatestItems(int x, int y) {
        BaseDt latestBase = resolveLatestBaseToday(); // 오늘 기준으로 잡기
        List<KmaItem> acc = new ArrayList<>();

        int startIdx = indexOf(BASE_TIMES, latestBase.time());
        if (startIdx < 0) {
            // latestBase가 허용목록에 없다면, 가장 가까운 이전 허용 base를 선택
            startIdx = nearestAllowedIndex(latestBase.time());
        }
        for (int i = startIdx; i < BASE_TIMES.length; i++) {
            BaseDt tb = new BaseDt(latestBase.date(), BASE_TIMES[i]);
            List<KmaItem> items = callVillageOnce(x, y, tb.date(), tb.time());
            if (!items.isEmpty()) {
                acc.addAll(items);
                break;
            }
        }
        if (acc.isEmpty()) {
            log.warn("[KMA] no data for today base nx={}, ny={}", x, y);
            return List.of();
        }

        // 2) 어제 fcstDate가 없으면, 어제 base도 1~2개 보완 조회
        LocalDate today = ZonedDateTime.now(KST).toLocalDate();
        boolean hasYesterday = acc.stream().anyMatch(it ->
            LocalDate.parse(it.fcstDate(), DATE).isEqual(today.minusDays(1))
        );
        if (!hasYesterday) {
            LocalDate prevDate = today.minusDays(1);
            int[] prevCandidates = {2300, 2000}; // 필요시 1700까지
            for (int bt : prevCandidates) {
                String baseTime = String.format("%04d", bt);
                List<KmaItem> prev = callVillageOnce(x, y, prevDate, baseTime);
                if (!prev.isEmpty()) {
                    acc.addAll(prev);
                    break;
                }
            }
        }

        // 3) 중복 제거: 같은 (category|fcstDate|fcstTime)은 base 최신(=baseDate+baseTime 큰 것)으로
        Map<String, KmaItem> dedup = getStringKmaItemMap(acc);

        return new ArrayList<>(dedup.values());
    }

    private int indexOf(String[] arr, String v) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(v)) {
                return i;
            }
        }
        return -1;
    }

    private int nearestAllowedIndex(String time) {
        // time은 "HHmm". time 이하 중 가장 큰 허용 base 반환 (없으면 0)
        for (int i = 0; i < BASE_TIMES.length; i++) {
            if (time.compareTo(BASE_TIMES[i]) == 0) {
                return i;
            }
            if (time.compareTo(BASE_TIMES[i]) > 0) {
                return i; // 23/20/17 내에서 가장 가까운 과거
            }
        }
        return BASE_TIMES.length - 1; // 기본값: 1700
    }

    private Map<String, KmaItem> getStringKmaItemMap(List<KmaItem> acc) {
        Map<String, KmaItem> dedup = new LinkedHashMap<>();
        for (KmaItem it : acc) {
            String key = it.category() + "|" + it.fcstDate() + "|" + it.fcstTime();
            KmaItem ex = dedup.get(key);
            if (ex == null) {
                dedup.put(key, it);
            } else {
                String oldBase = ex.baseDate() + ex.baseTime();
                String newBase = it.baseDate() + it.baseTime();
                if (oldBase.compareTo(newBase) < 0) {
                    dedup.put(key, it);
                }
            }
        }
        return dedup;
    }

    // 오늘 기준으로 베이스 선택
    private BaseDt resolveLatestBaseToday() {
        ZonedDateTime now = ZonedDateTime.now(KST);
        LocalDate d = now.toLocalDate();
        LocalTime t = now.toLocalTime();
        for (String bt : BASE_TIMES) {
            LocalTime cand = LocalTime.of(Integer.parseInt(bt) / 100, 0);
            if (!t.isBefore(cand)) {
                return new BaseDt(d, bt);
            }
        }
        return new BaseDt(d.minusDays(1), "2300");
    }


    private List<KmaItem> callVillageOnce(int nx, int ny, LocalDate baseDate, String baseTime) {
        try {
            String json = weatherApiClient.get()
                .uri(u -> u.path("/VilageFcstInfoService_2.0/getVilageFcst")
                    .queryParam("numOfRows", NUM_ROWS)
                    .queryParam("pageNo", 1)
                    .queryParam("nx", nx)
                    .queryParam("ny", ny)
                    .queryParam("base_date", baseDate.format(DATE))
                    .queryParam("base_time", baseTime)
                    .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                    res.bodyToMono(String.class).flatMap(b ->
                        Mono.error(
                            new RuntimeException("KMA HTTP " + res.statusCode() + " body=" + b))
                    )
                )
                .bodyToMono(String.class)
                .block();

            Root root = objectMapper.readValue(json, Root.class);
            if (root == null || root.response == null || root.response.header == null) {
                log.warn("KMA empty/invalid JSON nx={},ny={},base={}{}",
                    nx, ny, baseDate.format(DATE), baseTime);
                return List.of();
            }
            String code = root.response.header.resultCode;
            String msg = root.response.header.resultMsg;
            if (!"00".equals(code)) {
                log.warn("KMA resultCode={} msg={} nx={},ny={},base={}{}",
                    code, msg, nx, ny, baseDate.format(DATE), baseTime);
                return List.of();
            }

            List<KmaItem> items = Optional.ofNullable(root.response.body)
                .map(b -> b.items)
                .map(i -> i.item)
                .orElse(List.of());

            String baseStr = baseDate.format(DATE);
            List<KmaItem> out = new ArrayList<>(items.size());
            for (KmaItem it : items) {
                out.add(it.withBase(baseStr, baseTime));
            }
            return out;
        } catch (Exception e) {
            log.error("KMA call error nx={}, ny={}, base={}{}", nx, ny, baseDate.format(DATE),
                baseTime, e);
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Root {

        public Resp response;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Resp {

        public Header header;
        public Body body;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Header {

        public String resultCode;
        public String resultMsg;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Body {

        public Items items;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Items {

        public List<KmaItem> item;
    }
}
