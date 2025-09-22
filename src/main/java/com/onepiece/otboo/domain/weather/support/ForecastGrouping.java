package com.onepiece.otboo.domain.weather.support;

import com.onepiece.otboo.infra.api.dto.KmaItem;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ForecastGrouping {

    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    public static Extremes collectDailyExtremes(List<KmaItem> items, LocalDate start,
        LocalDate end) {
        Map<String, Double> tmxByDate = new HashMap<>();
        Map<String, Double> tmnByDate = new HashMap<>();

        for (KmaItem it : items) {
            LocalDate d = LocalDate.parse(it.fcstDate(), DATE);
            if (d.isBefore(start.minusDays(1)) || d.isAfter(end)) {
                continue;
            }

            if ("TMX".equals(it.category())) {
                toDouble(it).ifPresent(v -> tmxByDate.put(it.fcstDate(), v));
            } else if ("TMN".equals(it.category())) {
                toDouble(it).ifPresent(v -> tmnByDate.put(it.fcstDate(), v));
            }
        }
        return new Extremes(tmxByDate, tmnByDate);
    }

    public static Map<ForecastKey, Map<String, KmaItem>> groupByForecastKey(List<KmaItem> items,
        LocalDate start, LocalDate end) {
        Map<ForecastKey, Map<String, KmaItem>> bucket = new LinkedHashMap<>();
        for (KmaItem it : items) {
            LocalDate d = LocalDate.parse(it.fcstDate(), DATE);
            if (d.isBefore(start.minusDays(1)) || d.isAfter(end)) {
                continue;
            }

            ForecastKey key = new ForecastKey(it.fcstDate(), it.fcstTime());
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

