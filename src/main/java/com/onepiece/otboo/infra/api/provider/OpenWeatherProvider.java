package com.onepiece.otboo.infra.api.provider;

import com.onepiece.otboo.infra.api.client.OpenWeatherClient;
import com.onepiece.otboo.infra.api.client.OpenWeatherClient.Root;
import com.onepiece.otboo.infra.api.dto.KmaItem;
import com.onepiece.otboo.infra.api.mapper.OwmToKmaItemMapper;
import com.onepiece.otboo.infra.converter.LatLonXYConverter;
import com.onepiece.otboo.infra.converter.LatLonXYConverter.Point;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenWeatherProvider implements WeatherProvider {

    private final OpenWeatherClient owmClient;
    private final CacheManager cacheManager;
    private final Clock clock;

    @Override
    public List<KmaItem> fetchLatestItems(double latitude, double longitude) {

        Root root = owmClient.get5Day3HourForecast(latitude, longitude);

        if (root == null || root.list == null || root.list.isEmpty()) {
            log.warn("[OpenWeatherProvider] 날씨 데이터가 없습니다 - 위도: {}, 경도: {}", latitude, longitude);

            Cache cache = cacheManager.getCache("owm:forecast");
            if (cache != null) {
                String key = String.format("%f:%f", latitude, longitude);
                cache.evictIfPresent(key);
                log.debug("[OWM] evicted empty cache entry key: {}, cache: 'owm:forecast'", key);
            }
            return List.of();
        }

        Point p = LatLonXYConverter.latLonToXY(latitude, longitude);

        List<KmaItem> mapped = OwmToKmaItemMapper.map(root, p.x(), p.y());

        if (mapped.isEmpty()) {
            return List.of();
        }

        // KMA 로직과 동등: 중복 제거 (같은 category|fcstDate|fcstTime → base 최신)
        Map<String, KmaItem> dedup = dedupByKeyPreferLatestBase(mapped);
        return new ArrayList<>(dedup.values());
    }

    private Map<String, KmaItem> dedupByKeyPreferLatestBase(List<KmaItem> items) {
        Map<String, KmaItem> dedup = new LinkedHashMap<>();
        for (KmaItem it : items) {
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
}
