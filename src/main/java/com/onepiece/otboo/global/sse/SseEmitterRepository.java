package com.onepiece.otboo.global.sse;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Repository
public class SseEmitterRepository {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(UUID userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
        log.debug("[SseEmitterRepository] Emitter 저장 - userId: {}, 총 연결 수: {}",
            userId, emitters.size());
    }

    public SseEmitter get(UUID userId) {
        return emitters.get(userId);
    }

    public void delete(UUID userId) {
        emitters.remove(userId);
        log.debug("[SseEmitterRepository] Emitter 삭제 - userId: {}, 남은 연결 수: {}",
            userId, emitters.size());
    }

    public int size() {
        return emitters.size();
    }

    public Set<UUID> getAllUserIds() {
        return new HashSet<>(emitters.keySet());
    }
}