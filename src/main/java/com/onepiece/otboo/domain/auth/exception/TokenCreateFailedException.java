package com.onepiece.otboo.domain.auth.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class TokenCreateFailedException extends CustomAuthException {

    public TokenCreateFailedException() {
        super(ErrorCode.TOKEN_CREATE_FAILED);
    }
}