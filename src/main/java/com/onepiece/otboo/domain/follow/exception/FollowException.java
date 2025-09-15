package com.onepiece.otboo.domain.follow.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.Map;

public class FollowException extends GlobalException {

    public FollowException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FollowException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
