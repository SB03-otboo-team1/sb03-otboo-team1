package com.onepiece.otboo.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.global.config.TestJpaConfig;
import com.onepiece.otboo.global.config.TestJpaConfig.MutableDateTimeProvider;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class ClothesRepositoryTest {

    @Autowired
    private ClothesRepository clothesRepository;

    @Autowired
    private MutableDateTimeProvider time;

    private UUID ownerId;

    @BeforeEach
    void 데이터_준비() {
        ownerId = UUID.randomUUID();
        User owner = UserFixture.createUser("test@test.com");
        ownerId = UUID.randomUUID();

        ReflectionTestUtils.setField(owner, "id", ownerId);

        time.setNow(Instant.parse("2025-09-29T13:00:00Z"));
        Clothes shirt = Clothes.builder()
            .owner(owner)
            .name("셔츠")
            .type(ClothesType.TOP)
            .build();

        time.setNow(Instant.parse("2025-09-29T13:30:00Z"));
        Clothes pants = Clothes.builder()
            .owner(owner)
            .name("바지")
            .type(ClothesType.BOTTOM)
            .build();

        clothesRepository.save(shirt);
        clothesRepository.save(pants);
    }

    @Test
    void 옷_목록_커서기반_조회_성공() {
        List<Clothes> result = clothesRepository.getClothesWithCursor(
            ownerId, null, null, 10, SortBy.CREATED_AT, SortDirection.DESCENDING, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void 타입조건_없이_옷_목록_카운트_성공() {
        Long count = clothesRepository.countClothes(ownerId, null);
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void 특정타입_옷_목록_카운트_성공() {
        Long countTop = clothesRepository.countClothes(ownerId, ClothesType.TOP);
        assertThat(countTop).isEqualTo(1L);
    }

    @Test
    void 데이터_없을_때_옷_목록_조회결과_빈리스트() {
        List<Clothes> result = clothesRepository.getClothesWithCursor(
            UUID.randomUUID(), null, null, 10, SortBy.CREATED_AT, SortDirection.DESCENDING, null);

        assertThat(result).isEmpty();
    }

    @Test
    void 데이터_없을_때_옷_목록_카운트_0_반환() {
        Long count = clothesRepository.countClothes(UUID.randomUUID(), null);
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void 잘못된_정렬_기준으로_옷_목록_조회_실패() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            clothesRepository.getClothesWithCursor(
                ownerId, null, null, 10, null, SortDirection.ASCENDING, null
            );
        });
    }
}
