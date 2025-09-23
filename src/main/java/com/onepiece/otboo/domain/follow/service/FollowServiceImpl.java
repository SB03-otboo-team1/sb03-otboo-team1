package com.onepiece.otboo.domain.follow.service;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.follow.mapper.FollowMapper;
import com.onepiece.otboo.domain.follow.repository.FollowRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import java.util.stream.Collectors;
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

    @Override
    public FollowResponse createFollow(FollowRequest request) {
        User follower = userRepository.findById(request.getFollowerId())
            .orElseThrow(() -> UserNotFoundException.byId(request.getFollowerId()));
        User following = userRepository.findById(request.getFollowingId())
            .orElseThrow(() -> UserNotFoundException.byId(request.getFollowingId()));

        Follow follow = Follow.builder()
            .follower(follower)
            .following(following)
            .build();

        boolean alreadyExists = followRepository.existsByFollowerAndFollowing(follower, following);
        follow.validateDuplicate(alreadyExists);

        Follow saved = followRepository.save(follow);
        return followMapper.toResponse(saved);
    }

    @Override
    public List<FollowResponse> getFollowers(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));

        return followRepository.findByFollowing(user).stream()
            .map(followMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<FollowingResponse> getFollowings(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));

        return followRepository.findFollowingsWithProfile(user);
    }

    @Override
    public void deleteFollow(FollowRequest request) {
        User follower = userRepository.findById(request.getFollowerId())
            .orElseThrow(() -> UserNotFoundException.byId(request.getFollowerId()));
        User following = userRepository.findById(request.getFollowingId())
            .orElseThrow(() -> UserNotFoundException.byId(request.getFollowingId()));

        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    @Override
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

        return FollowSummaryResponse.builder()
            .userId(userId)
            .followerCount(followerCount)
            .followingCount(followingCount)
            .isFollowing(isFollowing)
            .build();
    }
}