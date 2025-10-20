package com.onepiece.otboo.domain.notification.service;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.repository.NotificationRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    private final ApplicationEventPublisher publisher;

    /**
     * 알림 생성
     */
    @Override
    @Transactional
    public void create(Set<UUID> receiverIds, String title, String content, Level level) {

        if (receiverIds.isEmpty()) {
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

    /**
     * 알림 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(UUID receiverId) {
        log.info("[NotificationService] 알림 목록 조회 시작 - receiverId: {}", receiverId);

        List<Notification> notifications =
            notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId);

        List<NotificationResponse> responses = notifications.stream()
            .map(n -> NotificationResponse.builder()
                .id(n.getId())
                .receiverId(n.getReceiverId())
                .title(n.getTitle())
                .content(n.getContent())
                .level(n.getLevel())
                .createdAt(n.getCreatedAt())
                .build())
            .toList();

        log.info("[NotificationService] 알림 {}개 조회 완료", responses.size());
        return responses;
    }
}