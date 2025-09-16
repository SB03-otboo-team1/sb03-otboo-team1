package com.onepiece.otboo.domain.follow.entity;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FollowTest {

    @Test
    @DisplayName("Follow 엔티티를 빌더로 생성할 수 있다")
    void createFollowEntity_success() {
        User follower = User.builder()
            .provider(Provider.LOCAL)
            .providerUserId("follower-" + UUID.randomUUID())
            .email("follower@test.com")
            .password("password123")
            .locked(false)
            .role(Role.USER)
            .build();

        User following = User.builder()
            .provider(Provider.LOCAL)
            .providerUserId("following-" + UUID.randomUUID())
            .email("following@test.com")
            .password("password123")
            .locked(false)
            .role(Role.USER)
            .build();

        Follow follow = Follow.builder()
            .follower(follower)
            .following(following)
            .build();

        assertThat(follow).isNotNull();
        assertThat(follow.getFollower().getEmail()).isEqualTo("follower@test.com");
        assertThat(follow.getFollowing().getEmail()).isEqualTo("following@test.com");
    }
}