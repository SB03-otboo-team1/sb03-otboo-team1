package com.onepiece.otboo.domain.clothes.api;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 의상 관리 API 인터페이스
 * Swagger 문서 생성을 위한 API 명세를 정의합니다.
 */
@Tag(name = "의상 관리", description = "의상 관련 API")
public interface ClothesApi {

  @Operation(summary = "의상 조회", description = "특정 의상의 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "의상 조회 성공"),
      @ApiResponse(responseCode = "404", description = "의상을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  ResponseEntity<CursorPageResponseDto<ClothesDto>> getClothes(@Parameter(description = "의상 ID") @PathVariable UUID id);

}