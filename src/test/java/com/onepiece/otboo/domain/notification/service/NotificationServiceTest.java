package com.onepiece.otboo.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.repository.NotificationRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("알림 목록 조회 성공 (커서 기반)")
    void getNotifications_Success() {
        UUID receiverId = UUID.randomUUID();

        Notification notification = Notification.builder()
            .receiverId(receiverId)
            .title("새 메시지가 도착했습니다")
            .content("홍길동님이 메시지를 보냈습니다.")
            .level(Level.INFO)
            .build();

        when(notificationRepository.findNotifications(receiverId, null, 11))
            .thenReturn(List.of(notification));

        when(notificationRepository.countByReceiverId(receiverId))
            .thenReturn(1L);

        var result = notificationService.getNotifications(receiverId, null, 10);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getTitle()).isEqualTo("새 메시지가 도착했습니다");
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("알림 생성 성공 - 수신자 여러 명")
    void createNotifications_Success() {
        Set<UUID> receiverIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
        String title = "새 알림";
        String content = "여러 수신자에게 알림 발송";
        Level level = Level.WARNING;

        notificationService.create(receiverIds, title, content, level);

        verify(notificationRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("알림 생성 실패 - 수신자 없음")
    void createNotifications_EmptyReceivers() {
        Set<UUID> receiverIds = Set.of();

        notificationService.create(receiverIds, "제목", "내용", Level.ERROR);

        verify(notificationRepository, never()).saveAll(any());
    }
}