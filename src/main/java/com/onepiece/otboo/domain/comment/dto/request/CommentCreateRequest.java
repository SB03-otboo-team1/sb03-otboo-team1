package com.onepiece.otboo.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "피드 댓글 등록 요청")
public record CommentCreateRequest(
    @Schema(description = "본문에 있을 수 있으나 path의 feedId와 일치해야 함", nullable = true)
    UUID feedId,

    @Schema(description = "작성자 사용자 ID(UUID)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    UUID authorId,

    @Schema(description = "댓글 내용(최대 200자)", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 200)
    @NotBlank @Size(max = 200)
    String content
) {

}
