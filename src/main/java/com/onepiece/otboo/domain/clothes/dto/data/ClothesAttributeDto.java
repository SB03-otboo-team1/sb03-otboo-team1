package com.onepiece.otboo.domain.clothes.dto.data;

import java.util.UUID;
import lombok.Builder;

/**
 * 의상 속성값 DTO
 * 관리자가 등록한 의상 속성 정의에 딸린 속성값을 저장합니다.
 * */

@Builder
public record ClothesAttributeDto(

    UUID definitionId,
    String value
) {

}
