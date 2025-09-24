package com.onepiece.otboo.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.mapper.ClothesMapper;
import com.onepiece.otboo.domain.clothes.repository.ClothesRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClothesServiceTest {

    @Mock
    ClothesRepository clothesRepository;

    @Mock
    ClothesMapper clothesMapper;

    @InjectMocks
    ClothesServiceImpl clothesService;

    @Test
    void 옷목록_커서_페이징으로_조회_성공() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID idAfter = UUID.randomUUID();
        int limit = 5;
        String sortBy = "id";
        String sortDirection = "asc";
        ClothesType typeEqual = ClothesType.TOP;

        CursorPageResponseDto<ClothesDto> response =
            new CursorPageResponseDto<>(
                List.of(new ClothesDto(UUID.randomUUID(), ownerId, "셔츠", "string", ClothesType.TOP)),
                "next-cursor",
                UUID.randomUUID(),
                true,
                10L,
                sortBy,
                sortDirection
            );

        when(clothesRepository.findCursorPage(ownerId, null, idAfter, limit, sortBy, sortDirection, typeEqual))
            .thenReturn(response);

        // when
        CursorPageResponseDto<ClothesDto> result =
            clothesService.getClothes(ownerId, null, idAfter, limit, sortBy, sortDirection, typeEqual);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        assertThat(result.totalCount()).isEqualTo(10L);

        verify(clothesRepository, times(1))
            .findCursorPage(ownerId, null, idAfter, limit, sortBy, sortDirection, typeEqual);
    }

    @Test
    void 옷목록_조회_실패() {
        // given
        UUID ownerId = UUID.randomUUID();
        when(clothesRepository.findCursorPage(any(), any(), any(), anyInt(), any(), any(), any()))
            .thenThrow(new RuntimeException("DB 오류 발생"));

        // when & then
        assertThatThrownBy(() ->
            clothesService.getClothes(ownerId, null, null, 5, "createdAt", "ASCENDING", ClothesType.TOP)
        )
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("DB 오류 발생");

        verify(clothesRepository, times(1))
            .findCursorPage(any(), any(), any(), anyInt(), any(), any(), any());
    }

}
