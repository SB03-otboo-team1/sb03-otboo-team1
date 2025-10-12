package com.onepiece.otboo.domain.notification.service;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.exception.NotificationNotFoundException;
import com.onepiece.otboo.domain.notification.repository.NotificationRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.sse.SseService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 서비스 구현체
 * - 알림 생성 (SSE 실시간 전송 포함)
 * - 알림 목록 조회 (커서 기반)
 * - 알림 읽음 처리 (Soft Delete)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher publisher;
    private final SseService sseService;

    /**
     * 알림 생성 + SSE 전송
     */
    @Override
    @Transactional
    public void create(Set<UUID> receiverIds, String title, String content, Level level) {
        if (receiverIds.isEmpty()) {
            log.warn("[NotificationService] 알림 수신자가 없음");
            return;
        }

        log.info("[NotificationService] 알림 생성 시작 - receiverIds: {}", receiverIds);

        receiverIds.forEach(receiverId -> {
            Notification notification = Notification.builder()
                .receiverId(receiverId)
                .title(title)
                .content(content)
                .level(level)
                .build();

            notificationRepository.save(notification);
            log.info("[NotificationService] 알림 저장 완료 - receiverId: {}", receiverId);

            NotificationResponse response = NotificationResponse.builder()
                .id(notification.getId())
                .receiverId(notification.getReceiverId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .level(notification.getLevel())
                .createdAt(notification.getCreatedAt())
                .build();

            sseService.send(receiverId, response);
            log.info("[NotificationService] SSE 전송 완료 - receiverId: {}", receiverId);
        });
    }

    /**
     * 알림 목록 조회 (커서 기반)
     */
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseDto<NotificationResponse> getNotifications(
        UUID receiverId,
        UUID idAfter,
        int limit
    ) {
        log.info("[NotificationService] 알림 목록 조회 시작 - receiverId: {}", receiverId);

        List<Notification> notifications =
            notificationRepository.findNotifications(receiverId, idAfter, limit + 1);

        boolean hasNext = notifications.size() > limit;
        if (hasNext) {
            notifications = notifications.subList(0, limit);
        }

        UUID nextCursor = hasNext
            ? notifications.get(notifications.size() - 1).getId()
            : null;

        long totalCount = notificationRepository.countByReceiverId(receiverId);

        List<NotificationResponse> data = notifications.stream()
            .map(n -> NotificationResponse.builder()
                .id(n.getId())
                .receiverId(n.getReceiverId())
                .title(n.getTitle())
                .content(n.getContent())
                .level(n.getLevel())
                .createdAt(n.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        return new CursorPageResponseDto<>(
            data,
            nextCursor != null ? nextCursor.toString() : null,
            null,
            hasNext,
            totalCount,
            SortBy.CREATED_AT,
            SortDirection.DESCENDING
        );
    }

    /**
     * 알림 읽음 처리 (Soft Delete)
     */
    @Override
    @Transactional
    public void deleteNotification(UUID id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NotificationNotFoundException(id));

        notification.delete();
        log.info("[NotificationService] 알림 읽음 처리 완료 - id: {}", id);
    }
}