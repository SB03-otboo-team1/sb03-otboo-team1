package com.onepiece.otboo.infra.redis.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class RedisLockAcquisitionException extends RedisException {

    public RedisLockAcquisitionException() {
        super(ErrorCode.REDIS_LOCK_ACQUISITION_FAILED);
    }
}
