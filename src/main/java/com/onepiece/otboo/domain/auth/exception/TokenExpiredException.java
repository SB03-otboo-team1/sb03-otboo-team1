package com.onepiece.otboo.domain.auth.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class TokenExpiredException extends CustomAuthException {

    public TokenExpiredException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public TokenExpiredException(String message) {
        super(ErrorCode.UNAUTHORIZED);
    }
}