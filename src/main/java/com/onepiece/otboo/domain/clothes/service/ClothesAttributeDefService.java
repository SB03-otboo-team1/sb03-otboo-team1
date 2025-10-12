package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.util.List;

public interface ClothesAttributeDefService {

    List<ClothesAttributeDefDto> getClothesAttributeDefs(
        SortBy sortBy, SortDirection sortDirection, String keywordLike
    );
}
