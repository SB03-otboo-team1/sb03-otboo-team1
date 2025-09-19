package com.onepiece.otboo.infra.security.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class SecurityUnauthorizedException extends SecurityException {

    public SecurityUnauthorizedException(Throwable cause) {
        super(ErrorCode.UNAUTHORIZED, cause);
    }

    public SecurityUnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
}