package com.onepiece.otboo.domain.feed.controller.api;

import com.onepiece.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.onepiece.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

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

    @Operation(
        summary = "피드 삭제",
        description = "본인 소유의 피드를 삭제합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "피드 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class
                ))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 부족/소유권 불일치",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class
                ))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 피드",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class
                ))
        )
    })
    @DeleteMapping(value = "/{feedId}")
    ResponseEntity<Void> deleteFeed(@PathVariable UUID feedId);

    @Operation(summary = "피드 수정",
        description = "작성자 본인이 피드 내용을 수정합니다."
    )
    @PatchMapping(value = "/{feedId}", consumes = "application/json", produces = "application/json")
    ResponseEntity<FeedResponse> updateFeed(@PathVariable UUID feedId, @Valid @RequestBody FeedUpdateRequest req);

    @Operation(
        summary = "피드 목록 조회",
        description = "커서 기반(Keyset) 페이지네이션으로 피드 목록을 조회합니다. 다음 페이지 요청 시 `nextCursor`, `nextIdAfter`를 그대로 전달하세요."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "피드 목록 조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CursorPageResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 파라미터",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(produces = "application/json")
    ResponseEntity<CursorPageResponseDto<FeedResponse>> listFeeds(
        @Parameter(
            description = "다음 페이지 커서 (정렬키). sortBy=createdAt일 때 ISO-8601 시간",
            example = "2025-09-10T00:21:17.683Z"
        )
        @RequestParam(required = false) String cursor,

        @Parameter(
            description = "다음 페이지 커서 (동률 우회 키). 마지막 항목의 UUID",
            schema = @Schema(format = "uuid")
        )
        @RequestParam(required = false) UUID idAfter,

        @Parameter(
            description = "페이지 크기 (1~100)",
            example = "10"
        )

        @Min(1)
        @Max(100)
        @RequestParam(defaultValue = "20") int limit,

        @Parameter(
            description = "정렬 기준",
            schema = @Schema(allowableValues = {"createdAt", "likeCount"}),
            example = "createdAt"
        )
        @RequestParam String sortBy,

        @Parameter(
            description = "정렬 방향",
            schema = @Schema(allowableValues = {"ASCENDING", "DESCENDING"}),
            example = "DESCENDING"
        )
        @RequestParam String sortDirection,

        @Parameter(
            description = "내용 키워드 (부분 일치)",
            example = "후드티"
        )
        @RequestParam(required = false) String keywordLike,

        @Parameter(
            description = "하늘 상태",
            schema = @Schema(allowableValues = {"CLEAR", "MOSTLY_CLOUDY", "CLOUDY"}),
            example = "CLEAR"
        )
        @RequestParam(required = false) String skyStatusEqual,

        @Parameter(
            description = "강수 유형",
            schema = @Schema(allowableValues = {"NONE", "RAIN", "RAIN_SNOW", "SNOW", "SHOWER"}),
            example = "NONE"
        )
        @RequestParam(required = false) String precipitationTypeEqual,

        @Parameter(
            description = "작성자 ID 필터",
            schema = @Schema(format = "uuid")
        )
        @RequestParam(required = false) UUID authorIdEqual
    );
}


