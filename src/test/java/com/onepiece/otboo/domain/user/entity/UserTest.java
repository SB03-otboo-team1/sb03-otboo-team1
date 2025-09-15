package com.onepiece.otboo.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import java.time.Instant;
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

    @Test
    void 임시_비밀번호_설정_및_유효성_검증() {
        String tempPassword = "temp123";
        Instant expiration = Instant.now().plusSeconds(60);
        User user = UserFixture.createUserWithTemporaryPassword(tempPassword, expiration);
        assertThat(user.getTemporaryPassword()).isEqualTo(tempPassword);
        assertThat(user.getTemporaryPasswordExpirationTime()).isEqualTo(expiration);
        assertThat(user.isTemporaryPasswordValid(tempPassword)).isTrue();
    }

    @Test
    void 임시_비밀번호_만료_시_유효성_실패() {
        String tempPassword = "temp123";
        Instant expiration = Instant.now().minusSeconds(10); // 이미 만료
        User user = UserFixture.createUserWithTemporaryPassword(tempPassword, expiration);
        assertThat(user.isTemporaryPasswordValid(tempPassword)).isFalse();
    }

    @Test
    void 임시_비밀번호_또는_만료시간_null_이면_항상_false() {
        User user = UserFixture.createUser();
        // 임시 비밀번호/만료시간 모두 null
        assertThat(user.isTemporaryPasswordValid("any")).isFalse();
        // 임시 비밀번호만 설정
        user = UserFixture.createUserWithTemporaryPassword("temp", null);
        assertThat(user.isTemporaryPasswordValid("temp")).isFalse();
        // 만료시간만 설정
        user = UserFixture.createUserWithTemporaryPassword(null, Instant.now().plusSeconds(60));
        assertThat(user.isTemporaryPasswordValid("temp")).isFalse();
    }
}
