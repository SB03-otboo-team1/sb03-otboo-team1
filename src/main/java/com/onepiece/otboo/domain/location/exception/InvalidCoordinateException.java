package com.onepiece.otboo.domain.location.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;

public class InvalidCoordinateException extends GlobalException {

    public InvalidCoordinateException() {
        super(ErrorCode.INVALID_COORDINATE);
    }
}
