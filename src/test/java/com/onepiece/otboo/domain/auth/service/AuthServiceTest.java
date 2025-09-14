package com.onepiece.otboo.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.lenient;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;

import com.nimbusds.jose.JOSEException;
import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.exception.CustomAuthException;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.exception.UserException;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private JwtRegistry jwtRegistry;

    @InjectMocks
    private AuthService authService;


    private User mockUser() {
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(UUID.randomUUID());
        lenient().when(user.getProvider()).thenReturn(Provider.LOCAL);
        lenient().when(user.getProviderUserId()).thenReturn("provider-id");
        lenient().when(user.getEmail()).thenReturn("test@email.com");
        lenient().when(user.getPassword()).thenReturn("password");
        lenient().when(user.getTemporaryPassword()).thenReturn(null);
        lenient().when(user.getTemporaryPasswordExpirationTime()).thenReturn(null);
        lenient().when(user.isLocked()).thenReturn(false);
        lenient().when(user.getRole()).thenReturn(Role.USER);
        return user;
    }

    @Test
    void 정상_로그인_토큰_반환() throws JOSEException {
        User user = mockUser();
        given(userRepository.findByEmail(any())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(any(), any())).willReturn(true);
        given(jwtProvider.generateAccessToken(any())).willReturn("jwt-token");

        JwtDto jwtDto = authService.login("test@email.com", "password");

        assertThat(jwtDto.getAccessToken()).isEqualTo("jwt-token");
        then(jwtRegistry).should().invalidateAllTokens(any());
    }

    @Test
    void 비밀번호_불일치_예외() {
        User user = mockUser();
        given(userRepository.findByEmail(any())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(any(), any())).willReturn(false);

        assertThatThrownBy(() -> authService.login("test@email.com", "wrong"))
            .isInstanceOf(UserException.class)
            .hasMessageContaining(ErrorCode.INVALID_PASSWORD.getMessage());
    }

    @Test
    void 이메일_없음_예외() {
        given(userRepository.findByEmail(any())).willReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login("notfound@email.com", "password"))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void 토큰_생성_실패_예외() throws JOSEException {
        User user = mockUser();
        given(userRepository.findByEmail(any())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(any(), any())).willReturn(true);
        given(jwtProvider.generateAccessToken(any())).willThrow(new JOSEException("fail"));

        assertThatThrownBy(() -> authService.login("test@email.com", "password"))
            .isInstanceOf(CustomAuthException.class)
            .hasMessageContaining(ErrorCode.TOKEN_CREATE_FAILED.getMessage());
    }
}
