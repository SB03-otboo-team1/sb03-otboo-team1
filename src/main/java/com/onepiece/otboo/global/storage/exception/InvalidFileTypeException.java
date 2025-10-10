package com.onepiece.otboo.global.storage.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.Map;
import java.util.Optional;

public class InvalidFileTypeException extends GlobalException {

    public InvalidFileTypeException(String contentType) {
        super(ErrorCode.INVALID_FILE_TYPE,
            Map.of("type", Optional.ofNullable(contentType).orElse("unknown")));
    }
}
