package com.onepiece.otboo.infra.security.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryJwtRegistry implements JwtRegistry {

    // 사용자별 마지막 무효화 시각 저장
    private final Map<UUID, Instant> invalidatedAtMap = new ConcurrentHashMap<>();

    @Override
    public void invalidateAllTokens(UUID userId, Instant invalidatedAt) {
        invalidatedAtMap.put(userId, invalidatedAt);
    }

    @Override
    public Instant getInvalidatedAt(UUID userId) {
        return invalidatedAtMap.get(userId);
    }
}
