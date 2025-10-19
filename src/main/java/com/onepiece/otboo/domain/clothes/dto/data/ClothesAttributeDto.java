package com.onepiece.otboo.domain.clothes.dto.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;

/**
 * 의상 속성값 DTO
 * 관리자가 등록한 의상 속성 정의에 딸린 속성값을 저장합니다.
 * */

@Builder
public record ClothesAttributeDto(
    @NotNull(message = "속성 정의 ID는 필수입니다")
    UUID definitionId,

    @NotBlank(message = "속성 값은 필수입니다")
    @Size(max = 50, message = "속성 값은 50자를 초과할 수 없습니다")
    String value
) {

}
