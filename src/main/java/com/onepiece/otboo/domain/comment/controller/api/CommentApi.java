package com.onepiece.otboo.domain.comment.controller.api;

import com.onepiece.otboo.domain.comment.dto.request.CommentCreateRequest;
import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Comment API", description = "피드 댓글 API")
public interface CommentApi {

    @Operation(
        summary = "피드 댓글 등록",
        description = "특정 피드에 댓글을 등록합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "성공",
                content = @Content(
                    schema = @Schema(implementation = CommentDto.class))
            )
        }
    )
    ResponseEntity<CommentDto> createComment(
        @Parameter(description = "댓글을 달 피드 ID", required = true)
        @PathVariable("feedId") UUID feedId,
        @Valid @RequestBody CommentCreateRequest request
    );

    @GetMapping
    @Operation(
        summary = "피드별 댓글 목록 조회 (커서 페이징)",
        description = "특정 피드의 댓글을 최신순으로 조회합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = CursorPageResponseDto.class))
            )
        }
    )
    ResponseEntity<CursorPageResponseDto<CommentDto>> listComments(
        @Parameter(description = "조회 대상 피드 ID", required = true)
        @PathVariable("feedId") UUID feedId,
        @Parameter(description = "다음 페이지 커서(이전 응답의 nextCursor)")
        @RequestParam(required = false) String cursor,
        @Parameter(description = "이후 항목 경계 ID")
        @RequestParam(required = false) UUID idAfter,
        @Parameter(description = "페이지 크기(1~100)")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    );
}
