package com.onepiece.otboo.domain.notification.service;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.exception.NotificationNotFoundException;
import com.onepiece.otboo.domain.notification.mapper.NotificationMapper;
import com.onepiece.otboo.domain.notification.repository.NotificationRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
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
    public CursorPageResponseDto<NotificationResponse> getNotifications(UUID receiverId,
        UUID idAfter, int limit) {

        List<Notification> notifications = notificationRepository.findNotifications(
            receiverId, idAfter, limit);

        List<NotificationResponse> data = notifications.stream()
            .limit(limit)
            .map(notificationMapper::toResponse)
            .collect(Collectors.toList());

        boolean hasNext = notifications.size() > limit;
        UUID nextIdAfter = hasNext ? data.get(data.size() - 1).getId() : null;
        long totalCount = notificationRepository.countByReceiverId(receiverId);

        return new CursorPageResponseDto<>(
            data,
            "cursor123",
            nextIdAfter,
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
    public void markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NotificationNotFoundException(id));

        if (!notification.isRead()) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }
}