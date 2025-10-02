package com.onepiece.otboo.infra.seeder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AdminAccountInitializerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AdminAccountInitializer adminAccountInitializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminAccountInitializer.adminEmail = "admin@example.com";
        adminAccountInitializer.adminPassword = "admin1234";
    }

    @Test
    void 어드민_계정_없으면_생성() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        adminAccountInitializer.initializeAdminAccount();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void 어드민_계정_있으면_초기화() {
        User admin = User.builder()
            .socialAccount(SocialAccount.builder().build())
            .email("admin@example.com")
            .password("old")
            .role(Role.USER)
            .locked(true)
            .build();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(admin));
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        adminAccountInitializer.initializeAdminAccount();
        verify(userRepository, times(1)).save(admin);
    }
}
