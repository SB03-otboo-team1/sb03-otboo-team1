package com.onepiece.otboo.domain.clothes.dto.request;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import java.util.List;
import lombok.Builder;

/**
 * 의상 수정 요청 DTO
 * 사용자가 의상 정보를 수정할 때 사용되는 요청 데이터입니다.
 */
@Builder
public record ClothesUpdateRequest(
    String name,
    ClothesType type,
    List<ClothesAttributeDto> attributes
) {

}
