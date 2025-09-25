package com.onepiece.otboo.domain.auth.service.social;

import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 인증 연동/매핑 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SocialAuthService {

    private final UserRepository userRepository;

    public User linkOrCreateUser(Provider provider, String providerUserId, String email) {
        SocialAccount socialAccount = SocialAccount.builder()
            .provider(provider)
            .providerUserId(providerUserId)
            .build();

        Optional<User> byProvider = userRepository.findBySocialAccountProviderAndSocialAccountProviderUserId(
            provider, providerUserId);
        if (byProvider.isPresent()) {
            return byProvider.get();
        }

        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            User updated = User.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .temporaryPassword(user.getTemporaryPassword())
                .temporaryPasswordExpirationTime(user.getTemporaryPasswordExpirationTime())
                .locked(user.isLocked())
                .role(user.getRole())
                .socialAccount(socialAccount)
                .build();
            return userRepository.save(updated);
        }

        User newUser = User.builder()
            .email(email)
            .socialAccount(socialAccount)
            .password("")
            .locked(false)
            .role(Role.USER)
            .build();

        return userRepository.save(newUser);
    }
}
