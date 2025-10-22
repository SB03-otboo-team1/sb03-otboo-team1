package com.onepiece.otboo.domain.clothes.dto.data;

import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import java.util.Map;
import lombok.Builder;

@Builder
public record ParsedClothesInfoDto(

    String clothesName,
    ClothesType clothesType,
    String imageUrl,
    Map<String, String> attributes
) {

}
