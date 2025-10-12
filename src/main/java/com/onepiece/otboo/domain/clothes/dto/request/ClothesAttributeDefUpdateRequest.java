package com.onepiece.otboo.domain.clothes.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

/**
 * 의상 속성 정의 수정 요청 DTO
 * 관리자가 의상 속성 정의를 수정할 때 사용되는 요청 데이터입니다.
 */
@Builder
@Valid
public record ClothesAttributeDefUpdateRequest(

    @NotBlank(message = "속성 이름은 필수입니다")
    @Size(max = 100, message = "속성 이름은 100자를 초과할 수 없습니다")
    String name,

    @NotEmpty(message = "속성값은 비어있을 수 없습니다")
    List<
        @NotBlank(message = "속성값은 빈 값일 수 없습니다")
        @Size(max = 50, message = "각 선택값은 50자를 초과할 수 없습니다")
            String> selectableValues
) {

}
