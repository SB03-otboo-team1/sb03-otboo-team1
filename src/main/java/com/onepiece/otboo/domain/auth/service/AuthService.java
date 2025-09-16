package com.onepiece.otboo.domain.auth.service;

import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.exception.UnAuthorizedException;
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
