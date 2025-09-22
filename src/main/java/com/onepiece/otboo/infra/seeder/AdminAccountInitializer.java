package com.onepiece.otboo.infra.seeder;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${otboo.admin.email}")
    protected String adminEmail;
    @Value("${otboo.admin.password}")
    protected String adminPassword;

    @PostConstruct
    public void initializeAdminAccount() {
        Optional<User> adminOpt = userRepository.findByEmail(adminEmail);
        User admin;
        if (adminOpt.isEmpty()) {
            admin = User.builder()
                .provider(Provider.LOCAL)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .locked(false)
                .temporaryPassword(null)
                .temporaryPasswordExpirationTime(null)
                .build();
        } else {
            admin = adminOpt.get();
            admin.updatePassword(passwordEncoder.encode(adminPassword));
            admin.updateRole(Role.ADMIN);
            admin.updateLocked(false);
        }
        userRepository.save(admin);
    }
}
