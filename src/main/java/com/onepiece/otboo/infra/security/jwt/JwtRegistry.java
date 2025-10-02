package com.onepiece.otboo.infra.security.jwt;

import java.time.Instant;
import java.util.UUID;

public interface JwtRegistry {

    void registerToken(UUID userId, String tokenId, Instant expiresAt);

    void blacklistAllTokens(UUID userId);

    boolean isBlacklisted(String tokenId);
}
