package com.onepiece.otboo.infra.security.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.exception.TokenCreateFailedException;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.mapper.UserMapper;
import com.onepiece.otboo.domain.user.repository.UserRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtProvider jwtProvider;
    private final JwtRegistry jwtRegistry;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserMapper userMapper;
    private final SecurityErrorResponseHandler responseHandler;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            responseHandler.handle(response, new SecurityUnauthorizedException());
            return;
        }

        jwtRegistry.blacklistAllTokens(userDetails.getUserId());

        String accessToken;
        String refreshToken;

        try {
            accessToken = jwtProvider.generateAccessToken(userDetails);
            refreshToken = jwtProvider.generateRefreshToken(userDetails);
        } catch (Exception e) {
            responseHandler.handle(response, new TokenCreateFailedException(e));
            return;
        }

        // 사용자 검증
        User user = userRepository.findById(userDetails.getUserId())
            .orElse(null);
        if (user == null) {
            responseHandler.handle(response, new SecurityUnauthorizedException());
            return;
        }

        response.addCookie(jwtProvider.generateRefreshTokenCookie(refreshToken));
        Profile profile = profileRepository.findByUserId(userDetails.getUserId()).orElse(null);
        UserDto userDto = userMapper.toDto(user, profile);
        JwtDto jwtDto = new JwtDto(accessToken, userDto);

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), jwtDto);
    }
}
