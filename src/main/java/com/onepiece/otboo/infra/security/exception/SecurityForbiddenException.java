package com.onepiece.otboo.infra.security.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class SecurityForbiddenException extends SecurityException {

    public SecurityForbiddenException(Throwable cause) {
        super(ErrorCode.FORBIDDEN, cause);
    }

    public SecurityForbiddenException() {
        super(ErrorCode.FORBIDDEN);
    }
}