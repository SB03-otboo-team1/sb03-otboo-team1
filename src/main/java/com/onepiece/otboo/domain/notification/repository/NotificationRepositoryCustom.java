package com.onepiece.otboo.domain.notification.repository;

import com.onepiece.otboo.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepositoryCustom {

    /**
     * 알림 목록 조회 (커서 기반: createdAt + idAfter)
     *
     * @param cursor  커서 기준 시각 (createdAt)
     * @param idAfter 동일 시각 내 정렬 기준 ID
     * @param limit   조회 개수
     * @return 알림 목록
     */
    List<Notification> findNotifications(UUID receiverId, Instant cursor, UUID idAfter, int limit);

    /**
     * 전체 알림 개수 (receiverId 기준)
     */
    long countByReceiverId(UUID receiverId);

    long countAll();
}