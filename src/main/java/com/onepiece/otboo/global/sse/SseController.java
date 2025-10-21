package com.onepiece.otboo.global.sse;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    /**
     * 클라이언트가 SSE 연결을 생성하는 엔드포인트 예시: GET /api/sse?userId={UUID}&lastEventId={eventId}
     */
    @GetMapping(value = "/api/sse", produces = "text/event-stream")
    public SseEmitter connect(
        @RequestParam UUID userId,
        @RequestParam(required = false, value = "LastEventId") String lastEventId
    ) {
        return sseService.connect(userId, lastEventId);
    }
}