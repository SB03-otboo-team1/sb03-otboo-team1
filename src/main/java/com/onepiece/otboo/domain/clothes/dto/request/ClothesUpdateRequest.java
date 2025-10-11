package com.onepiece.otboo.domain.clothes.dto.request;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

/**
 * 의상 수정 요청 DTO. 사용자가 의상 정보를 수정할 때 사용되는 요청 데이터입니다.
 */
@Builder
public record ClothesUpdateRequest(
    @NotBlank(message = "의상 이름은 필수입니다")
    @Size(max = 100, message = "의상 이름은 100자를 초과할 수 없습니다")
    String name,

    @NotNull(message = "의상 타입은 필수입니다")
    ClothesType type,

    @Valid
    @Size(max = 50, message = "속성은 최대 50개까지 등록 가능합니다")
    List<ClothesAttributeDto> attributes
) {

}
