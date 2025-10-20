package com.onepiece.otboo.domain.feed.repository;

import com.onepiece.otboo.domain.feed.entity.FeedLike;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {

    boolean existsByUser_IdAndFeed_Id(UUID userId, UUID feedId);

    Optional<FeedLike> findByUser_IdAndFeed_Id(UUID userId, UUID feedId);

    void deleteByUser_IdAndFeed_Id(UUID userId, UUID feedId);

    long countByFeed_Id(UUID feedId);

    List<FeedLike> findAllByUser_IdAndFeed_IdIn(UUID userId, Collection<UUID> feedIds);
}

