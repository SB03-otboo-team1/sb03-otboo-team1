package com.onepiece.otboo.global.exception;

import com.onepiece.otboo.global.dto.response.ErrorResponseDto;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {NoHandlerFoundException.class,
        HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ErrorResponseDto> handleNoPageFoundException(Exception e) {
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ErrorResponseDto.of(ErrorCode.INTERNAL_SERVER_ERROR, e,
                Map.of("reason", "No handler or unsupported method")));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponseDto> handleCustomException(GlobalException e) {
        ErrorResponseDto errorResponse = ErrorResponseDto.of(e.getErrorCode(), e, e.getDetails());
        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(errorResponse);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ErrorResponseDto.of(ErrorCode.INTERNAL_SERVER_ERROR, e,
                Map.of("reason", "Unexpected error")));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<String> errors = bindingResult.getFieldErrors().stream()
            .map(error -> String.format("[field=%s, rejected=%s, message=%s]", error.getField(),
                error.getRejectedValue(), error.getDefaultMessage()))
            .toList();
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
            .body(ErrorResponseDto.of(ErrorCode.INVALID_INPUT_VALUE, e,
                Map.of("validationError", errors)));
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ErrorResponseDto> handleBadRequest(Exception e) {
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
            .body(ErrorResponseDto.of(ErrorCode.INVALID_INPUT_VALUE, e,
                Map.of("reason", "잘못된 요청 형식 또는 Content-Type")));
    }
}
