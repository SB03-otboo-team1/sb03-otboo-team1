package com.onepiece.otboo.domain.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowDto;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryDto;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.follow.exception.DuplicateFollowException;
import com.onepiece.otboo.domain.follow.exception.FollowNotFoundException;
import com.onepiece.otboo.domain.follow.mapper.FollowMapper;
import com.onepiece.otboo.domain.follow.repository.FollowRepository;
import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FollowMapper followMapper;
    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private FollowServiceImpl followService;

    private User follower;   // me (viewer)로도 활용
    private User followee;   // target
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
        followee = User.builder()
            .socialAccount(SocialAccount.builder().build())
            .email("followee@test.com")
            .password("pwd123")
            .locked(false)
            .role(Role.USER)
            .build();

        ReflectionTestUtils.setField(follower, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(followee, "id", UUID.randomUUID());

        follow = Follow.builder()
            .follower(follower)
            .following(followee)
            .build();

        // id/createdAt 강제 세팅
        ReflectionTestUtils.setField(follow, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(follow, "createdAt", Instant.now());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -----------------------------
    // createFollow
    // -----------------------------
    @Test
    @DisplayName("팔로우 생성 성공")
    void createFollow_success() {
        FollowRequest request = new FollowRequest(follower.getId(), followee.getId());

        given(userRepository.findById(request.followerId())).willReturn(Optional.of(follower));
        given(userRepository.findById(request.followeeId())).willReturn(Optional.of(followee));
        given(followRepository.existsByFollowerAndFollowing(follower, followee)).willReturn(false);
        given(followRepository.save(any(Follow.class))).willReturn(follow);
        // 저장 직후 다시 로드
        given(followRepository.findById(any(UUID.class))).willReturn(Optional.of(follow));

        // mapper 결과 준비 (AuthorDto 2개를 포함하는 FollowDto)
        AuthorDto followerAuthor = AuthorDto.builder()
            .userId(follower.getId()).name("팔로워닉").profileImageUrl("follower.png").build();
        AuthorDto followeeAuthor = AuthorDto.builder()
            .userId(followee.getId()).name("팔로이닉").profileImageUrl("followee.png").build();
        FollowDto mapped = FollowDto.builder()
            .id((UUID) ReflectionTestUtils.getField(follow, "id"))
            .follower(followerAuthor)
            .followee(followeeAuthor)
            .build();

        given(followMapper.toDto(any(Follow.class), eq(fileStorage))).willReturn(mapped);

        FollowDto response = followService.createFollow(request);

        assertThat(response).isNotNull();
        assertThat(response.follower().userId()).isEqualTo(follower.getId());
        assertThat(response.followee().userId()).isEqualTo(followee.getId());
        assertThat(response.follower().name()).isEqualTo("팔로워닉");
        assertThat(response.followee().name()).isEqualTo("팔로이닉");
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
    @DisplayName("팔로우 생성 실패 - 이미 존재")
    void createFollow_fail_duplicate() {
        FollowRequest request = new FollowRequest(follower.getId(), followee.getId());
        given(userRepository.findById(request.followerId())).willReturn(Optional.of(follower));
        given(userRepository.findById(request.followeeId())).willReturn(Optional.of(followee));
        given(followRepository.existsByFollowerAndFollowing(follower, followee)).willReturn(true);

        assertThatThrownBy(() -> followService.createFollow(request))
            .isInstanceOf(DuplicateFollowException.class)
            .hasMessageContaining(ErrorCode.DUPLICATE_FOLLOW.getMessage());
    }

    // -----------------------------
    // getFollowers (followee의 팔로워 목록)
    // -----------------------------
    @Test
    @DisplayName("팔로워 목록 조회 성공 (커서 기반)")
    void getFollowers_success() {
        UUID followeeId = followee.getId();
        given(userRepository.findById(followeeId)).willReturn(Optional.of(followee));

        // 페이지 사이즈 이하 반환 → hasNext=false
        given(followRepository.findFollowersWithProfileCursor(
            eq(followee), any(), any(), anyInt(), any()
        )).willReturn(List.of(follow));

        // Mapper
        AuthorDto followerAuthor = AuthorDto.builder()
            .userId(follower.getId()).name("팔로워닉").profileImageUrl("follower.png").build();
        AuthorDto followeeAuthor = AuthorDto.builder()
            .userId(followee.getId()).name("팔로이닉").profileImageUrl("followee.png").build();
        FollowDto mapped = FollowDto.builder()
            .id((UUID) ReflectionTestUtils.getField(follow, "id"))
            .follower(followerAuthor)
            .followee(followeeAuthor)
            .build();
        given(followMapper.toDto(any(Follow.class), eq(fileStorage))).willReturn(mapped);

        given(followRepository.countByFollowing(followee)).willReturn(1L);

        CursorPageResponseDto<FollowDto> page =
            followService.getFollowers(followeeId, null, null, 10, null);

        assertThat(page.data()).hasSize(1);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.totalCount()).isEqualTo(1L);
        assertThat(page.data().get(0).follower().name()).isEqualTo("팔로워닉");
        assertThat(page.data().get(0).followee().userId()).isEqualTo(followeeId);
    }

    // -----------------------------
    // getFollowings (follower의 팔로잉 목록)
    // -----------------------------
    @Test
    @DisplayName("팔로잉 목록 조회 성공 (커서 기반)")
    void getFollowings_success() {
        UUID followerId = follower.getId();
        given(userRepository.findById(followerId)).willReturn(Optional.of(follower));

        given(followRepository.findFollowingsWithProfileCursor(
            eq(follower), any(), any(), anyInt(), any()
        )).willReturn(List.of(follow));

        AuthorDto followerAuthor = AuthorDto.builder()
            .userId(follower.getId()).name("팔로워닉").profileImageUrl("follower.png").build();
        AuthorDto followeeAuthor = AuthorDto.builder()
            .userId(followee.getId()).name("팔로이닉").profileImageUrl("followee.png").build();
        FollowDto mapped = FollowDto.builder()
            .id((UUID) ReflectionTestUtils.getField(follow, "id"))
            .follower(followerAuthor)
            .followee(followeeAuthor)
            .build();
        given(followMapper.toDto(any(Follow.class), eq(fileStorage))).willReturn(mapped);

        given(followRepository.countByFollower(follower)).willReturn(1L);

        CursorPageResponseDto<FollowDto> page =
            followService.getFollowings(followerId, null, null, 10, null);

        assertThat(page.data()).hasSize(1);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.data().get(0).followee().name()).isEqualTo("팔로이닉");
        assertThat(page.totalCount()).isEqualTo(1L);
    }

    // -----------------------------
    // deleteFollow (by followId)
    // -----------------------------
    @Test
    @DisplayName("언팔로우 성공 - followId로 삭제")
    void deleteFollow_success() {
        UUID followId = (UUID) ReflectionTestUtils.getField(follow, "id");
        given(followRepository.existsById(followId)).willReturn(true);

        followService.deleteFollow(followId);

        verify(followRepository).deleteById(followId);
    }

    @Test
    @DisplayName("언팔로우 실패 - Follow 없음")
    void deleteFollow_fail_notFound() {
        UUID followId = UUID.randomUUID();
        given(followRepository.existsById(followId)).willReturn(false);

        assertThatThrownBy(() -> followService.deleteFollow(followId))
            .isInstanceOf(FollowNotFoundException.class)
            .hasMessageContaining(ErrorCode.FOLLOW_NOT_FOUND.getMessage());
    }

    // -----------------------------
    // getFollowSummary (SecurityContext의 me 사용)
    // -----------------------------
    @Test
    @DisplayName("팔로우 요약 조회 성공 - me가 target을 팔로우 중")
    void getFollowSummary_success_followingTrue() {
        // SecurityContext 설정: principal = CustomUserDetails(username=email)
        CustomUserDetails cud = org.mockito.Mockito.mock(CustomUserDetails.class);
        given(cud.getUsername()).willReturn(follower.getEmail());
        var auth = new UsernamePasswordAuthenticationToken(cud, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // targetUser
        given(userRepository.findById(followee.getId())).willReturn(Optional.of(followee));
        // me by email
        given(userRepository.findByEmail(follower.getEmail())).willReturn(Optional.of(follower));

        given(followRepository.countByFollowing(followee)).willReturn(3L);
        given(followRepository.countByFollower(followee)).willReturn(2L);
        // me -> target
        given(followRepository.findByFollowerAndFollowing(follower, followee))
            .willReturn(Optional.of(follow));
        // target -> me
        given(followRepository.findByFollowerAndFollowing(followee, follower))
            .willReturn(Optional.empty());

        FollowSummaryDto summary = followService.getFollowSummary(followee.getId());

        assertThat(summary.followeeId()).isEqualTo(followee.getId());
        assertThat(summary.followerCount()).isEqualTo(3L);
        assertThat(summary.followingCount()).isEqualTo(2L);
        assertThat(summary.followedByMe()).isTrue();
        assertThat(summary.followedByMeId()).isEqualTo(
            (UUID) ReflectionTestUtils.getField(follow, "id"));
        assertThat(summary.followingMe()).isFalse();
    }

    @Test
    @DisplayName("팔로우 요약 조회 성공 - me가 target을 팔로우 중이 아님")
    void getFollowSummary_success_followingFalse() {
        CustomUserDetails cud = org.mockito.Mockito.mock(CustomUserDetails.class);
        given(cud.getUsername()).willReturn(follower.getEmail());
        var auth = new UsernamePasswordAuthenticationToken(cud, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        given(userRepository.findById(followee.getId())).willReturn(Optional.of(followee));
        given(userRepository.findByEmail(follower.getEmail())).willReturn(Optional.of(follower));

        given(followRepository.countByFollowing(followee)).willReturn(1L);
        given(followRepository.countByFollower(followee)).willReturn(0L);
        given(followRepository.findByFollowerAndFollowing(follower, followee))
            .willReturn(Optional.empty());
        given(followRepository.findByFollowerAndFollowing(followee, follower))
            .willReturn(Optional.of(follow));

        FollowSummaryDto summary = followService.getFollowSummary(followee.getId());

        assertThat(summary.followerCount()).isEqualTo(1L);
        assertThat(summary.followingCount()).isEqualTo(0L);
        assertThat(summary.followedByMe()).isFalse();
        assertThat(summary.followingMe()).isTrue();
    }

    @Test
    @DisplayName("팔로우 요약 조회 실패 - 대상 유저 없음")
    void getFollowSummary_fail_userNotFound() {
        CustomUserDetails cud = org.mockito.Mockito.mock(CustomUserDetails.class);
        given(cud.getUsername()).willReturn(follower.getEmail());
        var auth = new UsernamePasswordAuthenticationToken(cud, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        UUID targetId = UUID.randomUUID();
        given(userRepository.findById(targetId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> followService.getFollowSummary(targetId))
            .isInstanceOf(UserNotFoundException.class);
    }
}
