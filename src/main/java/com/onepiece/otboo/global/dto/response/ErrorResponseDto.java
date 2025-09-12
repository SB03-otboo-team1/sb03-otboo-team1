package com.onepiece.otboo.global.dto.response;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;

public record ErrorResponseDto(
    String code,
    String message,
    String detail,
    Map<String, Object> details
) {

    public static ErrorResponseDto of(ErrorCode errorCode, Throwable e,
        Map<String, Object> details) {
        return new ErrorResponseDto(
            errorCode.name(),
            errorCode.getMessage(),
            errorCode.getDetail(),
            details
        );
    }
}
