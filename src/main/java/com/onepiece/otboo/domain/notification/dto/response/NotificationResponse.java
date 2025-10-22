package com.onepiece.otboo.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * 알림 정보 DTO
 */
@Getter
@Builder
@Schema(description = "알림 정보 DTO")
public class NotificationResponse {

    @Schema(description = "알림 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "생성 시각", example = "2025-10-12T17:49:06.001Z")
    private Instant createdAt;

    @Schema(description = "수신자 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID receiverId;

    @Schema(description = "알림 제목", example = "새로운 팔로워가 있습니다.")
    private String title;

    @Schema(description = "알림 내용", example = "민준님을 팔로우하기 시작했습니다.")
    private String content;

    @Schema(description = "알림 레벨", example = "INFO")
    private String level;
}