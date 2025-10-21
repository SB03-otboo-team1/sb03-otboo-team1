package com.onepiece.otboo.domain.notification.controller;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{receiverId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
        @PathVariable UUID receiverId
    ) {
        List<NotificationResponse> notifications = notificationService.getNotifications(receiverId);
        return ResponseEntity.ok(notifications);
    }
}