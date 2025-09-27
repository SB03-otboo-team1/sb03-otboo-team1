package com.onepiece.otboo.infra.security.jwt.handler;

import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
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

    @Override
    public void logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        // Access Token 기반 전체 세션 만료
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.regionMatches(true, 0, BEARER_PREFIX, 0,
            BEARER_PREFIX_LENGTH)) {
            String token = bearer.substring(BEARER_PREFIX_LENGTH).trim();
            if (jwtProvider.validateAccessToken(token)) {
                UUID userId = jwtProvider.getUserIdFromToken(token);
                if (userId != null) {
                    jwtRegistry.blacklistAllTokens(userId);
                    log.debug("로그아웃: userId {}의 모든 세션 만료", userId);
                }
            } else {
                log.debug("로그아웃 무시: 액세스 토큰이 유효하지 않거나 만료됨");
            }
        } else {
            log.debug("로그아웃 무시: Authorization 헤더 없음 또는 잘못됨");
        }

        // Refresh Token 쿠키 만료 처리
        try {
            var cookies = request.getCookies();
            if (cookies != null) {
                for (var cookie : cookies) {
                    if (JwtProvider.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                        response.addCookie(jwtProvider.generateRefreshTokenExpirationCookie());
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
