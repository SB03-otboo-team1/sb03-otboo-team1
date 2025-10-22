package com.onepiece.otboo.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.exception.NotificationNotFoundException;
import com.onepiece.otboo.domain.notification.mapper.NotificationMapper;
import com.onepiece.otboo.domain.notification.repository.NotificationRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.sse.SseService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private SseService sseService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("알림 목록 조회 성공 (receiverId + cursor + idAfter 기반)")
    void getNotifications_Success() {
        UUID receiverId = UUID.randomUUID();
        String cursor = "2025-10-15T09:00:00Z";
        UUID idAfter = UUID.randomUUID();
        int limit = 10;

        Notification notification = Notification.builder()
            .receiverId(receiverId)
            .title("테스트 알림")
            .content("테스트 내용")
            .level(Level.INFO)
            .createdAt(Instant.now())
            .build();

        NotificationResponse response = NotificationResponse.builder()
            .id(UUID.randomUUID())
            .receiverId(receiverId)
            .title("테스트 알림")
            .content("테스트 내용")
            .level("INFO")
            .createdAt(notification.getCreatedAt())
            .build();

        given(notificationRepository.findNotifications(any(UUID.class), any(), any(), anyInt()))
            .willReturn(List.of(notification));
        given(notificationMapper.toResponse(notification)).willReturn(response);
        given(notificationRepository.countByReceiverId(any(UUID.class))).willReturn(1L);

        CursorPageResponseDto<NotificationResponse> result =
            notificationService.getNotifications(receiverId, cursor, idAfter, limit);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getTitle()).isEqualTo("테스트 알림");
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalCount()).isEqualTo(1L);
        assertThat(result.sortBy()).isEqualTo(SortBy.CREATED_AT);
        assertThat(result.sortDirection()).isEqualTo(SortDirection.DESCENDING);

        verify(notificationRepository).findNotifications(any(UUID.class), any(), any(), anyInt());
        verify(notificationRepository).countByReceiverId(any(UUID.class));
    }

    @Test
    @DisplayName("알림 삭제(읽음 대체) 성공 - deletedAt 설정 후 저장 호출")
    void deleteNotification_Success() {
        UUID id = UUID.randomUUID();
        Notification notification = Notification.builder()
            .receiverId(UUID.randomUUID())
            .title("삭제 테스트 알림")
            .content("내용")
            .level(Level.INFO)
            .createdAt(Instant.now())
            .build();

        given(notificationRepository.findById(id)).willReturn(Optional.of(notification));

        notificationService.deleteNotification(id);

        assertThat(notification.getDeletedAt()).isNotNull();
        verify(notificationRepository).findById(id);
        verify(notificationRepository, never()).delete(notification);
    }

    @Test
    @DisplayName("알림 삭제 실패 - 존재하지 않는 ID")
    void deleteNotification_Fail_NotFound() {
        UUID invalidId = UUID.randomUUID();
        given(notificationRepository.findById(invalidId)).willReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
            NotificationNotFoundException.class,
            () -> notificationService.deleteNotification(invalidId)
        );

        verify(notificationRepository).findById(invalidId);
        verify(notificationRepository, never()).delete(any(Notification.class));
    }

    @Test
    @DisplayName("이미 삭제된 알림에 delete() 호출 시 중복 저장되지 않음")
    void deleteNotification_AlreadyDeleted_NoUpdate() {
        UUID id = UUID.randomUUID();
        Notification notification = Notification.builder()
            .receiverId(UUID.randomUUID())
            .title("이미 삭제된 알림")
            .content("내용")
            .level(Level.INFO)
            .createdAt(Instant.now())
            .build();

        notification.delete();
        Instant deletedAtBefore = notification.getDeletedAt();

        given(notificationRepository.findById(id)).willReturn(Optional.of(notification));

        notificationService.deleteNotification(id);

        assertThat(notification.getDeletedAt()).isEqualTo(deletedAtBefore);
        verify(notificationRepository).findById(id);
        verify(notificationRepository, never()).delete(notification);
    }


    @Test
    @DisplayName("알림 생성 성공 - 저장 후 SSE 전송 호출")
    void createNotification_Success() {
        UUID receiverId = UUID.randomUUID();
        Set<UUID> receivers = Set.of(receiverId);
        Notification notification = Notification.builder()
            .receiverId(receiverId)
            .title("테스트 알림")
            .content("내용")
            .level(Level.INFO)
            .createdAt(Instant.now())
            .build();

        Notification saved = Notification.builder()
            .receiverId(receiverId)
            .title("테스트 알림")
            .content("내용")
            .level(Level.INFO)
            .createdAt(notification.getCreatedAt())
            .build();

        NotificationResponse response = NotificationResponse.builder()
            .receiverId(receiverId)
            .title("테스트 알림")
            .content("내용")
            .level("INFO")
            .createdAt(saved.getCreatedAt())
            .build();

        given(notificationRepository.save(any(Notification.class))).willReturn(saved);
        given(notificationMapper.toResponse(saved)).willReturn(response);

        notificationService.create(receivers, "테스트 알림", "내용", Level.INFO);

        verify(notificationRepository).save(any(Notification.class));
        verify(notificationMapper).toResponse(saved);
        verify(sseService).send(receiverId, "notifications", response);
    }

    @Test
    @DisplayName("알림 생성 중 SSE 전송 실패해도 예외 발생 없이 처리됨")
    void createNotification_SSEFailure_NoException() {
        UUID receiverId = UUID.randomUUID();
        Set<UUID> receivers = Set.of(receiverId);

        Notification saved = Notification.builder()
            .receiverId(receiverId)
            .title("테스트 알림")
            .content("내용")
            .level(Level.INFO)
            .createdAt(Instant.now())
            .build();

        NotificationResponse response = NotificationResponse.builder()
            .receiverId(receiverId)
            .title("테스트 알림")
            .content("내용")
            .level("INFO")
            .createdAt(saved.getCreatedAt())
            .build();

        given(notificationRepository.save(any(Notification.class))).willReturn(saved);
        given(notificationMapper.toResponse(saved)).willReturn(response);

        org.mockito.Mockito.doThrow(new RuntimeException("SSE 실패"))
            .when(sseService).send(any(UUID.class), any(), any());

        notificationService.create(receivers, "테스트 알림", "내용", Level.INFO);

        verify(notificationRepository).save(any(Notification.class));
        verify(sseService).send(any(UUID.class), any(), any());
    }


}