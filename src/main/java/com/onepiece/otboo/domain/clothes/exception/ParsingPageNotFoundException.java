package com.onepiece.otboo.domain.clothes.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;

public class ParsingPageNotFoundException extends ClothesException {

    public ParsingPageNotFoundException(String message) {
        super(ErrorCode.PARSING_PAGE_NOT_FOUND, Map.of("message", message));
    }

    public ParsingPageNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ParsingPageNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

}
