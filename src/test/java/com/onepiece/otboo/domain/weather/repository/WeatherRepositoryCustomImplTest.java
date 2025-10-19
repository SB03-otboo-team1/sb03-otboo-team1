package com.onepiece.otboo.domain.weather.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.global.config.TestJpaConfig;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class WeatherRepositoryCustomImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private WeatherRepository weatherRepository;

    @Test
    void 범위_내_데이터만_시간_오름차순으로_반환한다() {
        // given
        Location seoul = new Location(null, 37.5665, 126.9780,
            60, 127, "서울특별시,중구,명동,");
        Location busan = new Location(null, 35.1796, 129.0756,
            75, 129, "부산광역시,해운대구,");

        em.persist(seoul);
        em.persist(busan);

        // 타임라인
        Instant t0 = Instant.parse("2025-01-01T00:00:00Z"); // 경계 하한
        Instant t1 = Instant.parse("2025-01-01T03:00:00Z");
        Instant t2 = Instant.parse("2025-01-01T06:00:00Z"); // 경계 상한
        Instant resultBefore = Instant.parse("2024-12-31T23:59:59Z");
        Instant resultAfter  = Instant.parse("2025-01-01T06:00:01Z");

        // 범위 밖(하한 전/상한 후)
        em.persist(weather(seoul, resultBefore));
        em.persist(weather(seoul, resultAfter));

        // 범위 안(하한/중간/상한) — 서울
        Weather w0 = weather(seoul, t0);
        Weather w1 = weather(seoul, t1);
        Weather w2 = weather(seoul, t2);
        em.persist(w0);
        em.persist(w1);
        em.persist(w2);

        // 같은 시간 데이터라도 다른 위치면 제외되어야 함
        em.persist(weather(busan, t1));

        em.flush();
        em.clear();

        // when
        List<Weather> result = weatherRepository.findRange(seoul.getId(), t0, t2);

        // then
        assertEquals(3, result.size());
    }

    @Test
    void 특정_지역의_날씨_데이터가_없으면_빈_배열을_반환한다() {
        // given: 위치만 만들고 날씨는 저장하지 않음
        Location loc = new Location(null, 37.4375, 126.8054, 57,
            124, "경기도,시흥시,은행동,");
        em.persist(loc);
        em.flush();

        // when
        List<Weather> result = weatherRepository.findRange(
            loc.getId(),
            Instant.parse("2025-01-01T00:00:00Z"),
            Instant.parse("2025-01-02T00:00:00Z")
        );

        // then
        assertTrue(result.isEmpty());
    }

    private Weather weather(Location location, Instant forecastAt) {
        return Weather.builder()
            .forecastAt(forecastAt)
            .forecastedAt(forecastAt.minusSeconds(3600))
            .temperatureCurrent(20.0)
            .skyStatus(SkyStatus.CLEAR)
            .location(location)
            .build();
    }
}