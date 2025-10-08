package com.onepiece.otboo.domain.clothes.controller.api;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 의상 속성 정의 관리 API 인터페이스
 * Swagger 문서 생성을 위한 API 명세를 정의합니다.
 */
@Tag(name = "의상 속성 정의", description = "의상 속성 정의 관련 API")
public interface ClothesAttributeDefApi {

    @Operation(summary = "의상 속성 정의 목록 조회", description = "의상 속성 정의 목록 조회 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "의상 속성 정의 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "의상 속성 정의 목록 조회 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<List<ClothesAttributeDefDto>> getClothesAttributeDefs(
        @Parameter(description = "정렬 기준") @RequestParam(required = false, defaultValue = "createdAt") SortBy sortBy,
        @Parameter(description = "정렬 방향") @RequestParam(required = false, defaultValue = "ASCENDING") SortDirection sortDirection,
        @Parameter(description = "검색 키워드") @RequestParam(required = false) String keywordLike
    );

    @Operation(summary = "의상 속성 정의 등록", description = "의상 속성 정의 등록 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "의상 속성 정의 등록 성공"),
        @ApiResponse(responseCode = "400", description = "의상 속성 정의 등록 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ClothesAttributeDefDto> createClothesAttributeDef(
        @RequestBody ClothesAttributeDefCreateRequest request
    );

    @Operation(summary = "의상 속성 정의 수정", description = "의상 속성 정의 수정 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "의상 속성 정의 수정 성공"),
        @ApiResponse(responseCode = "400", description = "의상 속성 정의 수정 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ClothesAttributeDefDto> updateClothesAttributeDef(
        @PathVariable(name = "definitionId") UUID definitionId,
        @RequestBody ClothesAttributeDefUpdateRequest request
    );

    @Operation(summary = "의상 속성 정의 삭제", description = "의상 속성 정의 삭제 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "의상 속성 정의 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "의상 속성 정의 삭제 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<Void> deleteClothesAttributeDef(
        @PathVariable(name = "definitionId") UUID definitionId
    );
}
