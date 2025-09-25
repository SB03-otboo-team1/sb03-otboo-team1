package com.onepiece.otboo.domain.follow.repository;

import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.global.config.TestJpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class FollowRepositoryTest {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private com.onepiece.otboo.domain.user.repository.UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private User createUser(String email) {
        return User.builder()
            .socialAccount(SocialAccount.builder().build())
            .email(email)
            .password("password123")
            .locked(false)
            .role(Role.USER)
            .build();
    }

    private Profile createProfile(User user, String nickname) {
        return Profile.builder()
            .user(user)
            .nickname(nickname)
            .profileImageUrl("default.png")
            .build();
    }

    @Test
    @DisplayName("팔로우 저장 및 조회 성공")
    void saveAndFindFollow_success() {
        User follower = userRepository.save(createUser("follower@test.com"));
        User following = userRepository.save(createUser("following@test.com"));

        Follow saved = followRepository.save(Follow.builder()
            .follower(follower)
            .following(following)
            .build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFollower().getEmail()).isEqualTo("follower@test.com");
        assertThat(saved.getFollowing().getEmail()).isEqualTo("following@test.com");
    }

    @Test
    @DisplayName("follower 기준으로 Follow 조회")
    void findByFollower_success() {
        User follower = userRepository.save(createUser("f1@test.com"));
        User following1 = userRepository.save(createUser("f2@test.com"));
        User following2 = userRepository.save(createUser("f3@test.com"));

        followRepository.save(Follow.builder().follower(follower).following(following1).build());
        followRepository.save(Follow.builder().follower(follower).following(following2).build());

        List<Follow> follows = followRepository.findByFollower(follower);

        assertThat(follows).hasSize(2);
    }

    @Test
    @DisplayName("following 기준으로 Follow 조회")
    void findByFollowing_success() {
        User follower1 = userRepository.save(createUser("f1@test.com"));
        User follower2 = userRepository.save(createUser("f2@test.com"));
        User following = userRepository.save(createUser("target@test.com"));

        followRepository.save(Follow.builder().follower(follower1).following(following).build());
        followRepository.save(Follow.builder().follower(follower2).following(following).build());

        List<Follow> followers = followRepository.findByFollowing(following);

        assertThat(followers).hasSize(2);
    }

    @Test
    @DisplayName("이미 팔로우 관계가 존재하는지 확인")
    void existsByFollowerAndFollowing_success() {
        User follower = userRepository.save(createUser("f1@test.com"));
        User following = userRepository.save(createUser("f2@test.com"));
        followRepository.save(Follow.builder().follower(follower).following(following).build());

        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("팔로우 삭제 성공")
    void deleteByFollowerAndFollowing_success() {
        User follower = userRepository.save(createUser("f1@test.com"));
        User following = userRepository.save(createUser("f2@test.com"));
        followRepository.save(Follow.builder().follower(follower).following(following).build());

        followRepository.deleteByFollowerAndFollowing(follower, following);

        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 사용자의 팔로워 수 조회")
    void countByFollowing_success() {
        User following = userRepository.save(createUser("target@test.com"));
        User follower1 = userRepository.save(createUser("f1@test.com"));
        User follower2 = userRepository.save(createUser("f2@test.com"));
        followRepository.save(Follow.builder().follower(follower1).following(following).build());
        followRepository.save(Follow.builder().follower(follower2).following(following).build());

        long count = followRepository.countByFollowing(following);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("특정 사용자의 팔로잉 수 조회")
    void countByFollower_success() {
        User follower = userRepository.save(createUser("f1@test.com"));
        User following1 = userRepository.save(createUser("f2@test.com"));
        User following2 = userRepository.save(createUser("f3@test.com"));
        followRepository.save(Follow.builder().follower(follower).following(following1).build());
        followRepository.save(Follow.builder().follower(follower).following(following2).build());

        long count = followRepository.countByFollower(follower);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - 커서 기반")
    void findFollowingsWithProfileCursor_success() {
        User follower = userRepository.save(createUser("f1@test.com"));
        User following = userRepository.save(createUser("f2@test.com"));
        profileRepository.save(createProfile(following, "팔로잉닉네임"));

        followRepository.save(Follow.builder()
            .follower(follower)
            .following(following)
            .build());

        List<FollowingResponse> responses = followRepository
            .findFollowingsWithProfileCursor(follower, null, null, 10, null, "createdAt", "ASC");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getNickname()).isEqualTo("팔로잉닉네임");
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 커서 기반")
    void findFollowersWithProfileCursor_success() {
        User follower = userRepository.save(createUser("f1@test.com"));
        User following = userRepository.save(createUser("f2@test.com"));
        profileRepository.save(createProfile(follower, "팔로워닉네임"));

        followRepository.save(Follow.builder()
            .follower(follower)
            .following(following)
            .build());

        List<FollowResponse> responses = followRepository
            .findFollowersWithProfileCursor(following, null, null, 10, null, "createdAt", "ASC");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getNickname()).isEqualTo("팔로워닉네임");
    }
}