package com.onepiece.otboo.domain.clothes.dto.request;

import java.util.List;
import lombok.Builder;

/**
 * 의상 속성 정의 수정 요청 DTO
 * 관리자가 의상 속성 정의를 수정할 때 사용되는 요청 데이터입니다.
 */
@Builder
public record ClothesAttributeDefUpdateRequest(

    String name,
    List<String> selectableValues
) {

}
