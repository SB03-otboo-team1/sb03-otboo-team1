package com.onepiece.otboo.domain.notification.service;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.exception.NotificationNotFoundException;
import com.onepiece.otboo.domain.notification.mapper.NotificationMapper;
import com.onepiece.otboo.domain.notification.repository.NotificationRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 서비스 구현체
 * <p>
 * - 알림 목록 조회 (커서 기반) - 알림 생성 - 알림 읽음 처리
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    /**
     * 알림 목록 조회 (receiverId + cursor + idAfter 기반)
     */
    @Transactional(readOnly = true)
    @Override
    public CursorPageResponseDto<NotificationResponse> getNotifications(
        UUID receiverId,
        String cursor,
        UUID idAfter,
        int limit
    ) {
        Instant cursorInstant = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                cursorInstant = Instant.parse(cursor);
            } catch (Exception ignored) {
                throw new IllegalArgumentException("잘못된 cursor 형식입니다. ISO-8601 형식이어야 합니다.");
            }
        }

        List<Notification> notifications =
            notificationRepository.findNotifications(receiverId, cursorInstant, idAfter, limit + 1);

        boolean hasNext = notifications.size() > limit;
        if (hasNext) {
            notifications = notifications.subList(0, limit);
        }

        Instant nextCursor = hasNext
            ? notifications.get(notifications.size() - 1).getCreatedAt()
            : null;
        UUID nextIdAfter = hasNext
            ? notifications.get(notifications.size() - 1).getId()
            : null;

        long totalCount = notificationRepository.countByReceiverId(receiverId);

        List<NotificationResponse> data = notifications.stream()
            .map(notificationMapper::toResponse)
            .collect(Collectors.toList());

        return new CursorPageResponseDto<>(
            data,
            nextCursor != null ? nextCursor.toString() : null,
            nextIdAfter,
            hasNext,
            totalCount,
            SortBy.CREATED_AT,
            SortDirection.DESCENDING
        );
    }

    /**
     * 알림 생성
     */
    @Transactional
    @Override
    public void create(Set<UUID> receiverIds, String title, String content, Level level) {
        receiverIds.forEach(receiverId -> {
            Notification notification = Notification.builder()
                .receiverId(receiverId)
                .title(title)
                .content(content)
                .level(level)
                .createdAt(Instant.now())
                .build();

            notificationRepository.save(notification);
        });
    }

    /**
     * 알림 읽음 처리 (삭제 대체)
     */
    @Transactional
    @Override
    public void deleteNotification(UUID id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NotificationNotFoundException(id));

        notification.delete();
    }
}