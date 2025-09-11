package com.onepiece.otboo.domain.user.exception;

import com.onepiece.otboo.global.exception.CustomException;
import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;

public class UserException extends CustomException {

    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
