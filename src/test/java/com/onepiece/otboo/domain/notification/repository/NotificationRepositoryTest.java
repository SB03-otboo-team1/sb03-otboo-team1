package com.onepiece.otboo.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@EnableJpaAuditing
@DataJpaTest(showSql = true)
@EnableJpaRepositories(basePackages = "com.onepiece.otboo.domain")
@EntityScan(basePackages = "com.onepiece.otboo.domain")
@Import(NotificationRepositoryTest.QuerydslTestConfig.class)
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @TestConfiguration
    static class QuerydslTestConfig {

        @Bean
        public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
            return new JPAQueryFactory(entityManager);
        }
    }

    @Test
    @DisplayName("receiverId 기준 알림 목록 조회 성공")
    void findAllByReceiverIdOrderByCreatedAtDesc_Success() {
        User user = User.builder()
            .email("test@example.com")
            .password("1234")
            .role(Role.USER)
            .locked(false)
            .build();
        userRepository.save(user);

        Notification notification1 = Notification.builder()
            .receiverId(user.getId())
            .title("첫 번째 알림")
            .content("테스트 내용 1")
            .level(Level.INFO)
            .build();
        ReflectionTestUtils.setField(notification1, "createdAt", Instant.now().minusSeconds(60));

        Notification notification2 = Notification.builder()
            .receiverId(user.getId())
            .title("두 번째 알림")
            .content("테스트 내용 2")
            .level(Level.INFO)
            .build();
        ReflectionTestUtils.setField(notification2, "createdAt", Instant.now());

        notificationRepository.saveAll(List.of(notification1, notification2));

        List<Notification> result = notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(
            user.getId()
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("두 번째 알림");
        assertThat(result.get(1).getTitle()).isEqualTo("첫 번째 알림");
    }
}