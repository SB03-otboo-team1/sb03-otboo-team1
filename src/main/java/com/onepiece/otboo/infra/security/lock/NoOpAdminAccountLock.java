package com.onepiece.otboo.infra.security.lock;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!prod")
@Component
public class NoOpAdminAccountLock implements AdminAccountLock {

    @Override
    public void acquireLock(String key) {
    }

    @Override
    public void releaseLock(String key) {
    }
}