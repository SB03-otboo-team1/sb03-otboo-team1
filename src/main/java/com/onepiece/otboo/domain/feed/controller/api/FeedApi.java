package com.onepiece.otboo.domain.feed.controller.api;

import com.onepiece.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.global.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "피드 관리", description = "피드 관련 API")
public interface FeedApi {

    @Operation(
        summary = "피드 등록",
        description = "작성자가 선택한 옷 조합과 내용으로 피드를 등록합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "피드 등록 성공",
            headers = @Header(
                name = "Location",
                description = "생성된 리소스의 URI",
                schema = @Schema(type = "string", example = "/api/feeds/{id}")
            ),
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FeedResponse.class
                ))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 요청(필드 검증 실패/중복 등)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class
            ))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패(미인증/만료/무효 토큰)",
            content = @Content(
            schema = @Schema(implementation = ErrorResponse.class
            ))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 부족/소유권 불일치",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class
            ))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "관련 자원(사용자/의상/날씨) 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class
            ))
        )
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    ResponseEntity<FeedResponse> createFeed(@Valid @RequestBody FeedCreateRequest feedCreateRequest);
}
