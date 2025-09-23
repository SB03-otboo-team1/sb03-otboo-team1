package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;

@Service
public interface ClothesService {

  CursorPageResponseDto<ClothesDto> getClothes(UUID ownerId, String cursor, UUID idAfter, int limit, String sortBy, String sortDirection, ClothesType typeEqual);
}
