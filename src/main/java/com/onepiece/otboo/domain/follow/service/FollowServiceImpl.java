package com.onepiece.otboo.domain.follow.service;

import com.onepiece.otboo.domain.auth.exception.UnAuthorizedException;
import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowDto;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryDto;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.follow.exception.DuplicateFollowException;
import com.onepiece.otboo.domain.follow.exception.FollowNotAllowedException;
import com.onepiece.otboo.domain.follow.exception.FollowNotFoundException;
import com.onepiece.otboo.domain.follow.mapper.FollowMapper;
import com.onepiece.otboo.domain.follow.repository.FollowRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.event.event.FollowCreatedEvent;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;
    private final FileStorage fileStorage;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 팔로우 생성
     */
    @Override
    public FollowDto createFollow(FollowRequest request) {
        UUID followerId = request.followerId();
        UUID followeeId = request.followeeId();

        if (followerId.equals(followeeId)) {
            throw new FollowNotAllowedException(ErrorCode.FOLLOW_NOT_ALLOWED);
        }

        User follower = findUser(followerId);
        User followee = findUser(followeeId);

        if (followRepository.existsByFollowerAndFollowing(follower, followee)) {
            throw new DuplicateFollowException(ErrorCode.DUPLICATE_FOLLOW);
        }

        Follow saved = followRepository.save(
            Follow.builder()
                .follower(follower)
                .following(followee)
                .build()
        );

        eventPublisher.publishEvent(
            new FollowCreatedEvent(
                followMapper.toDto(saved, fileStorage),
                Instant.now()
            )
        );

        Follow loaded = followRepository.findById(saved.getId())
            .orElse(saved);

        return followMapper.toDto(loaded, fileStorage);
    }

    /**
     * 팔로워 목록 조회 (커서 기반 페이지네이션, QueryDSL Projection)
     */
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseDto<FollowDto> getFollowers(
        UUID followeeId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike
    ) {
        User followee = userRepository.findById(followeeId)
            .orElseThrow(() -> UserNotFoundException.byId(followeeId));

        List<Follow> fetched = followRepository.findFollowersWithProfileCursor(
            followee, cursor, idAfter, limit, nameLike
        );

        boolean hasNext = fetched.size() > limit;
        List<Follow> page = hasNext ? fetched.subList(0, limit) : fetched;

        List<FollowDto> results = page.stream()
            .map(f -> followMapper.toDto(f, fileStorage))
            .toList();

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (!page.isEmpty()) {
            Follow last = page.get(page.size() - 1);
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId();
        }

        long totalCount = followRepository.countByFollowing(followee);

        return new CursorPageResponseDto<>(
            results,
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            SortBy.CREATED_AT,
            SortDirection.DESCENDING
        );
    }

    /**
     * 팔로잉 목록 조회 (커서 기반 페이지네이션, QueryDSL Projection)
     */
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseDto<FollowDto> getFollowings(
        UUID followerId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike
    ) {
        User follower = userRepository.findById(followerId)
            .orElseThrow(() -> UserNotFoundException.byId(followerId));

        List<Follow> fetched = followRepository.findFollowingsWithProfileCursor(
            follower, cursor, idAfter, limit, nameLike
        );

        boolean hasNext = fetched.size() > limit;
        List<Follow> page = hasNext ? fetched.subList(0, limit) : fetched;

        List<FollowDto> results = page.stream()
            .map(f -> followMapper.toDto(f, fileStorage))
            .toList();

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (!page.isEmpty()) {
            Follow last = page.get(page.size() - 1);
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId();
        }

        long totalCount = followRepository.countByFollower(follower);

        return new CursorPageResponseDto<>(
            results,
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            SortBy.CREATED_AT,
            SortDirection.DESCENDING
        );
    }

    /**
     * 언팔로우 (팔로우 취소)
     */
    @Override
    public void deleteFollow(UUID followId) {
        if (!followRepository.existsById(followId)) {
            throw new FollowNotFoundException(ErrorCode.FOLLOW_NOT_FOUND);
        }
        followRepository.deleteById(followId);
    }

    /**
     * 팔로우 요약 정보 조회
     */
    @Override
    @Transactional(readOnly = true)
    public FollowSummaryDto getFollowSummary(UUID userId) {
        User targetUser = findUser(userId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnAuthorizedException();
        }

        String email = extractEmail(authentication);
        User me = userRepository.findByEmail(email)
            .orElseThrow(() -> UserNotFoundException.byEmail(email));

        // 카운트
        long followerCount = followRepository.countByFollowing(targetUser);
        long followingCount = followRepository.countByFollower(targetUser);

        Optional<Follow> meFollowsTarget = followRepository.findByFollowerAndFollowing(me,
            targetUser);
        Optional<Follow> targetFollowsMe = followRepository.findByFollowerAndFollowing(targetUser,
            me);

        return new FollowSummaryDto(
            targetUser.getId(),
            followerCount,
            followingCount,
            meFollowsTarget.isPresent(),
            meFollowsTarget.map(Follow::getId).orElse(null),
            targetFollowsMe.isPresent()
        );
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));
    }

    private String extractEmail(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUsername();
        }
        return Objects.toString(auth.getName(), null);
    }
}