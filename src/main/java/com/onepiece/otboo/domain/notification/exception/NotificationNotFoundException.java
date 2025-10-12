package com.onepiece.otboo.domain.notification.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.Map;
import java.util.UUID;

/**
 * 존재하지 않는 알림을 조회할 때 발생하는 예외
 */
public class NotificationNotFoundException extends GlobalException {

    public NotificationNotFoundException(UUID id) {
        super(
            ErrorCode.NOTIFICATION_NOT_FOUND,
            Map.of("notificationId", id != null ? id.toString() : "unknown")
        );
    }
}