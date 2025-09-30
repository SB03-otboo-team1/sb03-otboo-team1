package com.onepiece.otboo.domain.profile.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.Map;

public class InvalidFileTypeException extends GlobalException {

    public InvalidFileTypeException(String contentType) {
        super(ErrorCode.INVALID_FILE_TYPE,
            Map.of("type", contentType));
    }
}
