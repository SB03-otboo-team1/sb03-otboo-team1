package com.onepiece.otboo.domain.comment.repository;

import com.onepiece.otboo.domain.comment.entity.Comment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CommentRepositoryCustom {

    List<Comment> findSliceByFeedIdForCursor(UUID feedId, Instant createdAtLt, UUID idLt,
        int limitPlusOne);
}
