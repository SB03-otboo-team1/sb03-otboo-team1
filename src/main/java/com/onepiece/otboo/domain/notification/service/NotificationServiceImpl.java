package com.onepiece.otboo.domain.notification.service;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.exception.NotificationNotFoundException;
import com.onepiece.otboo.domain.notification.mapper.NotificationMapper;
import com.onepiece.otboo.domain.notification.repository.NotificationRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 서비스 구현체
 * <p>
 * - 알림 목록 조회 - 알림 읽음 처리
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    /**
     * 알림 목록 조회
     */
    @Transactional(readOnly = true)
    @Override
    public CursorPageResponseDto<NotificationResponse> getNotifications(
        UUID receiverId,
        Instant createdAtBefore,
        int limit
    ) {
        List<Notification> notifications = notificationRepository.findNotifications(
            receiverId, createdAtBefore, limit);

        boolean hasNext = notifications.size() > limit;
        if (hasNext) {
            notifications = notifications.subList(0, limit);
        }

        Instant nextCursor = hasNext
            ? notifications.get(notifications.size() - 1).getCreatedAt()
            : null;

        long totalCount = notificationRepository.countByReceiverId(receiverId);

        List<NotificationResponse> data = notifications.stream()
            .map(notificationMapper::toResponse)
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
     * 알림 읽음 처리
     */
    @Transactional
    @Override
    public void deleteNotification(UUID id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NotificationNotFoundException(id));

        notification.delete();

    }
}