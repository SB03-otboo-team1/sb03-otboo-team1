package com.onepiece.otboo.domain.recommendation.controller.api;

import com.onepiece.otboo.domain.recommendation.dto.data.RecommendationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 의상 추천 조회 API 인터페이스 Swagger 문서 생성을 위한 API 명세를 정의합니다.
 */
@Tag(name = "추천 관리", description = "추천 관련 API")
public interface RecommendationApi {

    /**
     * 추천 의상을 조회합니다.
     *
     * @param weatherId 날씨 ID
     * @return 추천 의상 목록
     */
    @Operation(summary = "추천 조회", description = "추천 조회 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "추천 조회 성공"),
        @ApiResponse(responseCode = "400", description = "추천 조회 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<RecommendationDto> getRecommendation(
        @Parameter(name = "weatherId", description = "날씨 ID") @RequestParam(required = true) @NotNull UUID weatherId);
}
