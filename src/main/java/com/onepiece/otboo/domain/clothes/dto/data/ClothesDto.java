package com.onepiece.otboo.domain.clothes.dto.data;

import com.onepiece.otboo.domain.clothes.entity.ClothesType;

import java.util.UUID;
import lombok.Builder;


/**
 * 의상 데이터 DTO
 * 의상의 기본 정보를 담는 데이터 클래스입니다.
 */
@Builder
public record ClothesDto (
    UUID id,
    UUID ownerId,
    String name,
    String imageUrl,
    ClothesType type
    // TODO : 의상 속성 추가해야 함
) {

}
