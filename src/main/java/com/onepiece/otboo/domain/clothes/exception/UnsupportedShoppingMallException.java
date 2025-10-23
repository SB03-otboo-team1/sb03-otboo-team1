package com.onepiece.otboo.domain.clothes.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;

public class UnsupportedShoppingMallException extends ClothesException {

    public UnsupportedShoppingMallException(String message) {
        super(ErrorCode.UNSUPPORTED_SHOPPING_MALL, Map.of("message", message));
    }

    public UnsupportedShoppingMallException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnsupportedShoppingMallException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
