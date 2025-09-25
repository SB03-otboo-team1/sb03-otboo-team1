package com.onepiece.otboo.domain.profile.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.Map;

public class ProfileException extends GlobalException {

    public ProfileException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ProfileException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
