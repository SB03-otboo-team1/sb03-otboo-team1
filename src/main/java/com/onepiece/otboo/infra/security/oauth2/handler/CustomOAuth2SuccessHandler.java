package com.onepiece.otboo.infra.security.oauth2.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.exception.TokenCreateFailedException;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.infra.security.exception.SecurityUnauthorizedException;
import com.onepiece.otboo.infra.security.handler.SecurityErrorResponseHandler;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtProvider jwtProvider;
    private final JwtRegistry jwtRegistry;
    private final SecurityErrorResponseHandler responseHandler;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            log.debug("OAuth2 인증 실패: {}", principal);
            responseHandler.handle(response, new SecurityUnauthorizedException());
            return;
        }

        UserDto userDto = userDetails.getUserDto();
        if (userDto == null) {
            log.debug("OAuth2 인증 실패 유저 정보 누락");
            responseHandler.handle(response, new SecurityUnauthorizedException());
            return;
        }

        jwtRegistry.blacklistAllTokens(userDetails.getUserId());

        try {
            String accessToken = jwtProvider.generateAccessToken(userDetails);
            String refreshToken = jwtProvider.generateRefreshToken(userDetails);
            response.addCookie(jwtProvider.generateRefreshTokenCookie(refreshToken));
            JwtDto jwtDto = new JwtDto(accessToken, userDto);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), jwtDto);
        } catch (Exception e) {
            responseHandler.handle(response, new TokenCreateFailedException(e));
        }
    }
}
