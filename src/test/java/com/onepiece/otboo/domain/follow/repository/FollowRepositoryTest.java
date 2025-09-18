package com.onepiece.otboo.domain.follow.repository;

import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.global.config.TestJpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class FollowRepositoryTest {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private com.onepiece.otboo.domain.user.repository.UserRepository userRepository;

    private User createUser(String email) {
        return User.builder()
            .provider(Provider.LOCAL)
            .providerUserId(UUID.randomUUID().toString())
            .email(email)
            .password("password123")
            .locked(false)
            .role(Role.USER)
            .build();
    }

    @Test
    @DisplayName("팔로우 저장 및 조회 성공")
    void saveAndFindFollow_success() {

        User follower = userRepository.save(createUser("follower@test.com"));
        User following = userRepository.save(createUser("following@test.com"));

        Follow follow = Follow.builder()
            .follower(follower)
            .following(following)
            .build();

        Follow saved = followRepository.save(follow);

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
    @DisplayName("특정 사용자의 팔로워 수를 조회할 수 있다")
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
    @DisplayName("특정 사용자의 팔로잉 수를 조회할 수 있다")
    void countByFollower_success() {

        User follower = userRepository.save(createUser("f1@test.com"));
        User following1 = userRepository.save(createUser("f2@test.com"));
        User following2 = userRepository.save(createUser("f3@test.com"));
        followRepository.save(Follow.builder().follower(follower).following(following1).build());
        followRepository.save(Follow.builder().follower(follower).following(following2).build());

        long count = followRepository.countByFollower(follower);

        assertThat(count).isEqualTo(2);
    }

}