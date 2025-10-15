package com.onepiece.otboo.domain.clothes.controller.api;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 의상 관리 API 인터페이스. Swagger 문서 생성을 위한 API 명세를 정의합니다.
 */
@Tag(name = "의상 관리", description = "의상 관련 API")
public interface ClothesApi {

    /**
     * 의상 정보를 조회합니다.
     *
     * @param cursor    커서 (선택사항)
     * @param idAfter   ID 이후 (선택사항)
     * @param limit     페이지 크기
     * @param typeEqual 의상 타입 (선택사항)
     * @param ownerId   소유자 ID
     * @return 의상 목록
     */
    @Operation(summary = "옷 목록 조회", description = "옷 목록 조회 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "옷 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "옷 목록 조회 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<CursorPageResponseDto<ClothesDto>> getClothes(
        @Parameter(description = "커서") @RequestParam(required = false) String cursor,
        @Parameter(description = "다음 ID 커서") @RequestParam(required = false) UUID idAfter,
        @Parameter(description = "페이지 크기") @RequestParam(required = true, defaultValue = "15") @Positive @Min(1) int limit,
        @Parameter(description = "의상 타입") @RequestParam(required = false) ClothesType typeEqual,
        @Parameter(description = "소유자 ID") @RequestParam(required = true) UUID ownerId
    );

}