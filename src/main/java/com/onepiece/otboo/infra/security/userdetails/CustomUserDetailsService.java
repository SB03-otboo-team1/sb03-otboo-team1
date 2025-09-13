package com.onepiece.otboo.infra.security.userdetails;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .map(user -> toCustomUserDetails(user))
            .orElseThrow(() -> new UsernameNotFoundException("사용자 이메일 조회 실패: " + email));
    }

    public static CustomUserDetails toCustomUserDetails(User user) {
        return new CustomUserDetails(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getRole(),
            user.isLocked(),
            user.getTemporaryPassword(),
            user.getTemporaryPasswordExpirationTime()
        );
    }
}
