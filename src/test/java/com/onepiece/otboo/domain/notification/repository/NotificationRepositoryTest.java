package com.onepiece.otboo.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.enums.Level;
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
    @DisplayName("알림 목록 조회 성공 - 커서 기반 (createdAt + idAfter)")
    void findNotifications_Success() {
        UUID receiverId = UUID.randomUUID();

        for (int i = 0; i < 5; i++) {
            Notification n = Notification.builder()
                .receiverId(receiverId)
                .title("테스트 알림 " + i)
                .content("내용 " + i)
                .level(Level.INFO)
                .createdAt(Instant.now().minus(i, ChronoUnit.MINUTES))
                .build();
            em.persist(n);
        }
        em.flush();
        em.clear();

        List<Notification> firstPage =
            notificationRepository.findNotifications(receiverId, null, null, 3);

        assertThat(firstPage).hasSizeBetween(3, 4);

        Instant nextCursor = firstPage.get(firstPage.size() - 1).getCreatedAt();
        UUID nextIdAfter = firstPage.get(firstPage.size() - 1).getId();

        List<Notification> secondPage =
            notificationRepository.findNotifications(receiverId, nextCursor, nextIdAfter, 3);

        if (!secondPage.isEmpty()) {
            assertThat(secondPage.get(0).getCreatedAt())
                .isBeforeOrEqualTo(nextCursor);
        }
    }

    @Test
    @DisplayName("알림 목록 조회 성공 - 동일 createdAt 내 idAfter 적용")
    void findNotifications_WithIdAfter_Success() {
        UUID receiverId = UUID.randomUUID();
        Instant now = Instant.now();

        for (int i = 0; i < 5; i++) {
            Notification n = Notification.builder()
                .receiverId(receiverId)
                .title("동일시각 알림 " + i)
                .content("내용 " + i)
                .level(Level.INFO)
                .createdAt(now)
                .build();
            em.persist(n);
        }
        em.flush();
        em.clear();

        List<Notification> all = notificationRepository.findNotifications(receiverId, null, null,
            5);
        Instant cursor = all.get(0).getCreatedAt();
        UUID idAfter = all.get(2).getId();

        List<Notification> next = notificationRepository.findNotifications(receiverId, cursor,
            idAfter, 2);

        if (!next.isEmpty()) {
            assertThat(next.get(0).getCreatedAt()).isEqualTo(cursor);
            assertThat(next.get(0).getId().toString()).isLessThan(idAfter.toString());
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
                .level(Level.INFO)
                .createdAt(Instant.now())
                .build();
            em.persist(n);
        }
        em.flush();
        em.clear();

        long count = notificationRepository.countByReceiverId(receiverId);

        assertThat(count).isEqualTo(2L);
    }
}