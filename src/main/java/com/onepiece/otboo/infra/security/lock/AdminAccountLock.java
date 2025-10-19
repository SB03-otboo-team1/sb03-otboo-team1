package com.onepiece.otboo.infra.security.lock;

public interface AdminAccountLock {

    void acquireLock(String key);

    void releaseLock(String key);
}