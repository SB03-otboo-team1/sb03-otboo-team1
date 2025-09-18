package com.onepiece.otboo.domain.follow.service;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.follow.mapper.FollowMapper;
import com.onepiece.otboo.domain.follow.repository.FollowRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowMapper followMapper;

    @InjectMocks
    private FollowServiceImpl followService;

    private User follower;
    private User following;
    private Follow follow;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        follower = User.builder()
            .provider(Provider.LOCAL)
            .providerUserId(UUID.randomUUID().toString())
            .email("follower@test.com")
            .password("pwd123")
            .locked(false)
            .role(Role.USER)
            .build();

        following = User.builder()
            .provider(Provider.LOCAL)
            .providerUserId(UUID.randomUUID().toString())
            .email("following@test.com")
            .password("pwd123")
            .locked(false)
            .role(Role.USER)
            .build();

        follow = Follow.builder()
            .follower(follower)
            .following(following)
            .build();
    }

    @Test
    @DisplayName("팔로우 생성 성공")
    void createFollow_success() {

        FollowRequest request = new FollowRequest(UUID.randomUUID(), UUID.randomUUID());
        given(userRepository.findById(request.getFollowerId())).willReturn(Optional.of(follower));
        given(userRepository.findById(request.getFollowingId())).willReturn(Optional.of(following));
        given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(false);
        given(followRepository.save(any(Follow.class))).willReturn(follow);
        given(followMapper.toResponse(follow)).willReturn(
            FollowResponse.builder()
                .id(UUID.randomUUID())
                .followerId(follower.getId())
                .followingId(following.getId())
                .createdAt(Instant.now())
                .build()
        );

        FollowResponse response = followService.createFollow(request);

        assertThat(response).isNotNull();
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    @DisplayName("이미 팔로우 했을 경우 예외 발생")
    void createFollow_alreadyExists_throwsException() {

        FollowRequest request = new FollowRequest(UUID.randomUUID(), UUID.randomUUID());
        given(userRepository.findById(request.getFollowerId())).willReturn(Optional.of(follower));
        given(userRepository.findById(request.getFollowingId())).willReturn(Optional.of(following));
        given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(true);

        assertThatThrownBy(() -> followService.createFollow(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Already following");
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공")
    void getFollowers_success() {

        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.of(following));
        given(followRepository.findByFollowing(following)).willReturn(List.of(follow));
        given(followMapper.toResponse(follow)).willReturn(
            FollowResponse.builder()
                .id(UUID.randomUUID())
                .followerId(follower.getId())
                .followingId(following.getId())
                .createdAt(Instant.now())
                .build()
        );

        List<FollowResponse> responses = followService.getFollowers(userId);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공")
    void getFollowings_success() {

        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.of(follower));
        given(followRepository.findByFollower(follower)).willReturn(List.of(follow));
        given(followMapper.toResponse(follow)).willReturn(
            FollowResponse.builder()
                .id(UUID.randomUUID())
                .followerId(follower.getId())
                .followingId(following.getId())
                .createdAt(Instant.now())
                .build()
        );

        List<FollowResponse> responses = followService.getFollowings(userId);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("언팔로우 성공")
    void deleteFollow_success() {

        FollowRequest request = new FollowRequest(UUID.randomUUID(), UUID.randomUUID());
        given(userRepository.findById(request.getFollowerId())).willReturn(Optional.of(follower));
        given(userRepository.findById(request.getFollowingId())).willReturn(Optional.of(following));

        followService.deleteFollow(request);

        verify(followRepository).deleteByFollowerAndFollowing(follower, following);
    }

    @Test
    @DisplayName("팔로우 요약 조회 성공 - viewer가 팔로우 중일 때")
    void getFollowSummary_success_isFollowingTrue() {
        UUID targetUserId = UUID.randomUUID();
        UUID viewerId = UUID.randomUUID();

        User targetUser = following; // 대상 유저
        User viewer = follower;      // 조회자

        given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));
        given(userRepository.findById(viewerId)).willReturn(Optional.of(viewer));

        // 팔로워 수: 3명
        given(followRepository.countByFollowing(targetUser)).willReturn(3L);
        // 팔로잉 수: 2명
        given(followRepository.countByFollower(targetUser)).willReturn(2L);
        // viewer가 targetUser를 팔로우 중
        given(followRepository.existsByFollowerAndFollowing(viewer, targetUser)).willReturn(true);

        var response = followService.getFollowSummary(targetUserId, viewerId);

        assertThat(response.getUserId()).isEqualTo(targetUserId);
        assertThat(response.getFollowerCount()).isEqualTo(3L);
        assertThat(response.getFollowingCount()).isEqualTo(2L);
        assertThat(response.isFollowing()).isTrue();
    }

    @Test
    @DisplayName("팔로우 요약 조회 성공 - viewer가 팔로우 중이 아닐 때")
    void getFollowSummary_success_isFollowingFalse() {
        UUID targetUserId = UUID.randomUUID();
        UUID viewerId = UUID.randomUUID();

        User targetUser = following;
        User viewer = follower;

        given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));
        given(userRepository.findById(viewerId)).willReturn(Optional.of(viewer));

        given(followRepository.countByFollowing(targetUser)).willReturn(1L);
        given(followRepository.countByFollower(targetUser)).willReturn(0L);
        given(followRepository.existsByFollowerAndFollowing(viewer, targetUser)).willReturn(false);

        var response = followService.getFollowSummary(targetUserId, viewerId);

        assertThat(response.getFollowerCount()).isEqualTo(1L);
        assertThat(response.getFollowingCount()).isEqualTo(0L);
        assertThat(response.isFollowing()).isFalse();
    }

    @Test
    @DisplayName("팔로우 요약 조회 실패 - 대상 유저 없음")
    void getFollowSummary_fail_userNotFound() {
        UUID targetUserId = UUID.randomUUID();
        UUID viewerId = UUID.randomUUID();

        given(userRepository.findById(targetUserId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> followService.getFollowSummary(targetUserId, viewerId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");
    }

}