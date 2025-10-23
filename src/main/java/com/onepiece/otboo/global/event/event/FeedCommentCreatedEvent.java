package com.onepiece.otboo.global.event.event;

import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import java.time.Instant;

public record FeedCommentCreatedEvent(
    CommentDto data,
    Instant createdAt
) {

}