package com.onepiece.otboo.infra.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    private final JwtProvider jwtProvider = mock(JwtProvider.class);

    private JwtAuthenticationFilter createFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        Mockito.reset(jwtProvider);
    }

    @Test
    void 유효한_토큰이면_보안_컨텍스트를_설정한다() throws ServletException, IOException {
        JwtAuthenticationFilter filter = createFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "token");
        given(jwtProvider.validateAccessToken("access-token")).willReturn(true);
        given(jwtProvider.getAuthentication("access-token")).willReturn(authentication);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(
            authentication);
    }

    @Test
    void 유효하지_않은_토큰이면_컨텍스트를_초기화한다() throws ServletException, IOException {
        JwtAuthenticationFilter filter = createFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        given(jwtProvider.validateAccessToken("expired-token")).willReturn(false);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("before", "cred")
        );

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtProvider, never()).getAuthentication(anyString());
    }

    @Test
    void 잘못된_Bearer_헤더이면_기존_컨텍스트를_유지한다() throws ServletException, IOException {
        JwtAuthenticationFilter filter = createFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic credentials");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        Authentication before = new UsernamePasswordAuthenticationToken("before", "cred");
        SecurityContextHolder.getContext().setAuthentication(before);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(before);
        verify(jwtProvider, never()).validateAccessToken(anyString());
    }
}