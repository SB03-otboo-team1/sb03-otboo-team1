package com.onepiece.otboo.infra.security.auth;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.security.mapper.CustomUserDetailsMapper;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsMapper customUserDetailsMapper;

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (user.isLocked()) {
            throw new LockedException("계정이 잠겨 있습니다.");
        }
        // 임시 비밀번호 처리
        if (user.isTemporaryPasswordValid(password, passwordEncoder)) {
            user.clearTemporaryPassword();
            userRepository.save(user);
            CustomUserDetails userDetails = customUserDetailsMapper.toCustomUserDetails(user);
            return new UsernamePasswordAuthenticationToken(userDetails, password,
                userDetails.getAuthorities());
        }
        // 비밀번호가 없는 경우 소셜 또는 비정상
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (passwordEncoder.matches(password, user.getPassword())) {
            CustomUserDetails userDetails = customUserDetailsMapper.toCustomUserDetails(user);
            return new UsernamePasswordAuthenticationToken(userDetails, password,
                userDetails.getAuthorities());
        }
        throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
