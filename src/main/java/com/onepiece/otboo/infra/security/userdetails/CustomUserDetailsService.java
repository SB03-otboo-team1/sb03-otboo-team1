package com.onepiece.otboo.infra.security.userdetails;

import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.security.mapper.CustomUserDetailsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CustomUserDetailsMapper customUserDetailsMapper;

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .map(customUserDetailsMapper::toCustomUserDetails)
            .orElseThrow(() -> new UsernameNotFoundException("사용자 이메일 조회 실패: " + email));
    }
}
