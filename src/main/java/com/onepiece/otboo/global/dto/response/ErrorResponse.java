package com.onepiece.otboo.global.dto.response;

import com.onepiece.otboo.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

public record ErrorResponse(
    @Schema(description = "예외 이름")
    String exceptionName,

    @Schema(description = "오류 메시지")
    String message,

    @Schema(description = "오류 부가 정보")
    Map<String, String> details
) {

    public static ErrorResponse of(
        ErrorCode errorCode,
        Throwable e,
        Map<String, String> details
    ) {
        return new ErrorResponse(
            e.getClass().getSimpleName(),
            errorCode.getMessage(),
            details
        );
    }
}
