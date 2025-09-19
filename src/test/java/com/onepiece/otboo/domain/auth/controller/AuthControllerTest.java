package com.onepiece.otboo.domain.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.auth.dto.data.RefreshTokenData;
import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.exception.UnAuthorizedException;
import com.onepiece.otboo.domain.auth.service.AuthService;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.api.mail.service.MailService;
import com.onepiece.otboo.infra.security.config.TestSecurityConfig;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtProvider jwtProvider;
    @MockitoBean
    private MailService mailService;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void 리프레시_토큰_재발급_성공() throws Exception {
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        UserDto userDto = Mockito.mock(UserDto.class);
        JwtDto jwtDto = new JwtDto(newAccessToken, userDto);
        RefreshTokenData refreshTokenData = new RefreshTokenData(jwtDto, refreshToken);
        given(authService.refreshToken(refreshToken)).willReturn(refreshTokenData);
        given(jwtProvider.generateRefreshTokenCookie(refreshToken)).willReturn(
            new Cookie("REFRESH_TOKEN", refreshToken));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh")
                .cookie(new Cookie("REFRESH_TOKEN", refreshToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value(newAccessToken))
            .andExpect(jsonPath("$.userDto").exists());
    }

    @Test
    void 리프레시_토큰_만료_실패() throws Exception {
        String expiredRefreshToken = "expired-refresh-token";
        given(authService.refreshToken(expiredRefreshToken)).willThrow(new UnAuthorizedException());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh")
                .cookie(new Cookie("REFRESH_TOKEN", expiredRefreshToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.exceptionName").value("UnAuthorizedException"));
    }

    @Test
    void 리프레시_토큰_위조_실패() throws Exception {
        String forgedRefreshToken = "forged-refresh-token";
        given(authService.refreshToken(forgedRefreshToken)).willThrow(new UnAuthorizedException());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh")
                .cookie(new Cookie("REFRESH_TOKEN", forgedRefreshToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.exceptionName").value("UnAuthorizedException"));
    }


    @Test
    void 임시_비밀번호_발급_성공() throws Exception {
        String email = "test@example.com";
        String body = "{\"email\": \"" + email + "\"}";
        Mockito.when(authService.saveTemporaryPassword(email)).thenReturn("dummyTempPassword");
        var mockUser = Mockito.mock(User.class);
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        Mockito.when(mockUser.getTemporaryPasswordExpirationTime())
            .thenReturn(Instant.now().plusSeconds(600));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/reset-password")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isNoContent());
    }

    @Test
    void 임시_비밀번호_발급_실패() throws Exception {
        String email = "notfound@example.com";
        String body = "{\"email\": \"" + email + "\"}";
        Mockito.doThrow(UserNotFoundException.byEmail(email)).when(authService)
            .saveTemporaryPassword(email);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/reset-password")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isNotFound());
    }
}
