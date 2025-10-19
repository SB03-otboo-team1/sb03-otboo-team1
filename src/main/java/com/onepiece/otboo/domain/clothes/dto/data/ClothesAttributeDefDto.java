package com.onepiece.otboo.domain.clothes.dto.data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * 의상 속성 정의 DTO
 * 관리자가 등록한 의상 속성의 정의를 저장합니다.
 * */
@Builder
public record ClothesAttributeDefDto(

    UUID id,
    String name,
    List<String> selectableValues,
    Instant createdAt
) {

}
