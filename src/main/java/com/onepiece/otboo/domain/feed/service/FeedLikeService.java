package com.onepiece.otboo.domain.feed.service;

import com.onepiece.otboo.domain.feed.repository.FeedRepository;
import com.onepiece.otboo.domain.feed.entity.FeedLike;
import com.onepiece.otboo.domain.feed.repository.FeedLikeRepository;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedLikeService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final EntityManager em;

    public void like(UUID feedId, UUID currentUserId) {
        if (!feedRepository.existsById(feedId)) {
            throw new EntityNotFoundException("Feed not found: " + feedId);
        }
        if (feedLikeRepository.existsByUserIdAndFeedId(currentUserId, feedId)) {
            return;
        }

        User userRef = em.getReference(User.class, currentUserId);
        Feed feedRef = em.getReference(Feed.class, feedId);

        FeedLike like = FeedLike.builder()
            .id(UUID.randomUUID())
            .user(userRef)
            .feed(feedRef)
            .createdAt(Instant.now())
            .build();
        feedLikeRepository.save(like);

        long count = feedLikeRepository.countByFeedId(feedId);
        feedRepository.updateLikeCount(feedId, count);
    }

    public void unlike(UUID feedId, UUID currentUserId) {
        if (!feedRepository.existsById(feedId)) {
            throw new EntityNotFoundException("Feed not found: " + feedId);
        }
        Optional<FeedLike> likeOpt = feedLikeRepository.findByUserIdAndFeedId(currentUserId, feedId);
        likeOpt.ifPresent(feedLikeRepository::delete);

        long count = feedLikeRepository.countByFeedId(feedId);
        feedRepository.updateLikeCount(feedId, count);
    }
}
