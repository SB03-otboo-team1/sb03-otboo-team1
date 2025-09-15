package com.onepiece.otboo.infra.security.handler;

import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

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
        if (bearer == null || !bearer.startsWith(BEARER_PREFIX)) {
            return;
        }
        String token = bearer.substring(BEARER_PREFIX_LENGTH);
        if (!jwtProvider.validateAccessToken(token)) {
            return;
        }
        String email = jwtProvider.getEmailFromToken(token);
        if (email == null) {
            return;
        }
        var userDetails = userDetailsService.loadUserByUsername(email);
        UUID userId = userDetails.getUserId();
        jwtRegistry.invalidateAllTokens(userId, Instant.now());
    }
}
