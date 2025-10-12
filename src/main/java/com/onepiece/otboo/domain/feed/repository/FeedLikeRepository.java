package com.onepiece.otboo.domain.feed.repository;

import com.onepiece.otboo.domain.feed.entity.FeedLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {

    boolean existsByUserIdAndFeedId(UUID userId, UUID feedId);

    Optional<FeedLike> findByUserIdAndFeedId(UUID userId, UUID feedId);

    long countByFeedId(UUID feedId);

    @Query("""
        select fl.feed.id
        from FeedLike fl
        where fl.user.id = :userId and fl.feed.id in :feedIds
    """)
    List<UUID> findLikedFeedIds(UUID userId, List<UUID> feedIds);
}
