package com.onepiece.otboo.infra.security.jwt;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryJwtRegistry implements JwtRegistry {

    private final Map<UUID, Object> invalidatedUsers = new ConcurrentHashMap<>();

    @Override
    public void invalidateAllTokens(UUID userId) {
        invalidatedUsers.put(userId, new Object());
    }
}
