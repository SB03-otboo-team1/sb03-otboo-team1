package com.onepiece.otboo.infra.security.lock;

import com.onepiece.otboo.infra.redis.RedisLockProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("prod")
@Component
@RequiredArgsConstructor
public class RedisAdminAccountLock implements AdminAccountLock {

    private final RedisLockProvider redisLockProvider;

    @Override
    public void acquireLock(String key) {
        redisLockProvider.acquireLock(key);
    }

    @Override
    public void releaseLock(String key) {
        redisLockProvider.releaseLock(key);
    }
}