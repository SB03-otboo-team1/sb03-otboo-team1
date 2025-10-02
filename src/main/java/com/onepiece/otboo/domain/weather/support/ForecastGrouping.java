package com.onepiece.otboo.domain.weather.support;

import com.onepiece.otboo.infra.api.dto.KmaItem;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ForecastGrouping {

    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private static String normalizeDate(String s) {
        // 숫자만 남기고 앞 8자리(yyyyMMdd)만 사용
        if (s == null) {
            return null;
        }
        String digits = s.replaceAll("\\D", ""); // "+0900" 같은 비숫자 제거
        return digits.length() >= 8 ? digits.substring(0, 8) : digits;
    }

    private static String padHHmm(String s) {
        if (s == null) {
            return null;
        }
        String digits = s.replaceAll("\\D", "");
        if (digits.length() == 1) {
            return "000" + digits;
        }
        if (digits.length() == 2) {
            return "00" + digits;
        }
        if (digits.length() == 3) {
            return "0" + digits;
        }
        return digits.substring(0, 4);
    }

    public static Extremes collectDailyExtremes(List<KmaItem> items, LocalDate start,
        LocalDate end) {
        Map<String, Double> tmxByDate = new HashMap<>();
        Map<String, Double> tmnByDate = new HashMap<>();

        for (KmaItem it : items) {
            String dStr = normalizeDate(it.fcstDate());   // ★ 동일하게 정규화
            LocalDate d = LocalDate.parse(dStr, DATE);
            if (d.isBefore(start.minusDays(1)) || d.isAfter(end)) {
                continue;
            }

            if ("TMX".equals(it.category())) {
                toDouble(it).ifPresent(v -> tmxByDate.put(dStr, v));
            } else if ("TMN".equals(it.category())) {
                toDouble(it).ifPresent(v -> tmnByDate.put(dStr, v));
            }
        }
        return new Extremes(tmxByDate, tmnByDate);
    }

    public static Map<ForecastKey, Map<String, KmaItem>> groupByForecastKey(List<KmaItem> items,
        LocalDate start, LocalDate end) {

        Map<ForecastKey, Map<String, KmaItem>> bucket = new LinkedHashMap<>();
        for (KmaItem it : items) {
            String dStr = normalizeDate(it.fcstDate());   // ★ 오프셋 제거
            String tStr = padHHmm(it.fcstTime());         // ★ 4자리 보장

            LocalDate d = LocalDate.parse(dStr, DATE);
            if (d.isBefore(start.minusDays(1)) || d.isAfter(end)) {
                continue;
            }

            ForecastKey key = new ForecastKey(dStr, tStr);
            bucket.computeIfAbsent(key, k -> new HashMap<>()).put(it.category(), it);
        }
        return bucket;
    }

    public static Indices buildIndices(Map<ForecastKey, Map<String, KmaItem>> bucket) {
        Map<ForecastKey, Double> tmpByKey = new HashMap<>();
        Map<ForecastKey, Double> rehByKey = new HashMap<>();
        for (Map.Entry<ForecastKey, Map<String, KmaItem>> entry : bucket.entrySet()) {
            toDouble(entry.getValue().get("TMP")).ifPresent(v -> tmpByKey.put(entry.getKey(), v));
            toDouble(entry.getValue().get("REH")).ifPresent(v -> rehByKey.put(entry.getKey(), v));
        }
        return new Indices(tmpByKey, rehByKey);
    }

    private static Optional<Double> toDouble(KmaItem it) {
        if (it == null || it.fcstValue() == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(it.fcstValue()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}

