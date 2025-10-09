package com.onepiece.otboo.domain.comment.repository;

import com.onepiece.otboo.domain.comment.entity.Comment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {
    long countByFeed_Id(UUID feedId);
}
