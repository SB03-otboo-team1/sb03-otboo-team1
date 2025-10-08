package com.onepiece.otboo.domain.weather.batch.processor;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.entity.WeatherAlertOutbox;
import com.onepiece.otboo.domain.weather.repository.WeatherAlertOutboxRepository;
import com.onepiece.otboo.domain.weather.support.Extremes;
import com.onepiece.otboo.domain.weather.support.ForecastGrouping;
import com.onepiece.otboo.domain.weather.support.ForecastKey;
import com.onepiece.otboo.domain.weather.support.Indices;
import com.onepiece.otboo.domain.weather.support.WeatherFactory;
import com.onepiece.otboo.infra.api.dto.KmaItem;
import com.onepiece.otboo.infra.api.provider.WeatherProvider;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final WeatherAlertOutboxRepository outboxRepository;
    private final WeatherAlertRuleEngine ruleEngine;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    public List<Weather> process(Location location) {
        List<KmaItem> items = weatherProvider.fetchLatestItems(
            location.getLatitude(), location.getLongitude()
        );

        if (items.isEmpty()) {
            log.warn("해당 지역에 대한 날씨 데이터 없음 - id: {}", location.getId());
            return List.of();
        }

        LocalDate today = LocalDate.now(KST);
        LocalDate end = today.plusDays(4);

        Extremes extremes = ForecastGrouping.collectDailyExtremes(items, today, end);
        Map<ForecastKey, Map<String, KmaItem>> bucket = ForecastGrouping.groupByForecastKey(items,
            today, end);
        Indices indices = ForecastGrouping.buildIndices(bucket);

        List<Weather> result = bucket.entrySet().stream()
            .map(e -> WeatherFactory.buildWeather(
                e.getKey(), e.getValue(), extremes, indices, location, today, end
            ))
            .flatMap(Optional::stream)
            .toList();

        // 생성된 날씨 데이터에 대해 알림 생성 여부 판단
        List<WeatherAlertOutbox> alerts = ruleEngine.evaluate(location, result);

        if (!alerts.isEmpty()) {
            outboxRepository.saveAll(alerts);
        }

        log.info("날씨 데이터 {}개 생성 완료 - locationId: {}", result.size(), location.getId());
        return result;
    }
}
