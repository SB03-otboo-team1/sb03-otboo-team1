package com.onepiece.otboo.domain.user.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;

public class DuplicateEmailException extends UserException {

    public DuplicateEmailException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
