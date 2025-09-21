package com.onepiece.otboo.domain.location.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.global.config.TestJpaConfig;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class LocationRepositoryCustomImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private LocationRepository locationRepository;

    @Test
    void 주어진_위경도에_가장_가까운_Location_데이터_1개를_반환한다() {

        // given
        Location seoul = new Location(null, 37.5665, 126.9780,
            60, 127, "서울특별시,중구,명동,");
        Location busan = new Location(null, 35.1796, 129.0756,
            75, 129, "부산광역시,해운대구,");
        Location incheon = new Location(null, 37.4563, 126.7052,
            55, 125, "인천광역시,부평구,");

        em.persist(seoul);
        em.persist(busan);
        em.persist(incheon);
        em.flush();

        double latitude = 37.562;
        double longitude = 126.801;

        // when
        Optional<Location> nearest = locationRepository.findNearest(latitude, longitude);

        // then
        assertTrue(nearest.isPresent());
        assertEquals(nearest.get().getId(), incheon.getId());
    }

    @Test
    void Location_데이터가_없으면_빈_데이터_반환() {

        // when
        Optional<Location> location = locationRepository.findByLatitudeAndLongitude(37.1234, 127.1234);

        // then
        assertTrue(location.isEmpty());
    }

    @Test
    void 동일_거리_후보가_둘일_때_1개의_데이터만_반환() {

        // given
        Location a = new Location(null, 0.0, 1.0,
            10, 43, "dummy,city,");
        Location b = new Location(null, 1.0, 0.0,
            11, 44, "dummy,city2,");

        em.persist(a);
        em.persist(b);
        em.flush();

        // when
        Optional<Location> nearest = locationRepository.findNearest(0.0, 0.0);

        // then
        assertTrue(nearest.isPresent());
        assertThat(nearest.get().getId()).isIn(a.getId(), b.getId());
    }
}