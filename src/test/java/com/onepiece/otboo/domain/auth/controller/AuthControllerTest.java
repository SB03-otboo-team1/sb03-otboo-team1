package com.onepiece.otboo.domain.auth.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.auth.exception.CustomAuthException;
import com.onepiece.otboo.domain.auth.service.AuthService;
import com.onepiece.otboo.domain.user.exception.UserException;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.global.config.TestSecurityConfig;
import com.onepiece.otboo.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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

    @Test
    void 로그인_성공_JWT_반환() throws Exception {
        String jwt = "jwt-token";
        given(authService.login(anyString(), anyString())).willReturn(jwt);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/sign-in")
                .param("username", "test@email.com")
                .param("password", "password123")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value(jwt));
    }

    @Test
    void 이메일_없음_401() throws Exception {
        given(authService.login(anyString(), anyString()))
            .willThrow(new UserNotFoundException());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/sign-in")
                .param("username", "notfound@email.com")
                .param("password", "password123")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value(ErrorCode.USER_NOT_FOUND.name()));
    }

    @Test
    void 비밀번호_불일치_401() throws Exception {
        given(authService.login(anyString(), anyString()))
            .willThrow(new UserException(
                ErrorCode.INVALID_PASSWORD));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/sign-in")
                .param("username", "test@email.com")
                .param("password", "wrong")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value(ErrorCode.INVALID_PASSWORD.name()));
    }

    @Test
    void 토큰_생성_실패_500() throws Exception {
        given(authService.login(anyString(), anyString()))
            .willThrow(new CustomAuthException(
                ErrorCode.TOKEN_CREATE_FAILED));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/sign-in")
                .param("username", "test@email.com")
                .param("password", "password123")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value(ErrorCode.TOKEN_CREATE_FAILED.name()));
    }
}
