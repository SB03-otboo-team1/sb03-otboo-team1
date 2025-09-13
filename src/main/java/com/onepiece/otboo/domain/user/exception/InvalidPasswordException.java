package com.onepiece.otboo.domain.user.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class InvalidPasswordException extends UserException {

    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }
}
