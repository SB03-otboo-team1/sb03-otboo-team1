package com.onepiece.otboo.infra.security.jwt;

import java.time.Instant;
import java.util.UUID;

public interface JwtRegistry {

    void invalidateAllTokens(UUID userId, Instant invalidatedAt);

    Instant getInvalidatedAt(UUID userId);
}
