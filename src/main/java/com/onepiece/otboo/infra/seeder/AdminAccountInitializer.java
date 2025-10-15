package com.onepiece.otboo.infra.seeder;

import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.security.lock.AdminAccountLock;
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
    private final AdminAccountLock adminAccountLock;

    @Value("${otboo.admin.email}")
    protected String adminEmail;
    @Value("${otboo.admin.password}")
    protected String adminPassword;

    @PostConstruct
    public void initializeAdminAccount() {

        adminAccountLock.acquireLock(ADMIN_LOCK_KEY);
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
            adminAccountLock.releaseLock(ADMIN_LOCK_KEY);
        }
    }
}
