package com.onepiece.otboo.global.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.onepiece.otboo.global.dto.response.ErrorResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 커스텀예외_핸들러_테스트() {
        GlobalException ex = new GlobalException(ErrorCode.USER_NOT_FOUND, Map.of("userId", 123L));
        ResponseEntity<ErrorResponse> response = handler.handleCustomException(ex);

        assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body);
        assertTrue(body.toString().contains(ErrorCode.USER_NOT_FOUND.getMessage()));
        assertTrue(body.toString().contains("userId=123"));
    }

    @Test
    void 기본예외_핸들러_테스트() {
        Exception ex = new Exception("서버 에러");
        ResponseEntity<ErrorResponse> response = handler.handleException(ex);

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getStatus(), response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body);
        assertTrue(body.toString().contains(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
