package com.onepiece.otboo.domain.auth.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.Map;

public class CustomAuthException extends GlobalException {

    public CustomAuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CustomAuthException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public CustomAuthException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
