package com.onepiece.otboo.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import org.junit.jupiter.api.Test;

public class UserTest {

    @Test
    void user_객체_생성() {
        User user = UserFixture.createUser();
        assertThat(user.getProvider()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.isLocked()).isFalse();
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void 비밀번호_변경_테스트() {
        User user = UserFixture.createUser();
        user.updatePassword("newPassword");
        assertThat(user.getPassword()).isEqualTo("newPassword");
    }

    @Test
    void 잠금_상태_변경_테스트() {
        User user = UserFixture.createUser();
        user.updateLocked(true);
        assertThat(user.isLocked()).isTrue();
    }

    @Test
    void 역할_변경_테스트() {
        User user = UserFixture.createUser();
        user.updateRole(Role.ADMIN);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }
}
