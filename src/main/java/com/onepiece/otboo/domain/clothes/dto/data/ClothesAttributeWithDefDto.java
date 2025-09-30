package com.onepiece.otboo.domain.clothes.dto.data;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * 의상 속성 DTO
 * 관리자가 등록한 의상 속성의 정의와 속성값을 저장합니다.
 * */
@Builder
public record ClothesAttributeWithDefDto(

    UUID definitionId,
    String definitionName,
    List<String> selectableValues,
    String value
) {

}
