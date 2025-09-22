package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ClothesRepository 테스트 클래스
 * 의상 데이터 접근 계층의 기능을 테스트합니다.
 */
@DataJpaTest
class ClothesRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClothesRepository clothesRepository;

    private Clothes testClothes;

    @BeforeEach
    void setUp() {
        testClothes = Clothes.builder()
                .name("테스트 의상")
                .type(ClothesType.TOP)
                .imageUrl("https://example.com/image.jpg")
                .purchaseLink("https://musinsa.com/product/123")
                .brand("테스트 브랜드")
                .price(50000)
                .color("블랙")
                .size("M")
                .material("면")
                .description("테스트용 의상입니다.")
                .isPublic(true)
                .build();

        entityManager.persistAndFlush(testClothes);
    }

    @Test
    @DisplayName("공개된 의상 목록을 페이징하여 조회한다")
    void findByIsPublicTrue() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Clothes> result = clothesRepository.findByIsPublicTrue(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("테스트 의상");
        assertThat(result.getContent().get(0).getIsPublic()).isTrue();
    }

    @Test
    @DisplayName("특정 타입의 의상 목록을 조회한다")
    void findByTypeAndIsPublicTrue() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Clothes> result = clothesRepository.findByTypeAndIsPublicTrue(ClothesType.TOP, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo(ClothesType.TOP);
    }

    @Test
    @DisplayName("브랜드명으로 의상을 검색한다")
    void findByBrandContainingIgnoreCaseAndIsPublicTrue() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Clothes> result = clothesRepository.findByBrandContainingIgnoreCaseAndIsPublicTrue("테스트", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBrand()).contains("테스트");
    }

    @Test
    @DisplayName("의상명으로 의상을 검색한다")
    void findByNameContainingIgnoreCaseAndIsPublicTrue() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Clothes> result = clothesRepository.findByNameContainingIgnoreCaseAndIsPublicTrue("의상", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).contains("의상");
    }

    @Test
    @DisplayName("가격 범위로 의상을 검색한다")
    void findByPriceRangeAndIsPublicTrue() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Integer minPrice = 40000;
        Integer maxPrice = 60000;

        // when
        Page<Clothes> result = clothesRepository.findByPriceRangeAndIsPublicTrue(minPrice, maxPrice, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPrice()).isBetween(minPrice, maxPrice);
    }

    @Test
    @DisplayName("색상으로 의상을 검색한다")
    void findByColorContainingIgnoreCaseAndIsPublicTrue() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Clothes> result = clothesRepository.findByColorContainingIgnoreCaseAndIsPublicTrue("블랙", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getColor()).contains("블랙");
    }

    @Test
    @DisplayName("구매 링크로 의상을 조회한다")
    void findByPurchaseLink() {
        // given
        String purchaseLink = "https://musinsa.com/product/123";

        // when
        Optional<Clothes> result = clothesRepository.findByPurchaseLink(purchaseLink);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPurchaseLink()).isEqualTo(purchaseLink);
    }

    @Test
    @DisplayName("복합 검색을 수행한다")
    void findByComplexSearch() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        ClothesType type = ClothesType.TOP;
        String brand = "테스트";
        Integer minPrice = 40000;
        Integer maxPrice = 60000;
        String color = "블랙";

        // when
        Page<Clothes> result = clothesRepository.findByComplexSearch(type, brand, minPrice, maxPrice, color, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        Clothes clothes = result.getContent().get(0);
        assertThat(clothes.getType()).isEqualTo(type);
        assertThat(clothes.getBrand()).contains(brand);
        assertThat(clothes.getPrice()).isBetween(minPrice, maxPrice);
        assertThat(clothes.getColor()).contains(color);
    }

    @Test
    @DisplayName("존재하지 않는 구매 링크로 조회하면 빈 결과를 반환한다")
    void findByPurchaseLinkNotFound() {
        // given
        String nonExistentLink = "https://nonexistent.com/product/999";

        // when
        Optional<Clothes> result = clothesRepository.findByPurchaseLink(nonExistentLink);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("비공개 의상은 조회되지 않는다")
    void findByIsPublicTrueExcludesPrivateClothes() {
        // given
        Clothes privateClothes = Clothes.builder()
                .name("비공개 의상")
                .type(ClothesType.BOTTOM)
                .isPublic(false)
                .build();
        entityManager.persistAndFlush(privateClothes);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Clothes> result = clothesRepository.findByIsPublicTrue(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("테스트 의상");
    }
}