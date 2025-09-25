package com.onepiece.otboo.domain.follow.entity;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class FollowTest {

    private User createUser(String email) {
        return User.builder()
            .provider(Provider.LOCAL)
            .providerUserId(UUID.randomUUID().toString())
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

    @Test
    @DisplayName("equals/hashCode 동작 확인")
    void equalsAndHashCode_success() {
        Follow f1 = Follow.builder().build();
        Follow f2 = Follow.builder().build();

        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(f1, "id", id);
        ReflectionTestUtils.setField(f2, "id", id);

        assertThat(f1).isEqualTo(f2);
        assertThat(f1.hashCode()).isEqualTo(f2.hashCode());
    }
}