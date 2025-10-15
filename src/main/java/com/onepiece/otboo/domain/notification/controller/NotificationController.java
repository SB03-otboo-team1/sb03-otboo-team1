package com.onepiece.otboo.domain.notification.controller;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 알림 관련 API 컨트롤러
 * <p>
 * - 알림 목록 조회 (GET /api/notifications) - 알림 삭제 (DELETE /api/notifications/{id})
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 조회
     */
    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 커서 기반(createdAt)으로 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    public ResponseEntity<CursorPageResponseDto<NotificationResponse>> getNotifications(
        @Parameter(description = "수신자 ID", required = true)
        @RequestParam UUID receiverId,

        @Parameter(description = "다음 조회 기준 시각 (ISO-8601 형식)", example = "2025-10-14T10:15:30Z")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant createdAtBefore,

        @Parameter(description = "조회 개수", example = "10")
        @RequestParam(defaultValue = "10")
        int limit
    ) {
        CursorPageResponseDto<NotificationResponse> response =
            notificationService.getNotifications(receiverId, createdAtBefore, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 알림 읽음 처리
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다. (읽음 처리 대체)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "알림 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 알림 ID")
    })
    public ResponseEntity<Void> deleteNotification(
        @Parameter(description = "알림 ID", required = true)
        @PathVariable UUID id
    ) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}