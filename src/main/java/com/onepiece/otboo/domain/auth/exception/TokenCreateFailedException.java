package com.onepiece.otboo.domain.auth.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class TokenCreateFailedException extends CustomAuthException {

    public TokenCreateFailedException() {
        super(ErrorCode.TOKEN_CREATE_FAILED);
    }

    public TokenCreateFailedException(Throwable cause) {
        super(ErrorCode.TOKEN_CREATE_FAILED, cause);
    }
}