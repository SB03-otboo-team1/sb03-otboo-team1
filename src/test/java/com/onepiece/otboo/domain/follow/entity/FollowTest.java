package com.onepiece.otboo.domain.follow.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FollowTest {

    private User createUser(String email) {
        return User.builder()
            .socialAccount(SocialAccount.builder().build())
            .email(email)
            .password("password")
            .locked(false)
            .role(Role.USER)
            .build();
    }

    @Test
    @DisplayName("Follow 엔티티를 빌더로 생성할 수 있다")
    void createFollowEntity_success() {
        User follower = createUser("follower@test.com");
        User following = createUser("following@test.com");

        Follow follow = Follow.builder()
            .follower(follower)
            .following(following)
            .build();

        assertThat(follow.getFollower().getEmail()).isEqualTo("follower@test.com");
        assertThat(follow.getFollowing().getEmail()).isEqualTo("following@test.com");
    }
}