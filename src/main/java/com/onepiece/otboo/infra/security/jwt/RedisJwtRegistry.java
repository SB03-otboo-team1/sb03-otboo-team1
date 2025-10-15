package com.onepiece.otboo.infra.security.jwt;

import com.onepiece.otboo.infra.security.dto.data.TokenMeta;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * {@link JwtRegistry}의 Redis 기반 구현체입니다.
 *
 * <p>키 스키마</p>
 * <ul>
 *     <li>{@code jwt:token:{jti}} - 토큰 만료 시각과 동일한 TTL로 저장되는 {@link TokenMeta} 객체</li>
 *     <li>{@code jwt:user:{userId}:tokens} - 해당 사용자의 활성 토큰 ID 집합. TTL은 가장 오래 남은 토큰 만료 시각까지 연장됨</li>
 *     <li>{@code jwt:blacklist:{jti}} - 블랙리스트 처리된 토큰의 마커 엔트리. TTL은 원래 토큰 만료와 동일(메타 정보가 없을 경우 1시간 기본값)</li>
 * </ul>
 */
@Component
@Primary
@Profile("!test & !test-integration")
@RequiredArgsConstructor
public class RedisJwtRegistry implements JwtRegistry {

    private static final Duration BLACKLIST_FALLBACK_TTL = Duration.ofHours(1);
    private static final String TOKEN_KEY_PREFIX = "jwt:token:";
    private static final String USER_TOKENS_KEY_PREFIX = "jwt:user:";
    private static final String USER_TOKENS_KEY_SUFFIX = ":tokens";
    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void registerToken(UUID userId, String tokenId, Instant expiresAt) {
        if (userId == null || tokenId == null || expiresAt == null) {
            return;
        }

        Instant now = Instant.now();
        Duration ttl = remainingDuration(now, expiresAt);
        if (ttl == null) {
            return;
        }

        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        valueOps.set(tokenKey(tokenId), new TokenMeta(userId, expiresAt), ttl);

        String userTokensKey = userTokensKey(userId);
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        setOps.add(userTokensKey, tokenId);
        extendUserTokensTtl(userTokensKey, ttl);
    }

    @Override
    public void blacklistAllTokens(UUID userId) {
        if (userId == null) {
            return;
        }

        String userTokensKey = userTokensKey(userId);
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Set<Object> tokenIds = setOps.members(userTokensKey);
        if (tokenIds == null || tokenIds.isEmpty()) {
            redisTemplate.delete(userTokensKey);
            return;
        }

        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        Instant now = Instant.now();
        for (Object tokenObject : tokenIds) {
            if (!(tokenObject instanceof String tokenId)) {
                continue;
            }

            String tokenKey = tokenKey(tokenId);
            TokenMeta tokenMeta = null;
            try {
                Object raw = valueOps.get(tokenKey);
                if (raw instanceof TokenMeta tm) {
                    tokenMeta = tm;
                }
            } catch (Exception ignored) {
            }
            redisTemplate.delete(tokenKey);

            Instant expiresAt =
                tokenMeta != null ? tokenMeta.expiresAt() : now.plus(BLACKLIST_FALLBACK_TTL);
            Duration ttl = Objects.requireNonNullElse(remainingDuration(now, expiresAt),
                BLACKLIST_FALLBACK_TTL);
            valueOps.set(blacklistKey(tokenId), Boolean.TRUE, ttl);
        }

        redisTemplate.delete(userTokensKey);
    }

    @Override
    public boolean isBlacklisted(String tokenId) {
        if (tokenId == null) {
            return false;
        }

        return redisTemplate.hasKey(blacklistKey(tokenId));
    }

    private void extendUserTokensTtl(String userTokensKey, Duration candidateTtl) {
        if (candidateTtl.isZero() || candidateTtl.isNegative()) {
            return;
        }

        long currentTtlSeconds = redisTemplate.getExpire(userTokensKey);
        if (currentTtlSeconds < 0) {
            redisTemplate.expire(userTokensKey, candidateTtl);
            return;
        }

        Duration currentTtl = Duration.ofSeconds(currentTtlSeconds);
        if (candidateTtl.compareTo(currentTtl) > 0) {
            redisTemplate.expire(userTokensKey, candidateTtl);
        }
    }

    private Duration remainingDuration(Instant reference, Instant expiresAt) {
        if (expiresAt == null) {
            return null;
        }

        Duration ttl = Duration.between(reference, expiresAt);
        return ttl.isNegative() ? null : ttl;
    }

    private String tokenKey(String tokenId) {
        return TOKEN_KEY_PREFIX + tokenId;
    }

    private String userTokensKey(UUID userId) {
        return USER_TOKENS_KEY_PREFIX + userId + USER_TOKENS_KEY_SUFFIX;
    }

    private String blacklistKey(String tokenId) {
        return BLACKLIST_KEY_PREFIX + tokenId;
    }
}
