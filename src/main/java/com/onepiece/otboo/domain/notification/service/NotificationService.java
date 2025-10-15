package com.onepiece.otboo.domain.notification.service;


import com.onepiece.otboo.domain.notification.enums.Level;
import java.util.Set;
import java.util.UUID;

public interface NotificationService {
    CursorPageResponseDto<NotificationResponse> getNotifications(UUID receiverId, UUID idAfter,
        int limit);

  void create(Set<UUID> userId, String title, String content, Level level);
}
