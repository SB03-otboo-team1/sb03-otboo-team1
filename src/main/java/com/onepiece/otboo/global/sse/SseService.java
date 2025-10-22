package com.onepiece.otboo.global.sse;

import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private static final long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final SseEmitterRepository emitterRepository;

    /**
     * 클라이언트와 SSE 연결을 생성합니다.
     *
     * @param userId      연결할 사용자 ID
     * @param lastEventId 클라이언트가 마지막으로 수신한 이벤트 ID (재연결 시)
     */
    public SseEmitter connect(UUID userId, String lastEventId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> emitterRepository.delete(userId));
        emitter.onTimeout(() -> emitterRepository.delete(userId));
        emitter.onError((e) -> emitterRepository.delete(userId));

        try {
            emitter.send(SseEmitter.event()
                .id(UUID.randomUUID().toString())
                .name("notifications")
                .data("SSE connected successfully."));
            log.info("[SSE] Connected: {}", userId);
        } catch (IOException e) {
            log.error("[SSE] Initial event send failed for user: {}", userId, e);
        }

        return emitter;
    }

    /**
     * 특정 사용자에게 이벤트를 전송합니다.
     *
     * @param userId    수신자 ID
     * @param eventName 이벤트 이름 (예: "notifications", "directMessages" 등)
     * @param data      전송할 데이터 (DTO, 문자열 등)
     */
    public void send(UUID userId, String eventName, Object data) {
        SseEmitter emitter = emitterRepository.get(userId);
        if (emitter == null) {
            log.warn("[SSE] No active emitter for user: {}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                .id(UUID.randomUUID().toString())
                .name(eventName)
                .data(data));
            log.info("[SSE] Sent event '{}' to user: {}", eventName, userId);
        } catch (IOException e) {
            log.error("[SSE] Send failed, removing emitter for user: {}", userId, e);
            emitterRepository.delete(userId);
            emitter.completeWithError(e);
        }
    }
}