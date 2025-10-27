package com.onepiece.otboo.domain.auth.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class TokenForgedException extends CustomAuthException {

    public TokenForgedException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public TokenForgedException(String message) {
        super(ErrorCode.UNAUTHORIZED);
    }
}