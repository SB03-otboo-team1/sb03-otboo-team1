package com.onepiece.otboo.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.auth.service.AuthService;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.api.mail.service.MailService;
import com.onepiece.otboo.infra.security.auth.CustomAuthenticationProvider;
import com.onepiece.otboo.infra.security.config.SecurityConfig;
import com.onepiece.otboo.infra.security.config.TestPropertiesScanConfig;
import com.onepiece.otboo.infra.security.handler.JwtLoginFailureHandler;
import com.onepiece.otboo.infra.security.handler.JwtLoginSuccessHandler;
import com.onepiece.otboo.infra.security.handler.JwtLogoutHandler;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ActiveProfiles("test-security")
@Import({SecurityConfig.class, TestPropertiesScanConfig.class})
@WebMvcTest(AuthController.class)
class CsrfTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtProvider jwtProvider;
    @MockitoBean
    private JwtLoginSuccessHandler jwtLoginSuccessHandler;
    @MockitoBean
    private JwtLoginFailureHandler jwtLoginFailureHandler;
    @MockitoBean
    private JwtLogoutHandler jwtLogoutHandler;
    @MockitoBean
    private MailService mailService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Test
    void CSRF_토큰_발급_엔드포인트는_204를_쿠키와_반환한다() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/auth/csrf-token"));
        result.andExpect(status().isNoContent());
        Cookie cookie = result.andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isNotEmpty();
    }
}