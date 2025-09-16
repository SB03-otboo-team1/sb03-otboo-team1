package com.onepiece.otboo.infra.security.handler;

import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor

public class JwtLogoutHandler implements LogoutHandler {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    private final JwtProvider jwtProvider;
    private final JwtRegistry jwtRegistry;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        String bearer = request.getHeader("Authorization");
        if (bearer == null
            || !bearer.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX_LENGTH)) {
            log.debug("로그아웃 무시: Authorization 헤더 없음 또는 잘못됨");
            return;
        }
        String token = bearer.substring(BEARER_PREFIX_LENGTH).trim();
        if (!jwtProvider.validateAccessToken(token)) {
            log.debug("로그아웃 무시: 토큰이 유효하지 않거나 만료됨");
            return;
        }
        String email = jwtProvider.getEmailFromToken(token);
        if (email == null || email.isBlank()) {
            log.debug("로그아웃 무시: 토큰에 이메일 정보 없음");
            return;
        }

        try {
            var userDetails = userDetailsService.loadUserByUsername(email);
            UUID userId = userDetails.getUserId();
            jwtRegistry.invalidateAllTokens(userId, Instant.now());
        } catch (UsernameNotFoundException ignored) {
        }
    }
}
