package com.onepiece.otboo.domain.auth.service;

import com.nimbusds.jose.JOSEException;
import com.onepiece.otboo.domain.auth.exception.CustomAuthException;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserException;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
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

    @Transactional
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> UserNotFoundException.byEmail(email));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserException(ErrorCode.INVALID_PASSWORD);
        }

        jwtRegistry.invalidateAllTokens(user.getId());

        CustomUserDetails userDetails = new CustomUserDetails(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getRole(),
            user.isLocked(),
            user.getTemporaryPassword(),
            user.getTemporaryPasswordExpirationTime()
        );

        try {
            return jwtProvider.generateAccessToken(userDetails);
        } catch (JOSEException e) {
            throw new CustomAuthException(ErrorCode.TOKEN_CREATE_FAILED, e);
        }
    }
}
