package com.onepiece.otboo.domain.clothes.dto.data;

import java.util.Map;
import lombok.Builder;

@Builder
public record ParsedClothesInfoDto(

    String clothesName,
    String clothesType,
    String imageUrl,
    Map<String, String> attributes
) {

}
