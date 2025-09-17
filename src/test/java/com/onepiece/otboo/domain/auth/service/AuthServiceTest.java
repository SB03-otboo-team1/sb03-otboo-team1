package com.onepiece.otboo.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willThrow;

import com.onepiece.otboo.domain.auth.dto.data.RefreshTokenData;
import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.exception.TokenExpiredException;
import com.onepiece.otboo.domain.auth.exception.TokenForgedException;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.mapper.UserMapper;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.security.fixture.UserDetailsFixture;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.mapper.CustomUserDetailsMapper;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private CustomUserDetailsMapper customUserDetailsMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private final String validToken = "valid-token";
    private final String expiredToken = "expired-token";
    private final String forgedToken = "forged-token";
    private final String email = "test@example.com";

    @BeforeEach
    void setup() {
        Mockito.reset(userRepository, jwtProvider, customUserDetailsMapper, userMapper);
        ReflectionTestUtils.setField(authService, "charset", "abcdefghijklmnopqrstuvwxyz");
        ReflectionTestUtils.setField(authService, "temporaryPasswordValiditySeconds", 60);
    }

    @Test
    void 정상_리프레시_토큰_재발급_성공() throws Exception {
        CustomUserDetails userDetails = UserDetailsFixture.createUser();
        User user = mock(User.class);
        UserDto userDto = mock(UserDto.class);
        given(jwtProvider.validateRefreshToken(validToken)).willReturn(true);
        given(jwtProvider.getEmailFromToken(validToken)).willReturn(email);
        given(userRepository.findByEmail(email)).willReturn(java.util.Optional.of(user));
        given(customUserDetailsMapper.toCustomUserDetails(user)).willReturn(userDetails);
        given(jwtProvider.generateAccessToken(userDetails)).willReturn("access-token");
        given(userMapper.toDto(user, null)).willReturn(userDto);
        given(jwtProvider.generateRefreshToken(userDetails)).willReturn("refresh-token");

        RefreshTokenData result = authService.refreshToken(validToken);
        JwtDto jwtDto = result.jwtDto();

        assertNotNull(jwtDto);
        assertEquals("access-token", jwtDto.accessToken());
        assertEquals(userDto, jwtDto.userDto());
    }

    @Test
    void 만료된_리프레시_토큰_예외발생() {
        given(jwtProvider.validateRefreshToken(expiredToken)).willReturn(false);
        given(jwtProvider.getEmailFromToken(expiredToken)).willReturn(email);

        assertThrows(TokenExpiredException.class, () -> authService.refreshToken(expiredToken));
    }

    @Test
    void 위조된_리프레시_토큰_예외발생() {
        given(jwtProvider.validateRefreshToken(forgedToken)).willReturn(false);
        given(jwtProvider.getEmailFromToken(forgedToken)).willReturn(null);

        assertThrows(TokenForgedException.class, () -> authService.refreshToken(forgedToken));
    }

    @Test
    void null_리프레시_토큰_예외발생() {
        assertThrows(TokenForgedException.class, () -> authService.refreshToken(null));
    }

    @Test
    void 공백_리프레시_토큰_예외발생() {
        String blankToken = "   ";

        assertThrows(TokenForgedException.class, () -> authService.refreshToken(blankToken));
    }

    @Test
    void 리프레시_토큰_유저없음_예외발생() {
        given(jwtProvider.validateRefreshToken(validToken)).willReturn(true);
        given(jwtProvider.getEmailFromToken(validToken)).willReturn(email);
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(TokenForgedException.class, () -> authService.refreshToken(validToken));
    }

    @Test
    void 액세스_토큰_생성실패_예외발생() throws Exception {
        CustomUserDetails userDetails = UserDetailsFixture.createUser();
        User user = mock(User.class);
        given(jwtProvider.validateRefreshToken(validToken)).willReturn(true);
        given(jwtProvider.getEmailFromToken(validToken)).willReturn(email);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(customUserDetailsMapper.toCustomUserDetails(user)).willReturn(userDetails);
        willThrow(new RuntimeException("fail")).given(jwtProvider).generateAccessToken(userDetails);

        assertThrows(TokenForgedException.class, () -> authService.refreshToken(validToken));
    }

    @Test
    void 임시_비밀번호_생성_및_저장_성공() {
        User user = mock(User.class);

        String rawTempPassword = authService.generateTemporaryPassword();
        assertNotNull(rawTempPassword);
        assertEquals(10, rawTempPassword.length());

        authService.saveTemporaryPassword(user);

        verify(user).clearTemporaryPassword();
        verify(user).updateTemporaryPassword(any(), any(), anyLong());
    }
}
