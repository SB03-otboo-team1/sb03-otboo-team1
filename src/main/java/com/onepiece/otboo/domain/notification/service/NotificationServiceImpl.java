package com.onepiece.otboo.domain.notification.service;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.mapper.NotificationMapper;
import com.onepiece.otboo.domain.notification.repository.NotificationRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    @Override
    public CursorPageResponseDto<NotificationResponse> getNotifications(UUID receiverId,
        UUID idAfter, int limit) {

        List<Notification> notifications = notificationRepository.findNotifications(
            receiverId, idAfter, limit + 1);

        boolean hasNext = notifications.size() > limit;
        if (hasNext) {
            notifications = notifications.subList(0, limit);
        }

        List<NotificationResponse> data = notifications.stream()
            .map(notificationMapper::toResponse)
            .collect(Collectors.toList());

        UUID nextIdAfter = hasNext && !data.isEmpty()
            ? data.get(data.size() - 1).getId()
            : null;

        long totalCount = notificationRepository.countByReceiverId(receiverId);

        return new CursorPageResponseDto<>(
            data,
            null,
            nextIdAfter,
            hasNext,
            totalCount,
            SortBy.CREATED_AT,
            SortDirection.DESCENDING
        );
    }

    @Override
    @Transactional
    public void create(Set<UUID> receiverIds, String title, String content, Level level) {

        if (receiverIds == null || receiverIds.isEmpty()) {
            log.warn("[NotificationService] 알림 수신자가 없음");
            return;
        }
        log.info("[NotificationService] 알림 생성 시작 - receiverIds: {}", receiverIds);

        List<Notification> notifications = receiverIds.stream()
            .map(receiverId -> Notification.builder()
                .receiverId(receiverId)
                .title(title)
                .content(content)
                .level(level)
                .build())
            .toList();

        notificationRepository.saveAll(notifications);
        log.info("[NotificationService] 알림 {}개 생성 완료", notifications.size());
    }
}