package com.onepiece.otboo.infra.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onepiece.otboo.infra.security.dto.data.TokenMeta;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisJwtRegistryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOps;
    @Mock
    private SetOperations<String, Object> setOps;
    @InjectMocks
    private RedisJwtRegistry registry;

    private UUID userId;
    private String tokenId;
    private Instant expiresAt;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tokenId = UUID.randomUUID().toString();
        expiresAt = Instant.now().plusSeconds(3600);
    }

    @Test
    void 토큰_등록시_TTL_키_값_정상저장() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        registry.registerToken(userId, tokenId, expiresAt);

        verify(valueOps).set(startsWith("jwt:token:"), any(TokenMeta.class), any(Duration.class));
        verify(setOps).add(startsWith("jwt:user:"), eq(tokenId));
    }

    @Test
    void 모든_토큰_블랙리스트_TTL_삭제_검증() {
        String userTokensKey = "jwt:user:" + userId + ":tokens";
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.members(userTokensKey)).thenReturn(Set.of(tokenId));
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        TokenMeta meta = new TokenMeta(userId, expiresAt);
        when(valueOps.get("jwt:token:" + tokenId)).thenReturn(meta);

        registry.blacklistAllTokens(userId);

        verify(valueOps).set(startsWith("jwt:blacklist:"), eq(Boolean.TRUE), any(Duration.class));
        verify(redisTemplate).delete("jwt:token:" + tokenId);
        verify(redisTemplate).delete(userTokensKey);
    }

    @Test
    void 블랙리스트_여부_true_false_반환() {
        String blacklistKey = "jwt:blacklist:" + tokenId;
        when(redisTemplate.hasKey(blacklistKey)).thenReturn(true, false);

        assertThat(registry.isBlacklisted(tokenId)).isTrue();
        assertThat(registry.isBlacklisted(tokenId)).isFalse();
    }
}
