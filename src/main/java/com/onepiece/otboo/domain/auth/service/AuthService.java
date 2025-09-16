package com.onepiece.otboo.domain.auth.service;

import com.nimbusds.jose.JOSEException;
import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.exception.TokenCreateFailedException;
import com.onepiece.otboo.domain.auth.exception.UnAuthorizedException;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.mapper.UserMapper;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import com.onepiece.otboo.infra.security.mapper.CustomUserDetailsMapper;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtRegistry jwtRegistry;
    private final CustomUserDetailsMapper customUserDetailsMapper;
    private final UserMapper userMapper;

    @Transactional
    public JwtDto login(String username, String password) {
        User user = userRepository.findByEmail(username)
            .orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new UnAuthorizedException();
        }

        jwtRegistry.invalidateAllTokens(user.getId(), Instant.now());

        CustomUserDetails userDetails = customUserDetailsMapper.toCustomUserDetails(user);

        String token;
        try {
            token = jwtProvider.generateAccessToken(userDetails);
        } catch (JOSEException e) {
            throw new TokenCreateFailedException(e);
        }

        UserDto userDto = userMapper.toDto(user, null);
        return new JwtDto(token, userDto);
    }

    @Transactional
    public JwtDto refreshToken(String refreshToken) {
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new UnAuthorizedException();
        }
        String email = jwtProvider.getEmailFromToken(refreshToken);
        if (email == null) {
            throw new UnAuthorizedException();
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new UnAuthorizedException();
        }
        CustomUserDetails userDetails = customUserDetailsMapper.toCustomUserDetails(user);
        try {
            String newAccessToken = jwtProvider.generateAccessToken(userDetails);
            UserDto userDto = userMapper.toDto(user, null);
            jwtProvider.generateRefreshToken(userDetails);
            return new JwtDto(newAccessToken, userDto);
        } catch (Exception e) {
            throw new UnAuthorizedException();
        }
    }
}
