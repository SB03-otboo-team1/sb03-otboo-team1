package com.onepiece.otboo.domain.clothes.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;

/**
 * 의상을 찾을 수 없을 때 발생하는 예외
 */
public class ClothesNotFoundException extends ClothesException {

    public ClothesNotFoundException(String message) {
        super(ErrorCode.CLOTHES_NOT_FOUND, Map.of("message", message));
    }

    public ClothesNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ClothesNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}

