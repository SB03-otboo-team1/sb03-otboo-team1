package com.onepiece.otboo.domain.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.follow.exception.DuplicateFollowException;
import com.onepiece.otboo.domain.follow.mapper.FollowMapper;
import com.onepiece.otboo.domain.follow.repository.FollowRepository;
import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.exception.ErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

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
            .socialAccount(SocialAccount.builder().build())
            .email("follower@test.com")
            .password("pwd123")
            .locked(false)
            .role(Role.USER)
            .build();

        following = User.builder()
            .socialAccount(SocialAccount.builder().build())
            .email("following@test.com")
            .password("pwd123")
            .locked(false)
            .role(Role.USER)
            .build();

        // 테스트용 id 주입
        ReflectionTestUtils.setField(follower, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(following, "id", UUID.randomUUID());

        follow = Follow.builder()
            .follower(follower)
            .following(following)
            .build();
    }

    @Test
    @DisplayName("팔로우 생성 성공")
    void createFollow_success() {
        FollowRequest request = new FollowRequest(follower.getId(), following.getId());
        given(userRepository.findById(request.followerId())).willReturn(Optional.of(follower));
        given(userRepository.findById(request.followeeId())).willReturn(Optional.of(following));
        given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(false);
        given(followRepository.save(any(Follow.class))).willReturn(follow);

        FollowResponse mockResponse = FollowResponse.builder()
            .id(UUID.randomUUID())
            .followerId(follower.getId())
            .nickname("팔로워닉네임")
            .profileImageUrl("follower.png")
            .createdAt(Instant.now())
            .build();

        given(followMapper.toResponse(follow)).willReturn(mockResponse);

        FollowResponse response = followService.createFollow(request);

        assertThat(response).isNotNull();
        assertThat(response.getFollowerId()).isEqualTo(follower.getId());
        assertThat(response.getNickname()).isEqualTo("팔로워닉네임");
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    @DisplayName("팔로우 생성 실패 - 존재하지 않는 유저")
    void createFollow_fail_userNotFound() {
        FollowRequest request = new FollowRequest(UUID.randomUUID(), UUID.randomUUID());
        given(userRepository.findById(request.followerId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> followService.createFollow(request))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("이미 팔로우 했을 경우 예외 발생")
    void createFollow_alreadyExists_throwsException() {
        FollowRequest request = new FollowRequest(follower.getId(), following.getId());
        given(userRepository.findById(request.followerId())).willReturn(Optional.of(follower));
        given(userRepository.findById(request.followeeId())).willReturn(Optional.of(following));
        given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(true);

        assertThatThrownBy(() -> followService.createFollow(request))
            .isInstanceOf(DuplicateFollowException.class)
            .hasMessageContaining(ErrorCode.DUPLICATE_FOLLOW.getMessage());
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공 (커서 기반)")
    void getFollowers_success() {
        UUID userId = following.getId();
        given(userRepository.findById(userId)).willReturn(Optional.of(following));

        FollowResponse followResponse = FollowResponse.builder()
            .id(UUID.randomUUID())
            .followerId(follower.getId())
            .nickname("팔로워닉네임")
            .profileImageUrl("follower.png")
            .createdAt(Instant.now())
            .build();

        given(followRepository.findFollowersWithProfileCursor(
            any(User.class), any(), any(), anyInt(), any(), any(), any()
        )).willReturn(List.of(followResponse));

        var response = followService.getFollowers(userId, null, null, 10, null, "createdAt", "ASC");

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).getNickname()).isEqualTo("팔로워닉네임");
        assertThat(response.data().get(0).getFollowerId()).isEqualTo(follower.getId());
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 (커서 기반)")
    void getFollowings_success() {
        UUID userId = follower.getId();
        given(userRepository.findById(userId)).willReturn(Optional.of(follower));

        FollowingResponse followingResponse = FollowingResponse.builder()
            .id(UUID.randomUUID())
            .followingId(following.getId())
            .nickname("팔로잉닉네임")
            .profileImage("profile.png")
            .createdAt(Instant.now())
            .build();

        given(followRepository.findFollowingsWithProfileCursor(
            any(User.class), any(), any(), anyInt(), any(), any(), any()
        )).willReturn(List.of(followingResponse));

        var response = followService.getFollowings(userId, null, null, 10, null, "createdAt", "ASC");

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).getNickname()).isEqualTo("팔로잉닉네임");
    }

    @Test
    @DisplayName("언팔로우 성공")
    void deleteFollow_success() {
        FollowRequest request = new FollowRequest(follower.getId(), following.getId());
        given(userRepository.findById(request.followerId())).willReturn(Optional.of(follower));
        given(userRepository.findById(request.followeeId())).willReturn(Optional.of(following));

        followService.deleteFollow(request);

        verify(followRepository).deleteByFollowerAndFollowing(follower, following);
    }

    @Test
    @DisplayName("언팔로우 실패 - 대상 유저 없음")
    void deleteFollow_fail_userNotFound() {
        FollowRequest request = new FollowRequest(follower.getId(), UUID.randomUUID());
        given(userRepository.findById(request.followeeId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> followService.deleteFollow(request))
            .isInstanceOf(UserNotFoundException.class);
    }


    @Test
    @DisplayName("팔로우 요약 조회 성공 - viewer가 팔로우 중일 때")
    void getFollowSummary_success_isFollowingTrue() {
        UUID targetUserId = following.getId();
        UUID viewerId = follower.getId();

        given(userRepository.findById(targetUserId)).willReturn(Optional.of(following));
        given(userRepository.findById(viewerId)).willReturn(Optional.of(follower));

        given(followRepository.countByFollowing(following)).willReturn(3L);
        given(followRepository.countByFollower(following)).willReturn(2L);
        given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(true);

        var response = followService.getFollowSummary(targetUserId, viewerId);

        assertThat(response.getUserId()).isEqualTo(targetUserId);
        assertThat(response.getFollowerCount()).isEqualTo(3L);
        assertThat(response.getFollowingCount()).isEqualTo(2L);
        assertThat(response.isFollowing()).isTrue();
    }

    @Test
    @DisplayName("팔로우 요약 조회 성공 - viewer가 팔로우 중이 아닐 때")
    void getFollowSummary_success_isFollowingFalse() {
        UUID targetUserId = following.getId();
        UUID viewerId = follower.getId();

        given(userRepository.findById(targetUserId)).willReturn(Optional.of(following));
        given(userRepository.findById(viewerId)).willReturn(Optional.of(follower));

        given(followRepository.countByFollowing(following)).willReturn(1L);
        given(followRepository.countByFollower(following)).willReturn(0L);
        given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(false);

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
            .isInstanceOf(UserNotFoundException.class);
    }
}