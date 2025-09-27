package com.onepiece.otboo.domain.follow.service;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowerResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.follow.exception.DuplicateFollowException;
import com.onepiece.otboo.domain.follow.mapper.FollowMapper;
import com.onepiece.otboo.domain.follow.repository.FollowRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;

    /**
     * 팔로우 생성
     */
    @Override
    public FollowResponse createFollow(FollowRequest request) {
        User follower = userRepository.findById(request.followerId())
            .orElseThrow(() -> UserNotFoundException.byId(request.followerId()));

        User followee = userRepository.findById(request.followeeId())
            .orElseThrow(() -> UserNotFoundException.byId(request.followeeId()));

        if (followRepository.existsByFollowerAndFollowing(follower, followee)) {
            throw new DuplicateFollowException(ErrorCode.DUPLICATE_FOLLOW);
        }

        Follow saved = followRepository.save(
            Follow.builder()
                .follower(follower)
                .following(followee)
                .build()
        );

        return followMapper.toResponse(saved);
    }

    /**
     * 팔로워 목록 조회 (커서 기반 페이지네이션, QueryDSL Projection)
     */
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseDto<FollowerResponse> getFollowers(
        UUID followeeId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        String sortBy,
        String sortDirection
    ) {
        User followee = userRepository.findById(followeeId)
            .orElseThrow(() -> UserNotFoundException.byId(followeeId));

        List<FollowerResponse> results = followRepository.findFollowersWithProfileCursor(
            followee, cursor, idAfter, limit, nameLike, sortBy, sortDirection
        );

        boolean hasNext = results.size() > limit;
        if (hasNext) {
            results = results.subList(0, limit);
        }

        String nextCursor = null;
        String nextIdAfter = null;
        if (!results.isEmpty()) {
            FollowerResponse last = results.get(results.size() - 1);
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId().toString();
        }

        long totalCount = followRepository.countByFollowing(followee);

        return new CursorPageResponseDto<>(
            results,
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            sortBy,
            sortDirection
        );
    }

    /**
     * 팔로잉 목록 조회 (커서 기반 페이지네이션, QueryDSL Projection)
     */
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseDto<FollowingResponse> getFollowings(
        UUID followerId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        String sortBy,
        String sortDirection
    ) {
        User follower = userRepository.findById(followerId)
            .orElseThrow(() -> UserNotFoundException.byId(followerId));

        // ✅ Repository에서 바로 DTO Projection 반환
        List<FollowingResponse> results = followRepository.findFollowingsWithProfileCursor(
            follower, cursor, idAfter, limit, nameLike, sortBy, sortDirection
        );

        boolean hasNext = results.size() > limit;
        if (hasNext) {
            results = results.subList(0, limit);
        }

        String nextCursor = null;
        String nextIdAfter = null;
        if (!results.isEmpty()) {
            FollowingResponse last = results.get(results.size() - 1);
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId().toString();
        }

        long totalCount = followRepository.countByFollower(follower);

        return new CursorPageResponseDto<>(
            results,
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            sortBy,
            sortDirection
        );
    }

    /**
     * 언팔로우 (팔로우 취소)
     */
    @Override
    public void deleteFollow(FollowRequest request) {
        User follower = userRepository.findById(request.followerId())
            .orElseThrow(() -> UserNotFoundException.byId(request.followerId()));

        User followee = userRepository.findById(request.followeeId())
            .orElseThrow(() -> UserNotFoundException.byId(request.followeeId()));

        followRepository.deleteByFollowerAndFollowing(follower, followee);
    }

    /**
     * 팔로우 요약 정보 조회
     */
    @Override
    @Transactional(readOnly = true)
    public FollowSummaryResponse getFollowSummary(UUID userId, UUID viewerId) {
        User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));

        long followerCount = followRepository.countByFollowing(targetUser);
        long followingCount = followRepository.countByFollower(targetUser);

        boolean isFollowing = false;
        if (viewerId != null) {
            User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> UserNotFoundException.byId(viewerId));
            isFollowing = followRepository.existsByFollowerAndFollowing(viewer, targetUser);
        }

        return new FollowSummaryResponse(
            userId,
            followerCount,
            followingCount,
            isFollowing
        );
    }
}