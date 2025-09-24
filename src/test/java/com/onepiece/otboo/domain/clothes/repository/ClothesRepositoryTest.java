package com.onepiece.otboo.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.mapper.ClothesMapper;
import com.onepiece.otboo.global.config.TestJpaConfig;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
public class ClothesRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ClothesRepository clothesRepository;

    @MockitoBean
    private ClothesMapper clothesMapper;

    private Clothes createClothes(UUID ownerId, String name, ClothesType type, String imageUrl) {
        Clothes clothes = Clothes.builder()
            .ownerId(ownerId)
            .name(name)
            .type(type)
            .imageUrl(imageUrl)
            .build();
        return clothes;
    }

    @Test
    void 옷목록_커서페이징_조회_성공() {
        // given
        UUID ownerId = UUID.randomUUID();
        Clothes c1 = createClothes(ownerId, "흰 셔츠", ClothesType.TOP, "https://test-bucket.s3.ap-northeast-2.amazonaws.com/test-key");
        Clothes c2 = createClothes(ownerId, "청바지", ClothesType.BOTTOM, "https://test-bucket.s3.ap-northeast-2.amazonaws.com/test-key-2");

        em.persist(c1);
        em.persist(c2);
        em.flush();
        em.clear();

        // when
        CursorPageResponseDto<ClothesDto> result = clothesRepository.findCursorPage(
            ownerId, null, null, 10, "id", "asc", null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(2);
        assertThat(result.totalCount()).isEqualTo(2L);
    }

    @Test
    void 옷목록_조회_실패() {
        // given
        UUID ownerId = UUID.randomUUID();

        // when
        CursorPageResponseDto<ClothesDto> result = clothesRepository.findCursorPage(
            ownerId,
            null,
            null,
            10,
            "id",
            "asc",
            ClothesType.TOP
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isEmpty();
        assertThat(result.totalCount()).isZero();
        assertThat(result.hasNext()).isFalse();
    }
}
