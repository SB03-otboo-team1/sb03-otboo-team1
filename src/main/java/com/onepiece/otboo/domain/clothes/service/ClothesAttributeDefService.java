package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

public interface ClothesAttributeDefService {

    List<ClothesAttributeDefDto> getClothesAttributeDefs(
        SortBy sortBy, SortDirection sortDirection, String keywordLike
    );

    ClothesAttributeDefDto createClothesAttributeDef(@Valid ClothesAttributeDefCreateRequest request);

    ClothesAttributeDefDto updateClothesAttributeDef(UUID definitionId, @Valid ClothesAttributeDefUpdateRequest request);
}
