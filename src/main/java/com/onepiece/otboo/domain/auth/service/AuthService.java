package com.onepiece.otboo.domain.auth.service;

import com.onepiece.otboo.domain.auth.dto.data.RefreshTokenData;
import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.exception.TokenExpiredException;
import com.onepiece.otboo.domain.auth.exception.TokenForgedException;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.mapper.UserMapper;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.mapper.CustomUserDetailsMapper;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsMapper customUserDetailsMapper;
    private final UserMapper userMapper;

    @Transactional
    public RefreshTokenData refreshToken(String refreshToken) {
        validateRefreshTokenOrThrow(refreshToken);

        String email = extractEmailOrThrow(refreshToken);
        User user = findUserOrThrow(email);
        CustomUserDetails userDetails = customUserDetailsMapper.toCustomUserDetails(user);

        try {
            String newAccessToken = jwtProvider.generateAccessToken(userDetails);
            UserDto userDto = userMapper.toDto(user, null);
            String newRefreshToken = jwtProvider.generateRefreshToken(userDetails);
            return new RefreshTokenData(new JwtDto(newAccessToken, userDto), newRefreshToken);
        } catch (Exception e) {
            throw new TokenForgedException();
        }
    }

    private void validateRefreshTokenOrThrow(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenForgedException();
        }
        boolean valid = jwtProvider.validateRefreshToken(refreshToken);
        if (!valid) {
            String email = jwtProvider.getEmailFromToken(refreshToken);
            if (email == null) {
                throw new TokenForgedException();
            } else {
                throw new TokenExpiredException();
            }
        }
    }

    private String extractEmailOrThrow(String refreshToken) {
        String email = jwtProvider.getEmailFromToken(refreshToken);
        if (email == null) {
            throw new TokenForgedException();
        }
        return email;
    }

    private User findUserOrThrow(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new TokenForgedException();
        }
        return user;
    }
}
