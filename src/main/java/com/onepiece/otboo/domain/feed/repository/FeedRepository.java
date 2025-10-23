package com.onepiece.otboo.domain.feed.repository;

import com.onepiece.otboo.domain.feed.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FeedRepository extends JpaRepository<Feed, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Feed f set f.likeCount = :likeCount where f.id = :feedId")
    int updateLikeCount(@Param("feedId") UUID feedId,
                        @Param("likeCount") long likeCount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Feed f set f.commentCount = f.commentCount + :delta where f.id = :feedId")
    int increaseCommentCount(@Param("feedId") UUID feedId,
                             @Param("delta") long delta);
}