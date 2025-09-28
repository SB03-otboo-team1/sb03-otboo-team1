package com.onepiece.otboo.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.exception.InvalidClothesSortException;
import com.onepiece.otboo.global.config.TestJpaConfig;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class ClothesRepositoryTest {

    @Autowired
    private ClothesRepository clothesRepository;


    private UUID ownerId;

    @BeforeEach
    void 데이터_준비() {
        ownerId = UUID.randomUUID();

        Clothes shirt = Clothes.builder()
            .ownerId(ownerId)
            .name("셔츠")
            .type(ClothesType.TOP)
            .build();
        ReflectionTestUtils.setField(shirt, "createdAt", Instant.now());

        Clothes pants = Clothes.builder()
                .ownerId(ownerId)
                .name("바지")
                .type(ClothesType.BOTTOM)
                .build();
        ReflectionTestUtils.setField(pants, "createdAt", Instant.now().plusSeconds(100));

        clothesRepository.save(shirt);
        clothesRepository.flush();
        clothesRepository.save(pants);
        clothesRepository.flush();
    }

    @Test
    void 옷_목록_커서기반_조회_성공() {
        List<Clothes> result = clothesRepository.getClothesWithCursor(
            ownerId, null, null, 10, "createdAt", "desc", null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("셔츠");
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
            UUID.randomUUID(), null, null, 10, "createdAt", "asc", null);

        assertThat(result).isEmpty();
    }

    @Test
    void 데이터_없을_때_옷_목록_카운트_0_반환() {
        Long count = clothesRepository.countClothes(UUID.randomUUID(), null);
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void 잘못된_정렬_기준으로_옷_목록_조회_실패() {
        assertThrows(InvalidClothesSortException.class, () -> {
            clothesRepository.getClothesWithCursor(
                ownerId, null, null, 10, "invalidField", "asc", null
            );
        });
    }
}
