package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortDirection;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ClothesService {

  CursorPageResponseDto<ClothesDto> getClothes(UUID ownerId, String cursor, UUID idAfter, int limit, String sortBy, SortDirection sortDirection, ClothesType typeEqual);

    ClothesDto createClothes(ClothesCreateRequest request, MultipartFile imageFile);
}
