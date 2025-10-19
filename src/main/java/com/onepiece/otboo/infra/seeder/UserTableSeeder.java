package com.onepiece.otboo.infra.seeder;

import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class UserTableSeeder implements DataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void seed() {
        if (userRepository.count() > 1) {
            return;
        }
        int count = 0;
        for (int i = 1; i <= 10; i++) {
            User user = User.builder()
                .socialAccount(SocialAccount.builder().build())
                .email("user" + i + "@example.com")
                .password(passwordEncoder.encode("!qwe1234"))
                .role(Role.USER)
                .locked(false)
                .temporaryPassword(null)
                .temporaryPasswordExpirationTime(null)
                .build();
            userRepository.save(user);
            count++;
        }
        log.info("UserTableSeeder: {}개의 유저 더미 데이터가 추가되었습니다.", count);
    }
}
