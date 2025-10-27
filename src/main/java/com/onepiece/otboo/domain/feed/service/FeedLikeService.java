package com.onepiece.otboo.domain.feed.service;

import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.entity.FeedLike;
import com.onepiece.otboo.domain.feed.repository.FeedLikeRepository;
import com.onepiece.otboo.domain.feed.repository.FeedRepository;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.event.event.FeedLikedEvent;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedLikeService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final EntityManager em;
    private final ApplicationEventPublisher eventPublisher;


    public void like(UUID userId, UUID feedId) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new GlobalException(ErrorCode.FEED_NOT_FOUND));
        User liker = userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        try {
            feedLikeRepository.save(FeedLike.of(liker, feed));
        } catch (DataIntegrityViolationException ignore) {
        }

        syncLikeCount(feedId);

        Profile profile = profileRepository.findByUserId(feed.getAuthorId())
            .orElseThrow(() -> new GlobalException(ErrorCode.PROFILE_NOT_FOUND));

        AuthorDto authorDto = AuthorDto.builder()
            .userId(feed.getAuthorId())
            .name(profile.getNickname())
            .profileImageUrl(profile.getProfileImageUrl())
            .build();

        FeedResponse feedResponse = new FeedResponse(
            feed.getId(),
            feed.getCreatedAt(),
            feed.getUpdatedAt(),
            authorDto,
            null,
            List.of(),
            feed.getContent(),
            feed.getLikeCount(),
            feed.getCommentCount(),
            false
        );

        eventPublisher.publishEvent(
            new FeedLikedEvent(feedResponse, userId, Instant.now())
        );
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