package com.onepiece.otboo.global.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CustomExceptionTest {

    @Test
    void 에러코드와_details가_포함된_커스텀예외_생성() {
        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;
        Map<String, Object> details = Map.of("userId", 123L);
        CustomException exception = new CustomException(errorCode, details);

        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorCode.getMessage(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getErrorCode().getStatus());
        assertEquals("존재하지 않는 사용자입니다.", exception.getErrorCode().getDetail());
        assertEquals(details, exception.getDetails());
    }

    @Test
    void details가_없는_커스텀예외는_빈_details_반환() {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        CustomException exception = new CustomException(errorCode);

        assertEquals(errorCode.getMessage(), exception.getMessage());
        assertTrue(exception.getDetails().isEmpty());
    }
}
