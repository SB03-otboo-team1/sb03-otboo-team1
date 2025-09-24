package com.onepiece.otboo.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void user_객체_생성() {
        User user = UserFixture.createUser();
        assertThat(user.getSocialAccount().getProvider()).isNotNull();
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
        User user = UserFixture.createUser();
        user.updateTemporaryPassword(tempPassword, encoder, 60);
        assertThat(encoder.matches(tempPassword, user.getTemporaryPassword())).isTrue();
        assertThat(user.getTemporaryPasswordExpirationTime()).isNotNull();
        assertThat(user.isTemporaryPasswordValid(tempPassword, encoder)).isTrue();
    }

    @Test
    void 임시_비밀번호_만료_시_유효성_실패() {
        String tempPassword = "temp123";
        User user = UserFixture.createUser();
        user.updateTemporaryPassword(tempPassword, encoder, -10); // 이미 만료
        assertThat(user.isTemporaryPasswordValid(tempPassword, encoder)).isFalse();
    }

    @Test
    void 임시_비밀번호_또는_만료시간_null_이면_항상_false() {
        User user = UserFixture.createUser();
        assertThat(user.isTemporaryPasswordValid("any", encoder)).isFalse();

        // 임시 비밀번호만 설정 후 클리어
        user.updateTemporaryPassword("temp", encoder, 0);
        user.clearTemporaryPassword();
        assertThat(user.isTemporaryPasswordValid("temp", encoder)).isFalse();
    }
}
