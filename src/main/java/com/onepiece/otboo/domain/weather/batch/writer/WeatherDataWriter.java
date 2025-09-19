package com.onepiece.otboo.domain.weather.batch.writer;

import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class WeatherDataWriter implements ItemWriter<List<Weather>> {

    private final WeatherRepository weatherRepository;

    // 대량 저장 시 한 번에 너무 많이 flush하지 않도록 분할 크기(상황에 따라 조절)
    private static final int BATCH_SIZE = 1000;

    @Override
    public void write(Chunk<? extends List<Weather>> chunk) {
        // 1) 평탄화
        List<Weather> flat = chunk.getItems()
            .stream()
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .filter(Objects::nonNull)
            .toList();

        if (flat.isEmpty()) {
            log.warn("[WeatherDataWriter] 날씨 데이터가 비어있습니다.");
            return;
        }

        // 2) 중복 제거 (키: locationId + forecastAt)
        Map<String, Weather> dedup = new LinkedHashMap<>();
        for (Weather w : flat) {
            UUID locationId = (w.getLocation() != null) ? w.getLocation().getId() : null;
            Instant forecastAt = w.getForecastAt();
            if (locationId == null || forecastAt == null) {
                // 키 구성 불가 데이터는 스킵(원하면 로그만 남기고 continue)
                log.debug("[WeatherDataWriter] 키 정보 누락으로 스킵: {}", w);
                continue;
            }
            String key = locationId + "|" + forecastAt;
            // 동일 키가 여러 번 오면 최초 항목 유지(원하면 최신으로 덮어쓰기)
            dedup.putIfAbsent(key, w);
        }

        List<Weather> toSave = new ArrayList<>(dedup.values());
        if (toSave.isEmpty()) {
            log.warn("[WeatherDataWriter] 중복 제거 후 저장할 데이터가 없습니다.");
            return;
        }

        // 3) 분할 저장
        try {
            for (int i = 0; i < toSave.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, toSave.size());
                List<Weather> slice = toSave.subList(i, end);
                // Spring Data JPA 사용 시 saveAllInBatch가 있으면 더 효율적일 수 있음:
                // weatherRepository.saveAllInBatch(slice);
                weatherRepository.saveAll(slice);
                log.info("[WeatherDataWriter] 날씨 데이터 {}개 저장 완료 ({}~{})",
                    slice.size(), i, end - 1);
            }
        } catch (Exception e) {
            log.error("날씨 데이터 저장 실패", e);
            throw e; // 스텝의 fault-tolerant/skip 설정에 따라 처리됨
        }
    }
}
