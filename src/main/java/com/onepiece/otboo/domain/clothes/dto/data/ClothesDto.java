package com.onepiece.otboo.domain.clothes.dto.data;

import com.onepiece.otboo.domain.clothes.entity.Attributes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import java.util.List;

public record ClothesDto (

    String id,
    String ownerId,
    String name,
    String imageUrl,
    ClothesType type,
    List<Attributes> attributes
) {

}
