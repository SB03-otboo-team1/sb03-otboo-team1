package com.onepiece.otboo.infra.seeder;

import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.redis.RedisLockProvider;
import com.onepiece.otboo.infra.redis.exception.RedisLockAcquisitionException;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer {

    private static final String ADMIN_LOCK_KEY = "admin-account-initializer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisLockProvider redisLockProvider;

    @Value("${otboo.admin.email}")
    protected String adminEmail;
    @Value("${otboo.admin.password}")
    protected String adminPassword;

    @PostConstruct
    public void initializeAdminAccount() {

        try {
            redisLockProvider.acquireLock(ADMIN_LOCK_KEY);
        } catch (RedisLockAcquisitionException e) {
            // 다른 인스턴스가 이미 초기화를 수행 중이거나 완료함
            log.info("관리자 계정 초기화 건너뜀: 다른 인스턴스가 락을 보유 중");
            return;
        }

        try {
            Optional<User> adminOpt = userRepository.findByEmail(adminEmail);
            User admin;
            if (adminOpt.isEmpty()) {
                log.debug("관리자 계정이 존재하지 않아 새로 생성합니다. (email: {})", adminEmail);
                admin = User.builder()
                    .socialAccount(SocialAccount.builder().build())
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .locked(false)
                    .temporaryPassword(null)
                    .temporaryPasswordExpirationTime(null)
                    .build();
            } else {
                log.debug("기존 관리자 계정이 존재하여 정보(비밀번호/권한/잠금)를 갱신합니다. (email: {})", adminEmail);
                admin = adminOpt.get();
                admin.updatePassword(passwordEncoder.encode(adminPassword));
                admin.updateRole(Role.ADMIN);
                admin.updateLocked(false);
            }
            userRepository.save(admin);
            log.debug("관리자 계정 초기화 완료 (email: {})", adminEmail);
        } finally {
            redisLockProvider.releaseLock(ADMIN_LOCK_KEY);
        }
    }
}
