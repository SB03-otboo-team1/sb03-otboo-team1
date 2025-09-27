package com.onepiece.otboo.infra.security.oauth2.handler;

import com.onepiece.otboo.domain.auth.exception.TokenCreateFailedException;
import com.onepiece.otboo.infra.security.exception.SecurityUnauthorizedException;
import com.onepiece.otboo.infra.security.handler.SecurityErrorResponseHandler;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final JwtRegistry jwtRegistry;
    private final SecurityErrorResponseHandler responseHandler;
    private static final String REDIRECT_URL = "/";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            log.debug("[CustomOAuth2SuccessHandler] OAuth2 인증 실패: {}", principal);
            responseHandler.handle(response, new SecurityUnauthorizedException());
            return;
        }

        jwtRegistry.blacklistAllTokens(userDetails.getUserId());

        try {
            String refreshToken = jwtProvider.generateRefreshToken(userDetails);
            response.addCookie(jwtProvider.generateRefreshTokenCookie(refreshToken));
            response.sendRedirect(REDIRECT_URL);
        } catch (Exception e) {
            responseHandler.handle(response, new TokenCreateFailedException(e));
        }
    }
}
