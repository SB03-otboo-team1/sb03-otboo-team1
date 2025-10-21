package com.onepiece.otboo.domain.notification.dto.response;

import com.onepiece.otboo.domain.notification.enums.Level;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID receiverId;
    private String title;
    private String content;
    private Level level;
    private Instant createdAt;
}