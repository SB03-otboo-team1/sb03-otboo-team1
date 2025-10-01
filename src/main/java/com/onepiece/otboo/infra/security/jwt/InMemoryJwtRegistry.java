package com.onepiece.otboo.infra.security.jwt;

import com.onepiece.otboo.infra.security.dto.data.TokenMeta;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class InMemoryJwtRegistry implements JwtRegistry {

    private static final long ONE_HOUR_SECONDS = 3600L;

    // 활성 토큰 저장
    private final Map<String, TokenMeta> activeTokens = new ConcurrentHashMap<>();
    // 사용자별 활성 토큰 목록
    private final Map<UUID, Set<String>> userTokens = new ConcurrentHashMap<>();
    // 블랙리스트
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    @Override
    public void registerToken(UUID userId, String tokenId, Instant expiresAt) {
        if (userId == null || tokenId == null || expiresAt == null) {
            return;
        }
        cleanup();
        activeTokens.put(tokenId, new TokenMeta(userId, expiresAt));
        userTokens.computeIfAbsent(userId, k -> new ConcurrentSkipListSet<>()).add(tokenId);
    }

    @Override
    public void blacklistAllTokens(UUID userId) {
        if (userId == null) {
            return;
        }
        cleanup();
        Set<String> tokens = userTokens.remove(userId);
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        for (String jti : tokens) {
            TokenMeta meta = activeTokens.remove(jti);
            Instant exp =
                meta != null ? meta.expiresAt() : Instant.now().plusSeconds(ONE_HOUR_SECONDS);
            blacklistedTokens.put(jti, exp);
        }
    }

    @Override
    public boolean isBlacklisted(String tokenId) {
        if (tokenId == null) {
            return false;
        }
        Instant exp = blacklistedTokens.get(tokenId);
        if (exp == null) {
            return false;
        }
        if (exp.isBefore(Instant.now())) {
            blacklistedTokens.remove(tokenId);
            return false;
        }
        return true;
    }

    private void cleanup() {
        Instant now = Instant.now();
        // 블랙리스트 정리
        blacklistedTokens.entrySet().removeIf(e -> e.getValue().isBefore(now));
        // 만료된 활성 토큰 정리
        activeTokens.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
        // 사용자별 목록에서 유효하지 않은 토큰 제거
        userTokens.entrySet().removeIf(entry -> {
            UUID userId = entry.getKey();
            Set<String> set = entry.getValue();
            set.removeIf(jti -> !activeTokens.containsKey(jti));
            if (set.isEmpty()) {
                userTokens.remove(userId);
                return true;
            }
            return false;
        });
    }
}
