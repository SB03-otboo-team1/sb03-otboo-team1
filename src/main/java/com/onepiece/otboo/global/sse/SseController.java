package com.onepiece.otboo.global.sse;

import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    /**
     * 클라이언트가 SSE 연결을 생성하는 엔드포인트 예시: GET /api/sse
     */
    @GetMapping(value = "/api/sse", produces = "text/event-stream")
    public SseEmitter connect(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
    ) {
        UUID userId = extractUserIdFrom(userDetails);
        return sseService.connect(userId, lastEventId);
    }

    /**
     * 인증 정보에서 userId 추출
     */
    private UUID extractUserIdFrom(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUserId();
        }
        throw new IllegalArgumentException("인증 정보에서 userId를 가져올 수 없습니다.");
    }
}