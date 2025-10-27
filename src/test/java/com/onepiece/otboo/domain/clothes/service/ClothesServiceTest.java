package com.onepiece.otboo.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.mapper.ClothesAttributeMapper;
import com.onepiece.otboo.domain.clothes.mapper.ClothesMapper;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeOptionsRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.event.event.ClothesAttributeAddedEvent;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ClothesServiceTest {

    // -------------------------
    // ClothesServiceImpl 관련
    // -------------------------
    @Mock
    private ClothesRepository clothesRepository;

    @Mock
    private ClothesAttributeRepository attributeRepository;

    @Mock
    private ClothesAttributeOptionsRepository optionsRepository;

    @Mock
    private ClothesMapper clothesMapper;

    @InjectMocks
    private ClothesServiceImpl clothesService;

    // -------------------------
    // ClothesAttributeDefServiceImpl 관련
    // -------------------------
    @Mock
    private ClothesAttributeDefRepository clothesAttributeDefRepository;

    @Mock
    private ClothesAttributeMapper clothesAttributeMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ClothesAttributeDefServiceImpl clothesAttributeDefService;

    @Test
    void 옷_목록_조회_성공_hasNext_false() {
        UUID ownerId = UUID.randomUUID();
        User owner = UserFixture.createUser("test@test.com");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Clothes clothes = Clothes.builder()
            .owner(owner)
            .name("코트")
            .type(ClothesType.TOP)
            .build();

        ClothesDto dto = ClothesDto.builder()
            .id(clothes.getId())
            .name(clothes.getName())
            .type(clothes.getType())
            .build();

        given(clothesRepository.getClothesWithCursor(ownerId, null, null, 10, SortBy.CREATED_AT,
            SortDirection.DESCENDING, null))
            .willReturn(List.of(clothes));
        given(clothesRepository.countClothes(ownerId, null)).willReturn(1L);
        given(clothesMapper.toDto(any(), any(), any())).willReturn(dto);

        CursorPageResponseDto<ClothesDto> response = clothesService.getClothesWithCursor(
            ownerId, null, null, 10, SortBy.CREATED_AT, SortDirection.DESCENDING, null);

        assertThat(response.data()).hasSize(1);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.totalCount()).isEqualTo(1L);

        verify(clothesRepository).getClothesWithCursor(ownerId, null, null, 10, SortBy.CREATED_AT,
            SortDirection.DESCENDING, null);
        verify(clothesRepository).countClothes(ownerId, null);
    }

    @Test
    void 옷_목록_조회_성공_hasNext_true() {
        // given
        UUID ownerId = UUID.randomUUID();
        User owner = UserFixture.createUser("test@test.com");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID clothes1Id = UUID.randomUUID();
        UUID clothes2Id = UUID.randomUUID();

        Clothes clothes1 = Clothes.builder()
            .owner(owner)
            .name("셔츠")
            .type(ClothesType.TOP)
            .build();

        Clothes clothes2 = Clothes.builder()
            .owner(owner)
            .name("바지")
            .type(ClothesType.BOTTOM)
            .build();

        ReflectionTestUtils.setField(clothes1, "id", clothes1Id);
        ReflectionTestUtils.setField(clothes1, "createdAt", Instant.now());
        ReflectionTestUtils.setField(clothes2, "id", clothes2Id);
        ReflectionTestUtils.setField(clothes2, "createdAt", Instant.now().plusSeconds(10));

        ClothesDto dto1 = ClothesDto.builder()
            .id(clothes1Id)
            .name(clothes1.getName())
            .type(clothes1.getType())
            .build();

        ClothesDto dto2 = ClothesDto.builder()
            .id(clothes2Id)
            .name(clothes2.getName())
            .type(clothes2.getType())
            .build();

        given(clothesRepository.getClothesWithCursor(ownerId, null, null, 1, SortBy.CREATED_AT,
            SortDirection.DESCENDING, null))
            .willReturn(List.of(clothes1, clothes2)); // limit+1 -> hasNext
        given(clothesRepository.countClothes(ownerId, null)).willReturn(2L);
        given(clothesMapper.toDto(any(), any(), any())).willReturn(dto1);

        CursorPageResponseDto<ClothesDto> response = clothesService.getClothesWithCursor(
            ownerId, null, null, 1, SortBy.CREATED_AT, SortDirection.DESCENDING, null);

        // when & then
        assertThat(response.data()).hasSize(1);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.totalCount()).isEqualTo(2L);
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.nextIdAfter()).isNotNull();
    }

    @Test
    void 옷_목록_조회_실패_빈결과() {
        UUID ownerId = UUID.randomUUID();

        given(clothesRepository.getClothesWithCursor(ownerId, null, null, 10, SortBy.CREATED_AT,
            SortDirection.DESCENDING, null))
            .willReturn(Collections.emptyList());
        given(clothesRepository.countClothes(ownerId, null)).willReturn(0L);

        CursorPageResponseDto<ClothesDto> response = clothesService.getClothesWithCursor(
            ownerId, null, null, 10, SortBy.CREATED_AT, SortDirection.DESCENDING, null);

        assertThat(response.data()).isEmpty();
        assertThat(response.totalCount()).isEqualTo(0L);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextIdAfter()).isNull();
    }

    @Test
    void 옷_목록_조회_실패_Repository에서_null반환() {
        UUID ownerId = UUID.randomUUID();

        given(clothesRepository.getClothesWithCursor(ownerId, null, null, 10, SortBy.CREATED_AT,
            SortDirection.DESCENDING, null))
            .willReturn(null); // 잘못된 상황 가정
        given(clothesRepository.countClothes(ownerId, null)).willReturn(0L);

        CursorPageResponseDto<ClothesDto> response = clothesService.getClothesWithCursor(
            ownerId, null, null, 10, SortBy.CREATED_AT, SortDirection.DESCENDING, null);

        // null 결과가 들어와도 서비스는 방어적으로 빈 리스트처럼 취급해야 한다고 가정
        assertThat(response.data()).isEmpty();
        assertThat(response.totalCount()).isEqualTo(0L);
        assertThat(response.hasNext()).isFalse();
    }
    
    @Test
    void 의상속성정의_등록시_이벤트_발행된다() {
        ClothesAttributeDefCreateRequest request =
            new ClothesAttributeDefCreateRequest("색상", List.of("빨강", "파랑", "노랑"));

        ClothesAttributeDefs savedDef = ClothesAttributeDefs.builder()
            .name("색상")
            .build();

        given(clothesAttributeDefRepository.save(any())).willReturn(savedDef);
        given(optionsRepository.saveAll(any())).willReturn(List.of());

        clothesAttributeDefService.createClothesAttributeDef(request);

        verify(eventPublisher).publishEvent(any(ClothesAttributeAddedEvent.class));
    }
}
