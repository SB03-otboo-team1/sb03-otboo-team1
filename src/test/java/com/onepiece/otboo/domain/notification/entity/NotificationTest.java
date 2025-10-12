package com.onepiece.otboo.domain.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;

import com.onepiece.otboo.domain.notification.enums.NotificationLevel;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class NotificationTest {

    @Test
    @DisplayName("엔티티 빌더 정상 동작 테스트")
    void builder_Success() {
        UUID receiverId = UUID.randomUUID();

        Notification notification = Notification.builder()
            .receiverId(receiverId)
            .title("새 팔로워 알림")
            .content("민준님을 팔로우했습니다.")
            .level(NotificationLevel.INFO)
            .createdAt(Instant.now())
            .build();

        assertThat(notification).isNotNull();
        assertThat(notification.getReceiverId()).isEqualTo(receiverId);
        assertThat(notification.getTitle()).isEqualTo("새 팔로워 알림");
        assertThat(notification.getContent()).contains("팔로우");
        assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getCreatedAt())
            .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));

        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getReadAt()).isNull();
    }

    @Test
    @DisplayName("equals & hashCode 테스트")
    void equalsAndHashCode_Success() {
        UUID id = UUID.randomUUID();

        Notification n1 = Notification.builder()
            .receiverId(UUID.randomUUID())
            .title("테스트 알림 1")
            .content("내용 1")
            .level(NotificationLevel.INFO)
            .createdAt(Instant.now())
            .build();

        Notification n2 = Notification.builder()
            .receiverId(UUID.randomUUID())
            .title("테스트 알림 2")
            .content("내용 2")
            .level(NotificationLevel.INFO)
            .createdAt(Instant.now())
            .build();

        ReflectionTestUtils.setField(n1, "id", id);
        ReflectionTestUtils.setField(n2, "id", id);

        assertThat(n1).isEqualTo(n2);
        assertThat(n1.hashCode()).isEqualTo(n2.hashCode());
    }

    @Test
    @DisplayName("markAsRead() 호출 시 isRead=true, readAt이 현재 시간으로 설정된다")
    void markAsRead_Success() {
        Notification notification = Notification.builder()
            .receiverId(UUID.randomUUID())
            .title("팔로워 알림")
            .content("새 팔로워가 있습니다.")
            .level(NotificationLevel.INFO)
            .createdAt(Instant.now())
            .build();

        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getReadAt()).isNull();

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
        assertThat(notification.getReadAt())
            .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("이미 읽은 알림에 markAsRead() 재호출 시 중복 갱신되지 않는다")
    void markAsRead_AlreadyRead_NoChange() throws InterruptedException {
        Notification notification = Notification.builder()
            .receiverId(UUID.randomUUID())
            .title("테스트 알림")
            .content("이미 읽은 알림입니다.")
            .level(NotificationLevel.INFO)
            .createdAt(Instant.now())
            .build();

        notification.markAsRead();
        Instant firstReadAt = notification.getReadAt();

        Thread.sleep(10);
        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isEqualTo(firstReadAt);
    }
}