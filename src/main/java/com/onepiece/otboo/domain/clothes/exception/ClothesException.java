package com.onepiece.otboo.domain.clothes.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.Map;

public class ClothesException extends GlobalException {

    public ClothesException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ClothesException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
