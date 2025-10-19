package com.onepiece.otboo.infra.redis.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;

public class RedisException extends GlobalException {

    public RedisException(ErrorCode errorCode) {
        super(errorCode);
    }

    public RedisException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
