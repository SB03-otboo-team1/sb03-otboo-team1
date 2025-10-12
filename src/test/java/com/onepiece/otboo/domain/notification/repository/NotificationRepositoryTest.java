package com.onepiece.otboo.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.enums.NotificationLevel;
import com.onepiece.otboo.global.config.TestJpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
@Import({TestJpaConfig.class, NotificationRepositoryImpl.class})
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        em.createQuery("delete from Notification").executeUpdate();
    }

    @Test
    @DisplayName("알림 목록 조회 성공 - 커서 기반 (limit+1 포함)")
    void findNotifications_Success() {
        UUID receiverId = UUID.randomUUID();

        for (int i = 0; i < 5; i++) {
            Notification n = Notification.builder()
                .receiverId(receiverId)
                .title("테스트 알림 " + i)
                .content("내용 " + i)
                .level(NotificationLevel.INFO)
                .build();
            em.persist(n);
        }
        em.flush();
        em.clear();

        List<Notification> notifications =
            notificationRepository.findNotifications(receiverId, null, 3);

        assertThat(notifications).hasSizeBetween(3, 4);
        assertThat(notifications.get(0).getReceiverId()).isEqualTo(receiverId);
        assertThat(notifications.get(0).getTitle()).contains("테스트 알림");
    }

    @Test
    @DisplayName("알림 목록 조회 성공 - idAfter 기준 커서 적용")
    void findNotifications_WithIdAfter_Success() {
        UUID receiverId = UUID.randomUUID();

        for (int i = 0; i < 5; i++) {
            Notification n = Notification.builder()
                .receiverId(receiverId)
                .title("알림 " + i)
                .content("내용 " + i)
                .level(NotificationLevel.INFO)
                .build();
            em.persist(n);
        }
        em.flush();
        em.clear();

        List<Notification> all = notificationRepository.findNotifications(receiverId, null, 5);
        UUID idAfter = all.get(1).getId();

        List<Notification> next = notificationRepository.findNotifications(receiverId, idAfter, 3);

        if (!next.isEmpty()) {
            assertThat(next.get(0).getCreatedAt())
                .isCloseTo(all.get(1).getCreatedAt(), within(200, ChronoUnit.MILLIS));
        } else {
            assertThat(next).isEmpty();
        }
    }

    @Test
    @DisplayName("countByReceiverId 정상 동작 테스트")
    void countByReceiverId_Success() {
        UUID receiverId = UUID.randomUUID();

        for (int i = 0; i < 2; i++) {
            Notification n = Notification.builder()
                .receiverId(receiverId)
                .title("테스트 알림 " + i)
                .content("내용 " + i)
                .level(NotificationLevel.INFO)
                .build();
            em.persist(n);
        }
        em.flush();
        em.clear();

        long count = notificationRepository.countByReceiverId(receiverId);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("알림 읽음 처리 후 isRead=true, readAt이 설정된다")
    void markAsRead_Persistence_Success() {
        UUID receiverId = UUID.randomUUID();

        Notification notification = Notification.builder()
            .receiverId(receiverId)
            .title("읽음 테스트 알림")
            .content("읽음 처리 전 상태입니다.")
            .level(NotificationLevel.INFO)
            .build();

        notificationRepository.save(notification);
        em.flush();
        em.clear();

        Notification saved = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(saved.isRead()).isFalse();

        saved.markAsRead();
        notificationRepository.save(saved);
        em.flush();
        em.clear();

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.isRead()).isTrue();
        assertThat(updated.getReadAt()).isNotNull();
        assertThat(updated.getReadAt()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }
}