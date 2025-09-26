package com.onepiece.otboo.domain.clothes.controller;

import com.onepiece.otboo.domain.clothes.api.ClothesApi;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.service.ClothesService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  @Operation(summary = "옷 목록 조회", description = "옷 목록 조회 API")
  public ResponseEntity<CursorPageResponseDto<ClothesDto>> getClothes(
      String cursor,
      UUID idAfter,
      int limit,
      ClothesType typeEqual,
      UUID ownerId,
      String sortBy,
      String sortDirection
  ) {
    log.info("의상 목록 조회 API 호출 - 소유자: {}, limit: {}", ownerId, limit);

    CursorPageResponseDto<ClothesDto> response = clothesService.getClothes(
        ownerId, cursor, idAfter, limit, sortBy, sortDirection, typeEqual);

    log.info("의상 목록 조회 성공");

    return ResponseEntity.ok(response);
  }
}
