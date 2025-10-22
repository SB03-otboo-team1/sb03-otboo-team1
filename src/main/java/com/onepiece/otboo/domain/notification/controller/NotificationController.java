package com.onepiece.otboo.domain.notification.controller;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 알림 관련 API 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 조회 (커서 기반)
     */
    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "커서 기반(createdAt + idAfter)으로 인증된 사용자의 알림 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    public ResponseEntity<CursorPageResponseDto<NotificationResponse>> getNotifications(
        @AuthenticationPrincipal CustomUserDetails principal,
        @Parameter(description = "커서 기준 시각 (ISO-8601 또는 문자열)", example = "2025-10-14T10:15:30Z")
        @RequestParam(required = false)
        String cursor,

        @Parameter(description = "커서 기준 이후의 알림 ID (UUID)", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        @RequestParam(required = false)
        UUID idAfter,

        @Parameter(description = "조회 개수", example = "10")
        @Min(1) @Max(100)
        @RequestParam(defaultValue = "10") int limit
    ) {
        UUID receiverId = principal.getUserId();
        CursorPageResponseDto<NotificationResponse> response =
            notificationService.getNotifications(receiverId, cursor, idAfter, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 알림 읽음 처리
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "알림 삭제(읽음 처리)", description = "특정 알림을 읽음 처리(삭제)합니다.")
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