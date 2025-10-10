package com.onepiece.otboo.domain.clothes.dto.request;

import java.util.List;
import lombok.Builder;

/**
 * 의상 속성 등록 요청 DTO
 * 관리자가 의상 속성을 등록할 때 사용되는 요청 데이터입니다.
 */
@Builder
public record ClothesAttributeDefCreateRequest(

    String name,
    List<String> selectableValues
) {

}
