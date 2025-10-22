package com.onepiece.otboo.domain.clothes.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;

public class ParsingPageForbiddenException extends ClothesException {

    public ParsingPageForbiddenException(String message) {
        super(ErrorCode.PARSING_PAGE_ACCESS_BLOCKED, Map.of("message", message));
    }

    public ParsingPageForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ParsingPageForbiddenException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

}
