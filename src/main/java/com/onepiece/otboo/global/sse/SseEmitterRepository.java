package com.onepiece.otboo.global.sse;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(UUID userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
    }

    public SseEmitter get(UUID userId) {
        return emitters.get(userId);
    }

    public void delete(UUID userId) {
        emitters.remove(userId);
    }
}