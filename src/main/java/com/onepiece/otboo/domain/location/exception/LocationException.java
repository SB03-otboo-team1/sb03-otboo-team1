package com.onepiece.otboo.domain.location.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.Map;

public class LocationException extends GlobalException {

    public LocationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public LocationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
