package com.onepiece.otboo.domain.notification.service;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.UUID;

public interface NotificationService {

    CursorPageResponseDto<NotificationResponse> getNotifications(
        String cursor,
        UUID idAfter,
        int limit
    );

    void create(UUID receiverId, String title, String content, Level level);

    void deleteNotification(UUID id);
}