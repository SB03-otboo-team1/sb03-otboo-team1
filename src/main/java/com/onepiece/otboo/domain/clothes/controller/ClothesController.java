package com.onepiece.otboo.domain.clothes.controller;

import com.onepiece.otboo.domain.clothes.controller.api.ClothesApi;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.service.ClothesService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortDirection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 의상 관리 컨트롤러
 * 사용자의 의상 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
@Validated
public class ClothesController implements ClothesApi {

  private final ClothesService clothesService;

  @GetMapping
  public ResponseEntity<CursorPageResponseDto<ClothesDto>> getClothes(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam(defaultValue = "15") @Positive @Min(1) int limit,
      @RequestParam(required = false) ClothesType typeEqual,
      @RequestParam UUID ownerId
  ) {
    log.info("의상 목록 조회 API 호출 - 소유자: {}, limit: {}", ownerId, limit);

    String sortBy = "createdAt";
    SortDirection sortDirection = SortDirection.DESCENDING;

    CursorPageResponseDto<ClothesDto> response = clothesService.getClothes(
        ownerId, cursor, idAfter, limit, sortBy, sortDirection, typeEqual);

    return ResponseEntity.ok(response);
  }

  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ClothesDto> createClothes(
        @Valid @RequestPart ClothesCreateRequest request,
        @RequestPart(required = false) MultipartFile imageFile
    ) throws IOException {
      log.info("의상 등록 API 호출 - request: {}", request);

      ClothesDto clothes = clothesService.createClothes(request, imageFile);

      return ResponseEntity.ok(clothes);
    }
}
