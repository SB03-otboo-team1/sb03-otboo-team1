package com.onepiece.otboo.domain.clothes.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;

public class ClothesParsingException extends ClothesException {

    public ClothesParsingException(String message) {
        super(ErrorCode.CLOTHES_PARSING_FAILED, Map.of("message", message));
    }

    public ClothesParsingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ClothesParsingException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

}
