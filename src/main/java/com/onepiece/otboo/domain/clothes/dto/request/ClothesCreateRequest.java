package com.onepiece.otboo.domain.clothes.dto.request;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * 의상 등록 요청 DTO
 * 사용자가 의상을 등록할 때 사용되는 요청 데이터입니다.
 */
@Builder
public record ClothesCreateRequest(
    UUID ownerId,
    String name,
    ClothesType type,
    List<ClothesAttributeDto> attributes
) {
}
