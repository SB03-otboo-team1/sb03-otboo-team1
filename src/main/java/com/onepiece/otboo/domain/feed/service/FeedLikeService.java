package com.onepiece.otboo.domain.feed.service;

import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.entity.FeedLike;
import com.onepiece.otboo.domain.feed.repository.FeedLikeRepository;
import com.onepiece.otboo.domain.feed.repository.FeedRepository;
import com.onepiece.otboo.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedLikeService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final EntityManager em;

    public void like(UUID userId, UUID feedId) {
        if (!feedRepository.existsById(feedId)) {
            throw new EntityNotFoundException("Feed not found: " + feedId);
        }

        User userRef = em.getReference(User.class, userId);
        Feed feedRef = em.getReference(Feed.class, feedId);

        try {
            feedLikeRepository.save(FeedLike.of(userRef, feedRef));
        } catch (DataIntegrityViolationException ignore) {
        }

        syncLikeCount(feedId);
    }

    public void unlike(UUID userId, UUID feedId) {
        feedLikeRepository.deleteByUser_IdAndFeed_Id(userId, feedId);
        syncLikeCount(feedId);
    }

    public boolean toggle(UUID userId, UUID feedId) {
        var existing = feedLikeRepository.findByUser_IdAndFeed_Id(userId, feedId);
        boolean liked;
        if (existing.isPresent()) {
            feedLikeRepository.delete(existing.get());
            liked = false;
        } else {
            like(userId, feedId);
            return true;
        }
        syncLikeCount(feedId);
        return liked;
    }

    @Transactional(readOnly = true)
    public long countByFeed(UUID feedId) {
        return feedLikeRepository.countByFeed_Id(feedId);
    }

    private void syncLikeCount(UUID feedId) {
        long realCount = feedLikeRepository.countByFeed_Id(feedId);
        feedRepository.updateLikeCount(feedId, realCount);
    }
}
