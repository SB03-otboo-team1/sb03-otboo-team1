package com.onepiece.otboo.infra.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtRegistryTest {

    private InMemoryJwtRegistry registry;
    private UUID userId;
    private String tokenId;
    private Instant expiresAt;

    @BeforeEach
    void setUp() {
        registry = new InMemoryJwtRegistry();
        userId = UUID.randomUUID();
        tokenId = UUID.randomUUID().toString();
        expiresAt = Instant.now().plusSeconds(3600);
    }

    @Test
    void 토큰_등록_및_블랙리스트_아님_확인() {
        registry.registerToken(userId, tokenId, expiresAt);

        assertThat(registry.isBlacklisted(tokenId)).isFalse();
    }

    @Test
    void 블랙리스트_등록_후_확인() {
        registry.registerToken(userId, tokenId, expiresAt);
        registry.blacklistAllTokens(userId);

        assertThat(registry.isBlacklisted(tokenId)).isTrue();
    }

    @Test
    void 블랙리스트_만료_후_false_반환() {
        Instant expired = Instant.now().minusSeconds(1);
        registry.registerToken(userId, tokenId, expired);
        registry.blacklistAllTokens(userId);

        assertThat(registry.isBlacklisted(tokenId)).isFalse();
    }

    @Test
    void 잘못된_입력_무시() {
        registry.registerToken(null, null, null);
        registry.blacklistAllTokens(null);

        assertThat(registry.isBlacklisted(null)).isFalse();
    }
}
