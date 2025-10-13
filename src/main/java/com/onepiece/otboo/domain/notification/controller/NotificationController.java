package com.onepiece.otboo.domain.notification.controller;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "인증된 사용자의 알림 목록을 커서 기반으로 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효성 검증 실패")
    })
    public ResponseEntity<CursorPageResponseDto<NotificationResponse>> getNotifications(
        @AuthenticationPrincipal UserDetails userDetails,
        @Parameter(description = "다음 조회 기준 ID")
        @RequestParam(required = false) UUID idAfter,
        @Parameter(description = "조회 개수", example = "10")
        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        UUID receiverId = extractUserIdFrom(userDetails);
        var response = notificationService.getNotifications(receiverId, idAfter, limit);
        return ResponseEntity.ok(response);
    }

    private UUID extractUserIdFrom(UserDetails userDetails) {
        return UUID.fromString(userDetails.getUsername());
    }
}