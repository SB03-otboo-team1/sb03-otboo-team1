package com.onepiece.otboo.domain.notification.repository;

import com.onepiece.otboo.domain.notification.entity.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationRepositoryCustom {

    List<Notification> findNotifications(UUID receiverId, UUID idAfter, int limit);

    long countByReceiverId(UUID receiverId);
}
