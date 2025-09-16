package com.onepiece.otboo.domain.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.exception.UnAuthorizedException;
import com.onepiece.otboo.domain.auth.service.AuthService;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.infra.security.config.TestSecurityConfig;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
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

    @Test
    void 리프레시_토큰_재발급_성공() throws Exception {
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        UserDto userDto = Mockito.mock(UserDto.class);
        JwtDto jwtDto = new JwtDto(newAccessToken, userDto);
        given(authService.refreshToken(refreshToken)).willReturn(jwtDto);
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
            .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.name()));
    }

    @Test
    void 리프레시_토큰_위조_실패() throws Exception {
        String forgedRefreshToken = "forged-refresh-token";
        given(authService.refreshToken(forgedRefreshToken)).willThrow(new UnAuthorizedException());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh")
                .cookie(new Cookie("REFRESH_TOKEN", forgedRefreshToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.name()));
    }
}
