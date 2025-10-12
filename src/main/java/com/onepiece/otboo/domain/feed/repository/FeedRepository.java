package com.onepiece.otboo.domain.feed.repository;

import com.onepiece.otboo.domain.feed.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface FeedRepository extends JpaRepository<Feed, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Feed f set f.likeCount = :likeCount where f.id = :feedId")
    void updateLikeCount(UUID feedId, long likeCount);
}