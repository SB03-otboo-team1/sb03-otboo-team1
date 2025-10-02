package com.onepiece.otboo.domain.clothes.dto.request;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * 의상 등록 요청 DTO
 * 사용자가 의상을 등록할 때 사용되는 요청 데이터입니다.
 */
@Builder
public record ClothesCreateRequest(
    @NotNull(message = "옷장 소유자 ID는 필수입니다")
    UUID ownerId,
    @NotBlank(message = "의상 이름은 필수입니다")
    @Size(max = 100, message = "의상 이름은 100자를 초과할 수 없습니다")
    String name,

    @NotNull(message = "의상 타입은 필수입니다")
    ClothesType type,

    @Valid
    @NotEmpty(message = "최소 하나의 속성이 필요합니다")
    @Size(max = 50, message = "속성은 최대 50개까지 등록 가능합니다")
    List<ClothesAttributeDto> attributes
) {
}
