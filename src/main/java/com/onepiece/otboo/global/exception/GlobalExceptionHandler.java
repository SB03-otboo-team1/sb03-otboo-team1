package com.onepiece.otboo.global.exception;

import com.onepiece.otboo.domain.auth.exception.TokenExpiredException;
import com.onepiece.otboo.domain.auth.exception.TokenForgedException;
import com.onepiece.otboo.global.dto.response.ErrorResponse;
import com.onepiece.otboo.infra.security.exception.SecurityForbiddenException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException e) {
        return ResponseEntity
            .status(ErrorCode.NOT_FOUND.getStatus())
            .body(ErrorResponse.of(ErrorCode.NOT_FOUND, e,
                Map.of("reason", "요청 경로를 찾을 수 없음")));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
        HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
            .status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
            .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, e,
                Map.of("reason", "허용되지 않은 HTTP 메서드")));
    }

    @ExceptionHandler({TokenExpiredException.class, TokenForgedException.class})
    public ResponseEntity<ErrorResponse> handleAuthTokenException(GlobalException e) {
        return getErrorResponseResponseEntity(e);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
        AuthorizationDeniedException e
    ) {
        return getErrorResponseResponseEntity(new SecurityForbiddenException(e));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleCustomException(GlobalException e) {
        return getErrorResponseResponseEntity(e);
    }

    private ResponseEntity<ErrorResponse> getErrorResponseResponseEntity(GlobalException e) {
        Map<String, String> details = e.getDetails().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> String.valueOf(entry.getValue())
            ));
        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(), e, details);
        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(errorResponse);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, e,
                Map.of("reason", "예상치 못한 오류가 발생했습니다.")));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<String> errors = bindingResult.getFieldErrors().stream()
            .map(error -> String.format("[field=%s, rejected=%s, message=%s]", error.getField(),
                error.getRejectedValue(), error.getDefaultMessage()))
            .toList();
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e,
                Map.of("validationError", String.valueOf(errors))));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
        HttpMessageNotReadableException e) {
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
            .body(ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE, e,
                Map.of("reason", "잘못된 요청 형식")));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
        HttpMediaTypeNotSupportedException e) {
        return ResponseEntity
            .status(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getStatus())
            .body(ErrorResponse.of(
                ErrorCode.UNSUPPORTED_MEDIA_TYPE, e,
                Map.of(
                    "reason", "지원하지 않는 Content-Type",
                    "supported", String.valueOf(e.getSupportedMediaTypes())
                )));
    }
}
