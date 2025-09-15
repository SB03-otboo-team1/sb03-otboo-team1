package com.onepiece.otboo.global.exception;

import com.onepiece.otboo.global.dto.response.ErrorResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    public ResponseEntity<ErrorResponse> handleNoPageFoundException(Exception e) {
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, e,
                Map.of("reason", "No handler or unsupported method")));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleCustomException(GlobalException e) {
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
                Map.of("reason", "Unexpected error")));
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

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e,
                Map.of("reason", "잘못된 요청 형식 또는 Content-Type")));
    }
}
