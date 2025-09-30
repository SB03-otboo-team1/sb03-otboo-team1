package com.onepiece.otboo.domain.dm.controller;

import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageResponse;
import com.onepiece.otboo.domain.dm.service.DirectMessageService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    /**
     * DM 생성
     */
    @PostMapping
    public ResponseEntity<DirectMessageResponse> createDirectMessage(
        @RequestBody DirectMessageRequest request
    ) {
        return ResponseEntity.ok(directMessageService.createDirectMessage(request));
    }

    /**
     * DM 목록 조회 (커서 기반 페이징)
     */
    @GetMapping
    public ResponseEntity<List<DirectMessageResponse>> getDirectMessages(
        @RequestParam UUID userId,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
            directMessageService.getDirectMessages(userId, cursor, idAfter, limit)
        );
    }
}