package com.onepiece.otboo.domain.clothes.dto.request;

import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * 의상 등록 요청 DTO
 * 사용자가 의상을 등록할 때 사용되는 요청 데이터입니다.
 */
@Builder
public record ClothesCreateRequest(
    UUID ownerId,
    String name,
    ClothesType type
    // TODO : 의상 속성 추가해야 함
) {
}
