package com.onepiece.otboo.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.enums.NotificationLevel;
import com.onepiece.otboo.domain.notification.mapper.NotificationMapper;
import com.onepiece.otboo.domain.notification.repository.NotificationRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.List;
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

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("알림 목록 조회 성공")
    void getNotifications_Success() {
        // given
        UUID receiverId = UUID.randomUUID();
        UUID idAfter = null;
        int limit = 10;

        Notification notification = Notification.builder()
            .receiverId(receiverId)
            .title("테스트 알림")
            .content("테스트 내용")
            .level(NotificationLevel.INFO)
            .build();

        NotificationResponse response = NotificationResponse.builder()
            .id(notification.getId())
            .receiverId(receiverId)
            .title("테스트 알림")
            .content("테스트 내용")
            .level("INFO")
            .createdAt(notification.getCreatedAt())
            .build();

        given(notificationRepository.findNotifications(receiverId, idAfter, limit))
            .willReturn(List.of(notification));
        given(notificationMapper.toResponse(notification)).willReturn(response);
        given(notificationRepository.countByReceiverId(receiverId)).willReturn(1L);

        CursorPageResponseDto<NotificationResponse> result =
            notificationService.getNotifications(receiverId, idAfter, limit);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getTitle()).isEqualTo("테스트 알림");
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalCount()).isEqualTo(1L);

        verify(notificationRepository).findNotifications(receiverId, idAfter, limit);
        verify(notificationRepository).countByReceiverId(receiverId);
    }
}