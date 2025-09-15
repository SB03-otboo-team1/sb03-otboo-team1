package com.onepiece.otboo.domain.auth.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.exception.TokenCreateFailedException;
import com.onepiece.otboo.domain.auth.exception.UnAuthorizedException;
import com.onepiece.otboo.domain.auth.service.AuthService;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.infra.security.config.TestSecurityConfig;
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
        given(authService.login(anyString(), anyString())).willReturn(new JwtDto(jwt, null));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/sign-in")
                .content("{\"username\":\"test@email.com\",\"password\":\"password123\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value(jwt));
    }

    @Test
    void 이메일_없음_404() throws Exception {
        given(authService.login(anyString(), anyString()))
            .willThrow(new UserNotFoundException());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/sign-in")
                .content("{\"username\":\"notfound@email.com\",\"password\":\"password123\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.name()));
    }

    @Test
    void 비밀번호_불일치_401() throws Exception {
        given(authService.login(anyString(), anyString()))
            .willThrow(new UnAuthorizedException());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/sign-in")
                .content("{\"username\":\"test@email.com\",\"password\":\"wrong\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.name()));
    }

    @Test
    void 토큰_생성_실패_500() throws Exception {
        given(authService.login(anyString(), anyString()))
            .willThrow(new TokenCreateFailedException());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/sign-in")
                .content("{\"username\":\"test@email.com\",\"password\":\"password123\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value(ErrorCode.TOKEN_CREATE_FAILED.name()));
    }
}
