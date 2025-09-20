package com.onepiece.otboo.domain.weather.batch.processor;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.enums.PrecipitationType;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.domain.weather.enums.WindSpeedWord;
import com.onepiece.otboo.global.util.NumberConverter;
import com.onepiece.otboo.infra.api.dto.KmaItem;
import com.onepiece.otboo.infra.api.provider.WeatherProvider;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class Weather5DayProcessor implements ItemProcessor<Location, List<Weather>> {

    private final WeatherProvider weatherProvider;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    @Override
    public List<Weather> process(Location location) {
        List<KmaItem> items = weatherProvider.fetchLatestItems(
            location.getXCoordinate(),
            location.getYCoordinate()
        );

        if (items.isEmpty()) {
            log.warn("해당 지역에 대한 날씨 데이터가 없습니다. id: {} (nx: {}, ny: {})",
                location.getId(), location.getXCoordinate(), location.getYCoordinate());
            return List.of();
        }

        // 어제~+4일만 (전날 비교를 위해 어제 데이터도 버킷에 포함)
        LocalDate today = LocalDate.now(KST);
        LocalDate end = today.plusDays(4);

        // 날짜별 TMX/TMN 미리 수집
        Map<String, Double> tmxByDate = new HashMap<>();
        Map<String, Double> tmnByDate = new HashMap<>();

        // fcstDate|fcstTime 단위로 카테고리를 묶기
        Map<String, Map<String, KmaItem>> bucket = new LinkedHashMap<>();
        for (KmaItem it : items) {
            LocalDate d = LocalDate.parse(it.fcstDate(), DATE);
            if (d.isBefore(today.minusDays(1)) || d.isAfter(end)) {
                continue;
            }

            String dateKey = it.fcstDate();
            String category = it.category();

            if ("TMX".equals(category)) {
                Double v = toDouble(it);
                if (v != null) {
                    tmxByDate.put(dateKey, v);
                }
            } else if ("TMN".equals(category)) {
                Double v = toDouble(it);
                if (v != null) {
                    tmnByDate.put(dateKey, v);
                }
            }

            bucket.computeIfAbsent(it.fcstDate() + "|" + it.fcstTime(), k -> new HashMap<>())
                .put(category, it);
        }

        // 시각별 TMP/REH 인덱스 (yyyyMMdd|HHmm → 값)
        Map<String, Double> tmpByKey = new HashMap<>();
        Map<String, Double> rehByKey = new HashMap<>();
        for (Map.Entry<String, Map<String, KmaItem>> entry : bucket.entrySet()) {
            Map<String, KmaItem> m = entry.getValue();
            Double tmp = toDouble(m.get("TMP"));
            Double reh = toDouble(m.get("REH"));
            if (tmp != null) {
                tmpByKey.put(entry.getKey(), tmp);
            }
            if (reh != null) {
                rehByKey.put(entry.getKey(), reh);
            }
        }

        List<Weather> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, KmaItem>> entry : bucket.entrySet()) {
            Map<String, KmaItem> m = entry.getValue();
            KmaItem sample = m.values().iterator().next(); // baseDate/time, nx/ny 참고

            String dateKey = sample.fcstDate();
            String timeKey = sample.fcstTime();

            LocalDate d = LocalDate.parse(dateKey, DATE);
            // 결과 생성은 오늘~+4일만 (어제는 비교를 위한 참조만)
            if (d.isBefore(today) || d.isAfter(end)) {
                continue;
            }

            // 필드 매핑
            Double tmp = toDouble(m.get("TMP"));
            if (tmp == null) {
                continue;
            }

            // 동일 시각의 전날 값 조회 (없으면 ±1~2시간 근처 탐색)
            String prevDateKey = d.minusDays(1).format(DATE);
            Double tmpPrev = lookupSameOrNearest(tmpByKey, prevDateKey, timeKey);
            Double reh = toDouble(m.get("REH"));
            Double rehPrev = lookupSameOrNearest(rehByKey, prevDateKey, timeKey);

            Double deltaTmp =
                (tmpPrev != null) ? NumberConverter.round(tmp - tmpPrev, 1) : 0.0;
            Double deltaReh =
                (reh != null && rehPrev != null) ? NumberConverter.round(reh - rehPrev, 1)
                    : 0.0;

            // TMX/TMN 보강(해당 시각 버킷에 없으면 날짜별 값으로 대체)
            Double tmx = toDouble(m.get("TMX"));
            Double tmn = toDouble(m.get("TMN"));
            if (tmx == null) {
                tmx = tmxByDate.get(dateKey);
            }
            if (tmn == null) {
                tmn = tmnByDate.get(dateKey);
            }

            Double pop = toDouble(m.get("POP"));
            Double wsd = parseWsdToDouble(m.get("WSD"));
            WindSpeedWord wsw = mapWindSpeedWord(m.get("WSD"));
            Double pcp = parsePcpAmountOrCode(m.get("PCP"));
            SkyStatus sky = mapSky(m.get("SKY"));
            PrecipitationType pty = mapPty(m.get("PTY"));

            Instant forecastedAt = toKstInstant(sample.baseDate(),
                sample.baseTime());  // 발표시각(base)
            Instant forecastAt = toKstInstant(sample.fcstDate(), sample.fcstTime());    // 예보시각

            Weather w = Weather.builder()
                .forecastedAt(forecastedAt)
                .forecastAt(forecastAt)
                .temperatureCurrent(tmp)
                .temperatureMax(tmx)
                .temperatureMin(tmn)
                .skyStatus(sky)
                .precipitationAmount(pcp)
                .precipitationProbability(pop)
                .precipitationType(pty)
                .windSpeed(wsd)
                .windSpeedWord(wsw)
                .humidity(reh)
                .temperatureComparedToDayBefore(deltaTmp)
                .humidityComparedToDayBefore(deltaReh)
                .location(location)
                .build();

            result.add(w);
        }

        log.info("날씨 데이터 {}개 생성 완료 - locationId: {}",
            result.size(), location.getId());
        return result;
    }

    private Instant toKstInstant(String yyyymmdd, String hhmm) {
        LocalDate d = LocalDate.parse(yyyymmdd, DATE);
        LocalTime t = LocalTime.of(Integer.parseInt(hhmm) / 100, Integer.parseInt(hhmm) % 100);
        return ZonedDateTime.of(d, t, KST).toInstant();
    }

    private Double toDouble(KmaItem it) {
        if (it == null || it.fcstValue() == null) {
            return null;
        }
        try {
            return Double.valueOf(it.fcstValue());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 정수만으로 구성되었는지(정성 코드값 판별)
    private boolean isPureInt(String v) {
        return v != null && v.trim().matches("\\d+");
    }

    // PCP: 코드(1/2/3) 또는 문자열("1~4mm", "50mm이상", "강수없음") -> mm/h 하한값
    private Double parsePcpAmountOrCode(KmaItem it) {
        if (it == null || it.fcstValue() == null) {
            return 0.0;
        }
        String v = it.fcstValue().trim();

        // 코드값 처리
        if (isPureInt(v)) {
            int code = Integer.parseInt(v);
            return switch (code) {
                case 1 -> 0.1;  // 약한 비: <3mm/h → 0.1 (0은 '강수없음'과 구분)
                case 2 -> 3.0;  // 보통 비: 3~15 → 3
                case 3 -> 15.0; // 강한 비: ≥15 → 15
                default -> 0.0;
            };
        }

        if (v.contains("강수없음")) {
            return 0.0;
        }

        Matcher mRange = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*~").matcher(v); // "1~4mm"
        if (mRange.find()) {
            return Double.valueOf(mRange.group(1));
        }

        Matcher mNum = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(v); // "50mm 이상", "3mm"
        if (mNum.find()) {
            return Double.valueOf(mNum.group(1));
        }

        return null;
    }

    private Double parseWsdToDouble(KmaItem wsdItem) {
        if (wsdItem == null || wsdItem.fcstValue() == null) {
            return null;
        }
        String v = wsdItem.fcstValue().trim();

        if (isPureInt(v)) {
            int code = Integer.parseInt(v);
            return switch (code) {
                case 1 -> 4.0; // 약한 바람: ≥4 → 4.0 (대표값 조정 가능)
                case 2 -> 4.0; // 약간 강한 바람: 4~9 → 4.0
                case 3 -> 9.0; // 강한 바람: ≥9 → 9.0
                default -> null;
            };
        }

        try {
            return Double.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * WSD → WindSpeedWord 매핑 - 코드값: 1=WEAK, 2=MODERATE, 3=STRONG - 숫자(m/s): <4 → WEAK, 4~<9 →
     * MODERATE, >=9 → STRONG
     */
    private WindSpeedWord mapWindSpeedWord(KmaItem wsdItem) {
        if (wsdItem == null || wsdItem.fcstValue() == null) {
            return null;
        }
        String v = wsdItem.fcstValue().trim();

        if (isPureInt(v)) {
            return switch (Integer.parseInt(v)) {
                case 1 -> WindSpeedWord.WEAK;
                case 2 -> WindSpeedWord.MODERATE;
                case 3 -> WindSpeedWord.STRONG;
                default -> null;
            };
        }

        Double speed = toDouble(wsdItem);
        if (speed == null) {
            return null;
        }
        if (speed < 4.0) {
            return WindSpeedWord.WEAK;
        }
        if (speed < 9.0) {
            return WindSpeedWord.MODERATE;
        }
        return WindSpeedWord.STRONG;
    }

    private SkyStatus mapSky(KmaItem it) {
        if (it == null) {
            return null;
        }
        try {
            int code = Integer.parseInt(it.fcstValue());
            // 1 맑음, 3 구름많음, 4 흐림
            return switch (code) {
                case 1 -> SkyStatus.CLEAR;
                case 3 -> SkyStatus.MOSTLY_CLOUDY;
                case 4 -> SkyStatus.CLOUDY;
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private PrecipitationType mapPty(KmaItem it) {
        if (it == null) {
            return null;
        }
        try {
            int code = Integer.parseInt(it.fcstValue());
            // 0 없음, 1 비, 2 비/눈, 3 눈, 4 소나기, (5~7: 빗방울/눈날림 등은 필요시 확장)
            return switch (code) {
                case 0 -> PrecipitationType.NONE;
                case 1 -> PrecipitationType.RAIN;
                case 2 -> PrecipitationType.RAIN_SNOW;
                case 3 -> PrecipitationType.SNOW;
                case 4 -> PrecipitationType.SHOWER;
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 같은 HHmm가 없으면 ±1시간 → ±2시간까지 근접 시각을 탐색
     */
    private Double lookupSameOrNearest(Map<String, Double> map, String dateKey, String timeKey) {
        String k = dateKey + "|" + timeKey;
        if (map.containsKey(k)) {
            return map.get(k);
        }

        int hh = Integer.parseInt(timeKey.substring(0, 2));
        int mm = Integer.parseInt(timeKey.substring(2, 4));

        int[] hourOffsets = {-1, 1, -2, 2};
        for (int off : hourOffsets) {
            int nh = hh + off;
            if (nh < 0 || nh > 23) {
                continue;
            }
            String t2 = String.format("%02d%02d", nh, mm);
            String k2 = dateKey + "|" + t2;
            if (map.containsKey(k2)) {
                return map.get(k2);
            }
        }
        return null;
    }
}
