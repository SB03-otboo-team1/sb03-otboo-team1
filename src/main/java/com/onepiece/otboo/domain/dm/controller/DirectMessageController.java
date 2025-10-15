package com.onepiece.otboo.domain.dm.controller;

import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.dm.service.DirectMessageService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    /**
     * DM 목록 조회 (커서 기반 페이징)
     */
    @GetMapping
    public ResponseEntity<CursorPageResponseDto<DirectMessageDto>> getDirectMessages(
        @RequestParam UUID userId,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "10") int limit
    ) {
        CursorPageResponseDto<DirectMessageDto> result = directMessageService.getDirectMessages(
            userId, cursor, idAfter, limit);
        return ResponseEntity.ok(result);
    }
}