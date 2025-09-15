package com.onepiece.otboo.domain.follow.service;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.follow.mapper.FollowMapper;
import com.onepiece.otboo.domain.follow.repository.FollowRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;

    @Override
    public FollowResponse createFollow(FollowRequest request) {
        User follower = userRepository.findById(request.followerId())
            .orElseThrow(() -> new IllegalArgumentException("Follower not found"));
        User following = userRepository.findById(request.followingId())
            .orElseThrow(() -> new IllegalArgumentException("Following not found"));

        // 이미 팔로우 했는지 검증
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalArgumentException("Already following this user");
        }

        // 빌더 활용
        Follow follow = Follow.builder()
            .follower(follower)
            .following(following)
            .build();

        Follow saved = followRepository.save(follow);
        return followMapper.toResponse(saved);
    }

    @Override
    public List<FollowResponse> getFollowers(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return followRepository.findByFollowing(user).stream()
            .map(followMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<FollowResponse> getFollowings(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return followRepository.findByFollower(user).stream()
            .map(followMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteFollow(FollowRequest request) {
        User follower = userRepository.findById(request.followerId())
            .orElseThrow(() -> new IllegalArgumentException("Follower not found"));
        User following = userRepository.findById(request.followingId())
            .orElseThrow(() -> new IllegalArgumentException("Following not found"));

        followRepository.deleteByFollowerAndFollowing(follower, following);
    }
}