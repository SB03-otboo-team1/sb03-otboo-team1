package com.onepiece.otboo.domain.clothes.api;

import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.onepiece.otboo.domain.clothes.dto.response.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * 의상 관리 API 인터페이스
 * Swagger 문서 생성을 위한 API 명세를 정의합니다.
 */
@Tag(name = "의상 관리", description = "의상 관련 API")
public interface ClothesApi {

  @Operation(summary = "의상 등록", description = "새로운 의상을 등록합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "의상 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  ResponseEntity<ClothesDto> createClothes(@RequestBody ClothesCreateRequest request);

  @Operation(summary = "의상 조회", description = "특정 의상의 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "의상 조회 성공"),
      @ApiResponse(responseCode = "404", description = "의상을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  ResponseEntity<ClothesDto> getClothes(@Parameter(description = "의상 ID") @PathVariable UUID id);

  @Operation(summary = "의상 수정", description = "의상 정보를 수정합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "의상 수정 성공"),
      @ApiResponse(responseCode = "404", description = "의상을 찾을 수 없음"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  ResponseEntity<ClothesDto> updateClothes(
      @Parameter(description = "의상 ID") @PathVariable UUID id,
      @RequestBody ClothesUpdateRequest request);

  @Operation(summary = "의상 삭제", description = "의상을 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "의상 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "의상을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  ResponseEntity<Void> deleteClothes(@Parameter(description = "의상 ID") @PathVariable UUID id);

  @Operation(summary = "의상 목록 조회", description = "의상 목록을 페이징하여 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "의상 목록 조회 성공"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  ResponseEntity<CursorPageResponse<ClothesDto>> getClothesList();

  @Operation(summary = "타입별 의상 목록 조회", description = "특정 타입의 의상 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "타입별 의상 목록 조회 성공"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  ResponseEntity<CursorPageResponse<ClothesDto>> getClothesByType(
      @Parameter(description = "의상 타입") @PathVariable ClothesType type,
      Pageable pageable);
}