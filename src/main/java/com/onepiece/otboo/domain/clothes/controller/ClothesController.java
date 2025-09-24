package com.onepiece.otboo.domain.clothes.controller;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.service.ClothesService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 의상 관리 컨트롤러
 * 사용자의 의상 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothesController {

  private final ClothesService clothesService;

  /**
   * 의상 정보를 조회합니다.
   *
   * @param cursor 커서 (선택사항)
   * @param idAfter ID 이후 (선택사항)
   * @param limit 페이지 크기
   * @param typeEqual 의상 타입 (선택사항)
   * @param ownerId 소유자 ID
   * @param sortBy 정렬 기준
   * @param sortDirection 정렬 방향
   * @return 의상 목록
   */
  @GetMapping
  @Operation(summary = "옷 목록 조회", description = "옷 목록 조회 API")
  public ResponseEntity<CursorPageResponseDto<ClothesDto>> getClothes(
      @Parameter(description = "커서") @RequestParam(required = false) String cursor,
      @Parameter(description = "ID 이후") @RequestParam(required = false) UUID idAfter,
      @Parameter(description = "페이지 크기") @RequestParam(required = true, defaultValue = "15") int limit,
      @Parameter(description = "의상 타입") @RequestParam(required = false) ClothesType typeEqual,
      @Parameter(description = "소유자 ID") @Valid @RequestParam(required = true) UUID ownerId,
      @Parameter(description = "정렬 기준") @RequestParam(required = true, defaultValue = "id") String sortBy,
      @Parameter(description = "정렬 방향") @RequestParam(required = true, defaultValue = "asc") String sortDirection
  ) {
    log.info("의상 목록 조회 API 호출 - 소유자: {}, limit: {}", ownerId, limit);

    CursorPageResponseDto<ClothesDto> response = clothesService.getClothes(
        ownerId, cursor, idAfter, limit, sortBy, sortDirection, typeEqual);

    log.info("의상 목록 조회 성공");

    return ResponseEntity.ok(response);
  }
}
