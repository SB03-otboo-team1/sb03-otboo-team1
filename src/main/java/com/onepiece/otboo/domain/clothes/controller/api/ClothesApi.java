package com.onepiece.otboo.domain.clothes.controller.api;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 의상 관리 API 인터페이스 Swagger 문서 생성을 위한 API 명세를 정의합니다.
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

    @Operation(summary = "옷 등록", description = "옷 등록 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "옷 등록 성공"),
        @ApiResponse(responseCode = "400", description = "옷 등록 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ClothesDto> createClothes(
        @Valid @RequestPart(required = true) ClothesCreateRequest request,
        @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException;

    @Operation(summary = "옷 수정", description = "옷 수정 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "옷 수정 성공"),
        @ApiResponse(responseCode = "400", description = "옷 수정 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ClothesDto> updateClothes(
        @PathVariable(name = "clothesId") UUID clothesId,
        @Valid @RequestPart(required = true) ClothesUpdateRequest request,
        @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException;

    @Operation(summary = "옷 삭제", description = "옷 삭제 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "옷 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "옷 삭제 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<Void> deleteClothes(
        @PathVariable(name = "clothesId") UUID clothesId
    );

    @Operation(summary = "구매 링크로 옷 정보 불러오기", description = "구매 링크로 옷 정보 불러오기 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "구매 링크로 옷 정보 불러오기 성공"),
        @ApiResponse(responseCode = "400", description = "구매 링크로 옷 정보 불러오기 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ClothesDto> getClothesByUrl(
        @PathVariable(name = "url") String url
    ) throws IOException;
}