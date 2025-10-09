package com.onepiece.otboo.domain.comment.controller.api;

import com.onepiece.otboo.domain.comment.dto.request.CommentCreateRequest;
import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Operation(summary = "피드별 댓글 목록 조회")
    @ApiResponses({/* ... */})
    CursorPageResponseDto<CommentDto> list(
        /* Swagger 문서용 파라미터 설명만 유지 */
        @Parameter(description = "댓글을 조회할 피드 ID") UUID feedId,
        @Parameter(description = "커서") String cursor,
        @Parameter(description = "타이브레이커 ID") UUID idAfter,
        @Parameter(description = "페이지 크기(1~100)") Integer limit
    );
}
