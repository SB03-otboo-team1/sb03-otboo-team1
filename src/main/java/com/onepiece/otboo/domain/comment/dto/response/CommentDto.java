package com.onepiece.otboo.domain.comment.dto.response;

import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "피드 댓글 응답")
public record CommentDto(
    UUID id,
    Instant createdAt,
    UUID feedId,
    AuthorDto author,
    String content
) {

}
