package com.onepiece.otboo.domain.notification.service;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.Set;
import java.util.UUID;

public interface NotificationService {

    void create(Set<UUID> userId, String title, String content, Level level);

    CursorPageResponseDto<NotificationResponse> getNotifications(
        UUID receiverId,
        UUID idAfter,
        int limit
    );

    void deleteNotification(UUID id);
}