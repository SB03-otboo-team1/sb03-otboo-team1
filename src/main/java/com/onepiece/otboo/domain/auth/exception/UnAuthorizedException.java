package com.onepiece.otboo.domain.auth.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class UnAuthorizedException extends CustomAuthException {

    public UnAuthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }

}
