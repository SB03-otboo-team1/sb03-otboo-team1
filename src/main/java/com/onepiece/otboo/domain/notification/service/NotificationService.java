package com.onepiece.otboo.domain.notification.service;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.UUID;

public interface NotificationService {

    CursorPageResponseDto<NotificationResponse> getNotifications(UUID receiverId, UUID idAfter,
        int limit);

    void markAsRead(UUID id);

}