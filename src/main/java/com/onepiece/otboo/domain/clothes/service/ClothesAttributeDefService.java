package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.enums.SortBy;

public interface ClothesAttributeDefService {

    CursorPageResponseDto<ClothesAttributeDefDto> getClothesAttributeDefsWithCursor(
        SortBy sortBy, SortDirection sortDirection, String keywordLike
    );
}
